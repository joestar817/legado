package io.legado.app.help.ai

import com.google.gson.reflect.TypeToken
import io.legado.app.utils.GSON

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
        val result = AiManager.generateText(
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
            params = AiTextParams(
                temperature = 0f,
                maxTokens = (source.length * 2 + 64).coerceIn(256, 4096),
                disableThinking = true
            )
        )
        val cleaned = normalizeModelOutput(result.content)
        check(cleaned.isNotBlank()) { "AI 返回空内容" }
        val validation = validate(source, cleaned)
        return AiPurifyResult(
            original = source,
            cleaned = cleaned,
            deletedPreview = validation.deletedPreview,
            canAutoApply = validation.canAutoApply,
            riskReason = validation.riskReason,
            model = result.model,
            paragraphIndex = paragraphIndex
        )
    }

    suspend fun purifyParagraphs(
        paragraphs: List<String>,
        chapterTitle: String? = null
    ): List<AiPurifyResult> {
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
        val batchResults = inputs.chunkForBatch().flatMap { purifyBatch(it) }
        return batchResults.mapIndexed { index, result ->
            val input = inputs[index]
            if (result.deletedPreview.isBlank() && input.needsSingleReviewAfterBatchMiss()) {
                runCatching {
                    purifyText(
                        text = input.text,
                        prompt = AiPromptStore.Prompt.CHAPTER_OPTIMIZE,
                        paragraphIndex = input.id
                    )
                }.getOrElse { result }
            } else {
                result
            }
        }
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

    private suspend fun purifyBatch(inputs: List<BatchInput>): List<AiPurifyResult> {
        val payload = GSON.toJson(inputs)
        val sourceLength = inputs.sumOf { it.text.length }
        val result = AiManager.generateText(
            messages = listOf(
                AiMessage(
                    AiMessage.Role.SYSTEM,
                    """
                    固定协议：
                    用户会给你一个 JSON 数组，每项包含 id 和 text。
                    你只能返回 JSON 数组，不要解释，不要使用 Markdown。
                    数组中只允许返回发生删除的段落，每项必须只包含 id 和 cleaned 两个字段。
                    cleaned 必须是删除污染内容后的结果，不得返回原文，不得使用 text 或 content 字段。
                    如果整段都是广告、群号、版权声明、站点宣传或无关提示，cleaned 必须返回空字符串 ""。
                    如果某段不需要删除任何内容，或你不确定是否应删除，必须完全省略该段。
                    严禁返回 cleaned 与原文完全相同的段落。

                    任务说明：
                    ${AiPromptStore.prompt(AiPromptStore.Prompt.CHAPTER_OPTIMIZE)}
                    """.trimIndent()
                ),
                AiMessage(AiMessage.Role.USER, payload)
            ),
            params = AiTextParams(
                temperature = 0f,
                maxTokens = (sourceLength * 2 + inputs.size * 48 + 128).coerceIn(512, 8192),
                disableThinking = true
            )
        )
        val outputs = parseBatchOutput(result.content)
        val outputMap = outputs.associateBy { it.id }
        return inputs.map { input ->
            val output = outputMap[input.id]
            val cleaned = if (output == null) {
                input.text
            } else {
                normalizeModelOutput(output.cleanedText.orEmpty())
            }
            val validation = validate(input.text, cleaned)
            AiPurifyResult(
                original = input.text,
                cleaned = cleaned,
                deletedPreview = validation.deletedPreview,
                canAutoApply = validation.canAutoApply,
                riskReason = validation.riskReason,
                model = result.model,
                paragraphIndex = input.id
            )
        }
    }

    private fun parseBatchOutput(text: String): List<BatchOutput> {
        var output = normalizeModelOutput(text)
        val start = output.indexOf('[')
        val end = output.lastIndexOf(']')
        check(start >= 0 && end > start) { "AI 未返回 JSON 数组" }
        output = output.substring(start, end + 1)
        val type = object : TypeToken<List<BatchOutput>>() {}.type
        return GSON.fromJson<List<BatchOutput>>(output, type)
            ?: error("AI 返回 JSON 解析失败")
    }

    private fun List<BatchInput>.chunkForBatch(): List<List<BatchInput>> {
        val maxBatchInputLength = AiConfig.purifyChapterSegmentLimit
        val chunks = arrayListOf<List<BatchInput>>()
        val current = arrayListOf<BatchInput>()
        var currentLength = 0
        for (input in this) {
            if (current.isNotEmpty() && currentLength + input.text.length > maxBatchInputLength) {
                chunks.add(current.toList())
                current.clear()
                currentLength = 0
            }
            current.add(input)
            currentLength += input.text.length
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
                deletedPreview = "",
                canAutoApply = false,
                riskReason = "AI 未删除任何内容"
            )
        }
        if (cleaned.length > original.length) {
            return ValidationResult(
                deletedPreview = "",
                canAutoApply = false,
                riskReason = "AI 输出比原文更长，可能发生改写"
            )
        }
        val deletedChars = ArrayList<Char>()
        var cleanedIndex = 0
        for (char in original) {
            if (cleanedIndex < cleaned.length && char == cleaned[cleanedIndex]) {
                cleanedIndex++
            } else {
                deletedChars.add(char)
            }
        }
        if (cleanedIndex != cleaned.length) {
            return ValidationResult(
                deletedPreview = original.bestEffortDeletedPreview(cleaned),
                canAutoApply = false,
                riskReason = "AI 输出不是原文删除后的结果，可能发生改写"
            )
        }
        val deleted = deletedChars.joinToString("")
        return ValidationResult(
            deletedPreview = deleted.compactPreview(),
            canAutoApply = true,
            riskReason = null
        )
    }

    private fun BatchInput.needsSingleReviewAfterBatchMiss(): Boolean {
        if (id == 1 && text.isLikelyChapterTitle()) {
            return false
        }
        return text.hasSuspiciousNoiseMarker()
    }

    private fun String.isChapterTitleForPurify(chapterTitle: String?): Boolean {
        return this == chapterTitle || isLikelyChapterTitle()
    }

    private fun String.isLikelyChapterTitle(): Boolean {
        if (length > 40) return false
        return Regex("""^第.{1,12}[章节卷集回部].*""").containsMatchIn(this)
    }

    private fun String.hasSuspiciousNoiseMarker(): Boolean {
        return any { char ->
            val block = Character.UnicodeBlock.of(char)
            block == Character.UnicodeBlock.ENCLOSED_ALPHANUMERICS
                || block == Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS
                || char in "∴∵∷∞≧≦≥≤♂♀※☆★○●◎◇◆□■△▲▽▼→←↔↕↖↗↘↙"
        }
    }

    private fun String.compactPreview(maxLength: Int = 160): String {
        val compact = replace("\n", "\\n")
        return if (compact.length <= maxLength) {
            compact
        } else {
            compact.take(maxLength) + "..."
        }
    }

    private fun String.bestEffortDeletedPreview(cleaned: String): String {
        return deletedByLcs(cleaned)
            .ifBlank { deletedByCommonWindow(cleaned) }
            .compactPreview()
    }

    private fun String.deletedByLcs(cleaned: String): String {
        val original = this
        if (original.isEmpty()) return ""
        if (cleaned.isEmpty()) return original
        val cellCount = original.length.toLong() * cleaned.length.toLong()
        if (cellCount > 4_000_000L) return ""
        val cols = cleaned.length + 1
        val table = IntArray((original.length + 1) * cols)
        for (i in 1..original.length) {
            val originalChar = original[i - 1]
            for (j in 1..cleaned.length) {
                val index = i * cols + j
                table[index] = if (originalChar == cleaned[j - 1]) {
                    table[(i - 1) * cols + j - 1] + 1
                } else {
                    maxOf(table[(i - 1) * cols + j], table[i * cols + j - 1])
                }
            }
        }
        val deleted = StringBuilder()
        var i = original.length
        var j = cleaned.length
        while (i > 0) {
            if (j > 0 && original[i - 1] == cleaned[j - 1]) {
                i--
                j--
            } else if (j > 0 && table[i * cols + j - 1] >= table[(i - 1) * cols + j]) {
                j--
            } else {
                deleted.append(original[i - 1])
                i--
            }
        }
        return deleted.reverse().toString()
    }

    private fun String.deletedByCommonWindow(cleaned: String): String {
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

    private data class ValidationResult(
        val deletedPreview: String,
        val canAutoApply: Boolean,
        val riskReason: String?
    )

    private data class BatchInput(
        val id: Int,
        val text: String
    )

    private data class BatchOutput(
        val id: Int,
        val cleaned: String? = null,
        val text: String? = null,
        val content: String? = null
    ) {
        val cleanedText: String?
            get() = cleaned ?: text ?: content
    }
}

data class AiPurifyResult(
    val original: String,
    val cleaned: String,
    val deletedPreview: String,
    val canAutoApply: Boolean,
    val riskReason: String?,
    val model: String?,
    val paragraphIndex: Int? = null
)
