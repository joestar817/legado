package io.legado.app.help.ai

import com.google.gson.annotations.SerializedName
import io.legado.app.utils.GSON
import kotlinx.coroutines.delay

object AiPurifyHelper {

    suspend fun purify(text: String): AiPurifyResult {
        return purifyText(
            text = text,
            prompt = AiPromptStore.Prompt.PARAGRAPH_PURIFY,
            maxInputLength = AiConfig.purifyParagraphLimit,
            lengthError = "选中文本过长，当前上限 ${AiConfig.purifyParagraphLimit} 字，请分段选择后再净化"
        )
    }

    private suspend fun purifyText(
        text: String,
        prompt: AiPromptStore.Prompt,
        maxInputLength: Int? = null,
        lengthError: String? = null,
        paragraphIndex: Int? = null
    ): AiPurifyResult {
        val source = normalizeSelectedText(text)
        check(source.isNotBlank()) { "选中文本为空" }
        if (maxInputLength != null) {
            check(source.length <= maxInputLength) {
                lengthError ?: "文本过长，当前上限 ${maxInputLength} 字"
            }
        }
        val selection = AiConfig.requirePurifyModel()
        val result = AiManager.generateText(
            providerId = selection.providerId,
            modelId = selection.modelId,
            messages = listOf(
                AiMessage(
                    AiMessage.Role.SYSTEM,
                    """
                    固定协议：
                    1. 只输出净化后的正文。
                    2. 不要解释，不要输出 JSON，不要使用 Markdown。

                    任务说明：
                    ${AiPromptStore.prompt(prompt)}
                    """.trimIndent()
                ),
                AiMessage(AiMessage.Role.USER, source)
            ),
            params = AiConfig.purifyParagraphParams(
                inputLength = source.length,
                supportsReasoning = selection.supportsReasoning()
            )
        )
        val cleaned = normalizeModelOutput(result.content)
        check(cleaned.isNotBlank()) { result.emptyContentMessage() }
        val validation = validate(source, cleaned)
        return AiPurifyResult(
            original = source,
            cleaned = cleaned,
            deletedCount = validation.deletedCount,
            replacementCount = validation.replacementCount,
            deletedPreview = validation.deletedPreview,
            replacementPreview = validation.replacementPreview,
            canAutoApply = validation.canAutoApply,
            riskReason = validation.riskReason,
            model = result.model,
            paragraphIndex = paragraphIndex
        )
    }

    suspend fun generateRuleCandidates(
        paragraphs: List<String>,
        chapterTitle: String? = null
    ): AiPurifyRuleGenerateResult {
        val normalizedChapterTitle = chapterTitle?.let { normalizeSelectedText(it) }
        val inputs = paragraphs
            .mapIndexedNotNull { index, text ->
                val source = normalizeSelectedText(text)
                when {
                    source.isBlank() -> null
                    index == 0 && source.isChapterTitleForPurify(normalizedChapterTitle) -> null
                    else -> BatchInput(index + 1, source)
                }
        }
        check(inputs.isNotEmpty()) { "当前章节正文为空" }
        val chunkResults = inputs.chunkForBatch().map { generateRuleBatchWithRetry(it) }
        val rules = chunkResults
            .flatMap { it.rules }
            .distinctBy { "${it.type}\u0000${it.old}\u0000${it.new}" }
        val model = chunkResults
            .mapNotNull { it.model?.takeIf { value -> value.isNotBlank() } }
            .distinct()
            .joinToString(" + ")
            .ifBlank { null }
        return AiPurifyRuleGenerateResult(
            rules = rules,
            model = model,
            originalCharCount = inputs.sumOf { it.input.length }
        )
    }

    fun normalizeSelectedText(text: String): String {
        return text.lineSequence()
            .map { it.trim() }
            .joinToString("\n")
            .trim()
    }

    private fun normalizeModelOutput(text: String): String {
        var output = text.trim()
        if (output.startsWith("```")) {
            output = output.lines()
                .drop(1)
                .dropLastWhile { it.trim() == "```" }
                .joinToString("\n")
                .trim()
        }
        return output
    }

    private fun AiTextResult.emptyContentMessage(): String {
        val reasoning = reasoning.orEmpty().trim()
        return if (reasoning.isNotBlank()) {
            if (finishReason == "length") {
                "AI 仅返回思考过程，未返回正文。请调低或关闭思考深度后重试"
            } else {
                "AI 仅返回思考过程，未返回正文"
            }
        } else {
            "AI 返回空内容"
        }
    }

    private suspend fun generateRuleBatch(inputs: List<BatchInput>): AiPurifyRuleGenerateResult {
        val payload = GSON.toJson(inputs)
        val sourceLength = inputs.sumOf { it.input.length }
        val selection = AiConfig.requirePurifyModel()
        val result = AiManager.generateText(
            providerId = selection.providerId,
            modelId = selection.modelId,
            messages = listOf(
                AiMessage(
                    AiMessage.Role.SYSTEM,
                    """
                    固定协议：
                    用户会给你一个 JSON 数组，每项只包含 id 和 input。
                    你只能返回一个 JSON 对象，不要解释，不要使用 Markdown。
                    JSON 对象只允许包含 rules 字段。
                    rules 是数组；没有候选规则时返回 {"rules":[]}。
                    rules 中每一项必须只包含 id、type、old、new 四个字段。
                    最终输出必须是完整合法 JSON，最后一个字符必须是 }。
                    如果候选很多，只返回最明确的候选；宁可少返回，也不要输出超长列表导致 JSON 不完整。
                    id 必须来自输入段落 id。
                    type 只能是 typo、noise、ad。
                    old 必须是输入原文中真实存在的连续文本。
                    new 是替换后的文本；删除时必须是空字符串。
                    严禁返回 cleaned、output、text、content、confidence、reason、evidenceIds 或其它字段。

                    任务说明：
                    ${AiPromptStore.prompt(AiPromptStore.Prompt.RULE_GENERATE)}
                    """.trimIndent()
                ),
                AiMessage(AiMessage.Role.USER, payload)
            ),
            params = AiConfig.purifyChapterRuleParams(
                sourceLength = sourceLength,
                paragraphCount = inputs.size,
                supportsReasoning = selection.supportsReasoning()
            )
        )
        check(result.content.isNotBlank()) { result.emptyContentMessage() }
        val parsedRules = parseRuleOutput(result.content).rules
        val inputMap = inputs.associateBy { it.id }
        val validRules = parsedRules
            .mapNotNull { it.validated(inputMap) }
            .distinctBy { "${it.type}\u0000${it.old}\u0000${it.new}" }
        return AiPurifyRuleGenerateResult(
            rules = validRules,
            model = result.model,
            originalCharCount = sourceLength
        )
    }

    private suspend fun generateRuleBatchWithRetry(inputs: List<BatchInput>): AiPurifyRuleGenerateResult {
        val maxAttempts = AiConfig.purifyChapterRetryCount + 1
        var lastError: Throwable? = null
        repeat(maxAttempts) { attemptIndex ->
            try {
                return generateRuleBatch(inputs)
            } catch (e: Throwable) {
                lastError = e
                if (attemptIndex < maxAttempts - 1) {
                    delay(300L * (attemptIndex + 1))
                }
            }
        }
        throw lastError ?: IllegalStateException("AI 规则生成失败")
    }

    private fun parseRuleOutput(text: String): RuleOutput {
        val output = normalizeModelOutput(text)
        val candidate = output.extractJsonObjectCandidate()
        check(candidate.isNotBlank()) { "AI 未返回 JSON 对象" }
        val closedCandidate = candidate.closeJsonStructures()
        return parseRuleOutputJson(candidate)
            ?: parseRuleOutputJson(closedCandidate)
            ?: parseRuleOutputJson(candidate.repairJsonForModelOutput())
            ?: parseRuleOutputJson(closedCandidate.repairJsonForModelOutput())
            ?: error("AI 返回 JSON 解析失败")
    }

    private fun parseRuleOutputJson(output: String): RuleOutput? {
        return runCatching {
            GSON.fromJson(output, RuleOutput::class.java)
        }.getOrNull()
    }

    private fun String.extractJsonObjectCandidate(): String {
        val start = indexOf('{')
        if (start < 0) {
            return ""
        }
        val source = substring(start).trim()
        var inString = false
        var escaped = false
        var depth = 0
        source.forEachIndexed { index, char ->
            if (escaped) {
                escaped = false
                return@forEachIndexed
            }
            when {
                char == '\\' && inString -> escaped = true
                char == '"' -> inString = !inString
                inString -> Unit
                char == '{' -> depth++
                char == '}' -> {
                    depth--
                    if (depth == 0) {
                        return source.substring(0, index + 1)
                    }
                }
            }
        }
        return source
    }

    private fun String.closeJsonStructures(): String {
        val stack = arrayListOf<Char>()
        var inString = false
        var escaped = false
        forEach { char ->
            if (escaped) {
                escaped = false
                return@forEach
            }
            when {
                char == '\\' && inString -> escaped = true
                char == '"' -> inString = !inString
                inString -> Unit
                char == '{' -> stack.add('}')
                char == '[' -> stack.add(']')
                char == '}' || char == ']' -> {
                    if (stack.isNotEmpty() && stack.last() == char) {
                        stack.removeAt(stack.lastIndex)
                    }
                }
            }
        }
        if (inString || stack.isEmpty()) {
            return this
        }
        return this + stack.asReversed().joinToString("")
    }

    private fun String.repairJsonForModelOutput(): String {
        return escapeRawControlCharsInJsonStrings()
            .removeTrailingCommasInJson()
            .closeJsonStructures()
    }

    private fun String.escapeRawControlCharsInJsonStrings(): String {
        val builder = StringBuilder(length)
        var inString = false
        var escaped = false
        forEach { char ->
            if (escaped) {
                builder.append(char)
                escaped = false
                return@forEach
            }
            when {
                char == '\\' && inString -> {
                    builder.append(char)
                    escaped = true
                }

                char == '"' -> {
                    builder.append(char)
                    inString = !inString
                }

                inString && char == '\n' -> builder.append("\\n")
                inString && char == '\r' -> builder.append("\\r")
                inString && char == '\t' -> builder.append("\\t")
                inString && char.code < 0x20 -> {
                    builder.append("\\u")
                    builder.append(char.code.toString(16).padStart(4, '0'))
                }

                else -> builder.append(char)
            }
        }
        return builder.toString()
    }

    private fun String.removeTrailingCommasInJson(): String {
        val builder = StringBuilder(length)
        var inString = false
        var escaped = false
        forEachIndexed { index, char ->
            if (escaped) {
                builder.append(char)
                escaped = false
                return@forEachIndexed
            }
            when {
                char == '\\' && inString -> {
                    builder.append(char)
                    escaped = true
                }

                char == '"' -> {
                    builder.append(char)
                    inString = !inString
                }

                !inString && char == ',' -> {
                    val next = substring(index + 1).firstOrNull { !it.isWhitespace() }
                    if (next != '}' && next != ']') {
                        builder.append(char)
                    }
                }

                else -> builder.append(char)
            }
        }
        return builder.toString()
    }

    private fun AiPurifyRuleCandidate.validated(
        inputMap: Map<Int, BatchInput>
    ): AiPurifyRuleCandidate? {
        val source = inputMap[id]?.input ?: return null
        val ruleType = type.trim().lowercase()
        if (ruleType !in setOf("typo", "noise", "ad")) {
            return null
        }
        val normalizedOld = normalizeSelectedText(old)
        val normalizedNew = normalizeSelectedText(new)
        if (
            normalizedOld.isBlank() ||
            normalizedOld.isNormalizedSameAs(normalizedNew) ||
            !source.contains(normalizedOld)
        ) {
            return null
        }
        if (normalizedOld.length == 1) {
            return null
        }
        if (ruleType == "typo") {
            if (normalizedNew.isBlank() || normalizedNew.length < 2) {
                return null
            }
            if (normalizedOld.contains('幺') && normalizedNew.contains('吗')) {
                return null
            }
        }
        if (ruleType == "ad" && (normalizedNew.isNotBlank() || normalizedOld != source)) {
            return null
        }
        if (ruleType == "noise" && normalizedNew.isBlank() && normalizedOld.length < 4) {
            return null
        }
        return AiPurifyRuleCandidate(
            id = id,
            type = ruleType,
            old = normalizedOld,
            new = normalizedNew
        )
    }

    private fun List<BatchInput>.chunkForBatch(): List<List<BatchInput>> {
        val maxBatchInputLength = AiConfig.purifyChapterSegmentLimit
        val chunks = arrayListOf<List<BatchInput>>()
        val current = arrayListOf<BatchInput>()
        var currentLength = 0
        for (input in this) {
            if (current.isNotEmpty() && currentLength + input.input.length > maxBatchInputLength) {
                chunks.add(current.toList())
                current.clear()
                currentLength = 0
            }
            current.add(input)
            currentLength += input.input.length
        }
        if (current.isNotEmpty()) {
            chunks.add(current.toList())
        }
        return chunks
    }

    private fun validate(
        original: String,
        cleaned: String
    ): ValidationResult {
        if (cleaned == original) {
            return ValidationResult(
                deletedCount = 0,
                replacementCount = 0,
                deletedPreview = "",
                replacementPreview = "",
                canAutoApply = false,
                riskReason = "AI 未修改任何内容"
            )
        }
        val summary = summarizeChange(original, cleaned)
        if (summary.insertedCount > 0) {
            return ValidationResult(
                deletedCount = summary.deletedCount,
                replacementCount = summary.replacementCount,
                deletedPreview = summary.deletedPreview,
                replacementPreview = summary.replacementPreview,
                canAutoApply = false,
                riskReason = "AI 输出包含新增内容，可能发生改写"
            )
        }
        return ValidationResult(
            deletedCount = summary.deletedCount,
            replacementCount = summary.replacementCount,
            deletedPreview = summary.deletedPreview,
            replacementPreview = summary.replacementPreview,
            canAutoApply = true,
            riskReason = null
        )
    }

    private fun String.isChapterTitleForPurify(chapterTitle: String?): Boolean {
        return this == chapterTitle || isLikelyChapterTitle()
    }

    private fun String.isNormalizedSameAs(other: String): Boolean {
        return this == other ||
                java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFKC) ==
                java.text.Normalizer.normalize(other, java.text.Normalizer.Form.NFKC)
    }

    private fun String.isLikelyChapterTitle(): Boolean {
        if (length > 40) return false
        return Regex("""^第.{1,12}[章节卷集回部].*""").containsMatchIn(this)
    }

    private fun String.compactPreview(maxLength: Int = 160): String {
        val compact = replace("\n", "\\n")
        return if (compact.length <= maxLength) {
            compact
        } else {
            compact.take(maxLength) + "..."
        }
    }

    private fun summarizeChange(original: String, cleaned: String): ChangeSummary {
        val cellCount = (original.length + 1L) * (cleaned.length + 1L)
        if (cellCount > 4_000_000L) {
            return original.summarizeChangeByCommonWindow(cleaned)
        }
        val width = cleaned.length + 1
        val cost = IntArray((original.length + 1) * width)
        for (i in 0..original.length) {
            cost[i * width] = i
        }
        for (j in 0..cleaned.length) {
            cost[j] = j
        }
        for (i in 1..original.length) {
            val row = i * width
            val prevRow = (i - 1) * width
            for (j in 1..cleaned.length) {
                val replaceCost = cost[prevRow + j - 1] +
                    if (original[i - 1] == cleaned[j - 1]) 0 else 1
                val deleteCost = cost[prevRow + j] + 1
                val insertCost = cost[row + j - 1] + 1
                cost[row + j] = minOf(replaceCost, deleteCost, insertCost)
            }
        }
        val deletedChars = ArrayList<Char>()
        val replacements = ArrayList<String>()
        var insertedCount = 0
        var i = original.length
        var j = cleaned.length
        while (i > 0 || j > 0) {
            val current = cost[i * width + j]
            if (
                i > 0 &&
                j > 0 &&
                original[i - 1] == cleaned[j - 1] &&
                current == cost[(i - 1) * width + j - 1]
            ) {
                i--
                j--
            } else if (
                i > 0 &&
                j > 0 &&
                current == cost[(i - 1) * width + j - 1] + 1
            ) {
                replacements.add("${original[i - 1]} -> ${cleaned[j - 1]}")
                i--
                j--
            } else if (i > 0 && current == cost[(i - 1) * width + j] + 1) {
                deletedChars.add(original[i - 1])
                i--
            } else {
                insertedCount++
                j--
            }
        }
        deletedChars.reverse()
        replacements.reverse()
        return ChangeSummary(
            deletedCount = deletedChars.size,
            replacementCount = replacements.size,
            insertedCount = insertedCount,
            deletedPreview = deletedChars.joinToString("").compactPreview(),
            replacementPreview = replacements.compactReplacementPreview()
        )
    }

    private fun String.summarizeChangeByCommonWindow(cleaned: String): ChangeSummary {
        val originalWindow = changedByCommonWindow(cleaned)
        val cleanedWindow = cleaned.changedByCommonWindow(this)
        val replacementCount = minOf(originalWindow.length, cleanedWindow.length)
        val deletedCount = (originalWindow.length - cleanedWindow.length).coerceAtLeast(0)
        val insertedCount = (cleanedWindow.length - originalWindow.length).coerceAtLeast(0)
        return ChangeSummary(
            deletedCount = deletedCount,
            replacementCount = replacementCount,
            insertedCount = insertedCount,
            deletedPreview = originalWindow.drop(replacementCount).compactPreview(),
            replacementPreview = originalWindow
                .zip(cleanedWindow)
                .map { "${it.first} -> ${it.second}" }
                .compactReplacementPreview()
        )
    }

    private fun String.changedByCommonWindow(cleaned: String): String {
        val original = this
        var prefix = 0
        val maxPrefix = minOf(original.length, cleaned.length)
        while (prefix < maxPrefix && original[prefix] == cleaned[prefix]) {
            prefix++
        }
        var originalSuffix = original.length
        var cleanedSuffix = cleaned.length
        while (
            originalSuffix > prefix &&
            cleanedSuffix > prefix &&
            original[originalSuffix - 1] == cleaned[cleanedSuffix - 1]
        ) {
            originalSuffix--
            cleanedSuffix--
        }
        return original.substring(prefix, originalSuffix)
    }

    private fun List<String>.compactReplacementPreview(maxItems: Int = 8): String {
        if (isEmpty()) return ""
        val counts = linkedMapOf<String, Int>()
        for (item in this) {
            counts[item] = (counts[item] ?: 0) + 1
        }
        val preview = counts.entries
            .take(maxItems)
            .joinToString("、") { (item, count) ->
                if (count > 1) "$item×$count" else item
            }
        return if (counts.size > maxItems) "$preview..." else preview
    }

    private data class ChangeSummary(
        val deletedCount: Int,
        val replacementCount: Int,
        val insertedCount: Int,
        val deletedPreview: String,
        val replacementPreview: String
    )

    private data class ValidationResult(
        val deletedCount: Int,
        val replacementCount: Int,
        val deletedPreview: String,
        val replacementPreview: String,
        val canAutoApply: Boolean,
        val riskReason: String?
    )

    private fun AiModelSelection.supportsReasoning(): Boolean {
        return AiProviderStore.provider(providerId)
            ?.models
            ?.firstOrNull { it.id == modelId }
            ?.abilities
            ?.contains(AiModelAbility.REASONING) == true
    }

    private data class BatchInput(
        @SerializedName("id")
        val id: Int,
        @SerializedName("input")
        val input: String
    )

    private data class RuleOutput(
        @SerializedName("rules")
        val rules: List<AiPurifyRuleCandidate> = emptyList()
    )
}

data class AiPurifyResult(
    val original: String,
    val cleaned: String,
    val deletedCount: Int,
    val replacementCount: Int,
    val deletedPreview: String,
    val replacementPreview: String,
    val canAutoApply: Boolean,
    val riskReason: String?,
    val model: String?,
    val paragraphIndex: Int? = null,
    val chapterIndex: Int? = null,
    val chapterTitle: String? = null
)

data class AiPurifyRuleGenerateResult(
    val rules: List<AiPurifyRuleCandidate>,
    val model: String?,
    val originalCharCount: Int
)

data class AiPurifyRuleCandidate(
    @SerializedName("id")
    val id: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("old")
    val old: String,
    @SerializedName("new")
    val new: String
)
