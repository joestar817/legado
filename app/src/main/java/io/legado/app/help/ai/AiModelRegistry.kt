package io.legado.app.help.ai

data class AiModelCapabilities(
    val type: AiModelType? = null,
    val inputModalities: List<AiModelModality> = emptyList(),
    val outputModalities: List<AiModelModality> = emptyList(),
    val abilities: List<AiModelAbility> = emptyList(),
    val reasoning: AiModelReasoningOptions = AiModelReasoningOptions()
)

data class AiModelReasoningOptions(
    val thinkingParam: String = "",
    val effortParam: String = "",
    val reasoningOutputField: String = "reasoning_content"
)

object AiModelRegistry {

    private val GPT4O = defineAiModel {
        tokens("gpt", "4", "o")
        visionInput()
        toolAbility()
    }

    private val GPT_4_1 = defineAiModel {
        tokens("gpt", "4", "1")
        visionInput()
        toolAbility()
    }

    private val OPENAI_O_MODELS = defineAiModel {
        tokens(aiTokenRegex("^o$"), aiTokenRegex("^\\d+$"))
        visionInput()
        toolReasoningAbility()
    }

    private val GPT_OSS = defineAiModel {
        tokens("gpt", "oss")
        toolReasoningAbility()
    }

    private val GPT_5 = defineAiModel {
        tokens("gpt", "5")
        notTokens("gpt", "5", ".")
        notTokens("gpt", "5", "chat")
        visionInput()
        toolReasoningAbility()
    }

    private val GPT_5_1 = defineAiModel {
        tokens("gpt", "5", "1")
        visionInput()
        toolReasoningAbility()
    }

    private val GPT_5_2 = defineAiModel {
        tokens("gpt", "5", "2")
        visionInput()
        toolReasoningAbility()
    }

    private val GPT_5_3 = defineAiModel {
        tokens("gpt", "5", "3")
        visionInput()
        toolAbility()
    }

    private val GPT_5_4 = defineAiModel {
        tokens("gpt", "5", "4")
        visionInput()
        toolReasoningAbility()
    }

    private val GPT_5_4_MINI = defineAiModel {
        tokens("gpt", "5", "4", "mini")
        visionInput()
        toolReasoningAbility()
    }

    private val GPT_5_4_NANO = defineAiModel {
        tokens("gpt", "5", "4", "nano")
        visionInput()
        toolReasoningAbility()
    }

    private val GPT_5_5 = defineAiModel {
        tokens("gpt", "5", "5")
        visionInput()
        toolReasoningAbility()
    }

    private val GPT_5_6 = defineAiModel {
        tokens("gpt", "5", "6")
        visionInput()
        toolReasoningAbility()
    }

    private val GEMINI_20_FLASH = defineAiModel {
        tokens("gemini", "2", "0", "flash")
        visionInput()
        toolAbility()
    }

    private val GEMINI_2_5_FLASH = defineAiModel {
        tokens("gemini", "2", "5", "flash")
        notTokens("image")
        visionInput()
        toolReasoningAbility()
    }

    private val GEMINI_2_5_PRO = defineAiModel {
        tokens("gemini", "2", "5", "pro")
        visionInput()
        toolReasoningAbility()
    }

    private val GEMINI_2_5_IMAGE = defineAiModel {
        tokens("gemini", "2", "5", "flash", "image")
        visionInput()
        imageOutput()
    }

    private val GEMINI_3_PRO_IMAGE = defineAiModel {
        tokens("gemini", "3", "pro", "image")
        visionInput()
        imageOutput()
    }

    private val GEMINI_NANO_BANANA = defineAiModel {
        tokens("nano", "banana")
        visionInput()
        imageOutput()
    }

    private val GEMINI_3_PRO = defineAiModel {
        tokens("gemini", "3", "pro")
        visionInput()
        toolReasoningAbility()
    }

    private val GEMINI_3_FLASH = defineAiModel {
        tokens("gemini", "3", "flash")
        visionInput()
        toolReasoningAbility()
    }

    private val GEMINI_3_1_PRO_PREVIEW = defineAiModel {
        tokens("gemini", "3", "1", "pro", "preview")
        visionInput()
        toolReasoningAbility()
    }

    private val GEMINI_3_1_PRO_PREVIEW_CUSTOMTOOLS = defineAiModel {
        tokens("gemini", "3", "1", "pro", "preview", "customtools")
        visionInput()
        toolReasoningAbility()
    }

    private val GEMINI_3_1_FLASH_IMAGE = defineAiModel {
        tokens("gemini", "3", "1", "flash", "image")
        visionInput()
        imageOutput()
        reasoningAbility()
    }

    private val GEMINI_3_5 = defineAiModel {
        tokens("gemini", "3", "5")
        visionInput()
        toolReasoningAbility()
    }

    private val GEMINI_FLASH_LATEST = defineAiModel {
        exact("gemini-flash-latest")
        visionInput()
        toolReasoningAbility()
    }

    private val GEMINI_PRO_LATEST = defineAiModel {
        exact("gemini-pro-latest")
        visionInput()
        toolReasoningAbility()
    }

    @Suppress("unused")
    private val GEMINI_LATEST = defineAiModelGroup {
        add(GEMINI_FLASH_LATEST, GEMINI_PRO_LATEST)
    }

    @Suppress("unused")
    private val GEMINI_3_SERIES = defineAiModelGroup {
        add(
            GEMINI_3_PRO,
            GEMINI_3_FLASH,
            GEMINI_3_1_PRO_PREVIEW,
            GEMINI_3_1_PRO_PREVIEW_CUSTOMTOOLS,
            GEMINI_3_5
        )
    }

    @Suppress("unused")
    private val GEMINI_SERIES = defineAiModelGroup {
        add(GEMINI_20_FLASH, GEMINI_2_5_FLASH, GEMINI_2_5_PRO, GEMINI_3_SERIES, GEMINI_LATEST)
    }

    private val CLAUDE_SONNET_3_5 = defineAiModel {
        tokens("claude", "3", "5", "sonnet")
        visionInput()
        toolReasoningAbility()
    }

    private val CLAUDE_SONNET_3_7 = defineAiModel {
        tokens("claude", "3", "7", "sonnet")
        visionInput()
        toolReasoningAbility()
    }

    private val CLAUDE_4 = defineAiModel {
        tokens("claude", "4")
        visionInput()
        toolReasoningAbility()
    }

    private val CLAUDE_4_5 = defineAiModel {
        tokens("claude", "4", "5")
        visionInput()
        toolReasoningAbility()
    }

    private val CLAUDE_SONNET_4_6 = defineAiModel {
        tokens("claude", "sonnet", "4", "6")
        visionInput()
        toolReasoningAbility()
    }

    private val CLAUDE_OPUS_4_6 = defineAiModel {
        tokens("claude", "opus", "4", "6")
        visionInput()
        toolReasoningAbility()
    }

    private val CLAUDE_OPUS_4_7 = defineAiModel {
        tokens("claude", "opus", "4", "7")
        visionInput()
        toolReasoningAbility()
    }

    private val CLAUDE_OPUS_4_8 = defineAiModel {
        tokens("claude", "opus", "4", "8")
        visionInput()
        toolReasoningAbility()
    }

    @Suppress("unused")
    private val CLAUDE_SERIES = defineAiModelGroup {
        add(
            CLAUDE_SONNET_3_5,
            CLAUDE_SONNET_3_7,
            CLAUDE_4,
            CLAUDE_4_5,
            CLAUDE_SONNET_4_6,
            CLAUDE_OPUS_4_6,
            CLAUDE_OPUS_4_7,
            CLAUDE_OPUS_4_8
        )
    }

    private val DEEPSEEK_V3_MODEL = defineAiModel {
        tokens("deepseek", "v", "3")
        toolAbility()
    }

    private val DEEPSEEK_CHAT = defineAiModel {
        tokens("deepseek", "chat")
        toolAbility()
    }

    @Suppress("unused")
    private val DEEPSEEK_V3 = defineAiModelGroup {
        add(DEEPSEEK_V3_MODEL, DEEPSEEK_CHAT)
    }

    private val DEEPSEEK_R1_MODEL = defineAiModel {
        tokens("deepseek", "r", "1")
        toolReasoningAbility()
    }

    private val DEEPSEEK_REASONER = defineAiModel {
        tokens("deepseek", "reasoner")
        toolReasoningAbility()
    }

    private val DEEPSEEK_V4_FLASH = defineAiModel {
        tokens("deepseek", "v", "4", "flash")
        visionInput()
        toolReasoningAbility()
    }

    private val DEEPSEEK_V4_PRO = defineAiModel {
        tokens("deepseek", "v", "4", "pro")
        toolReasoningAbility()
    }

    private val OCR_MODEL = defineAiModel {
        tokens("ocr")
        visionInput()
    }

    @Suppress("unused")
    private val DEEPSEEK_R1 = defineAiModelGroup {
        add(DEEPSEEK_R1_MODEL, DEEPSEEK_REASONER)
    }

    private val DEEPSEEK_V3_1 = defineAiModel {
        tokens("deepseek", "v", "3", "1")
        toolReasoningAbility()
    }

    private val DEEPSEEK_V3_2 = defineAiModel {
        tokens("deepseek", "v", "3", "2")
        toolReasoningAbility()
    }

    private val QWEN_3 = defineAiModel {
        tokens("qwen", "3")
        toolReasoningAbility()
    }

    private val QWEN_3_5 = defineAiModel {
        tokens("qwen", "3", "5")
        visionInput()
        toolReasoningAbility()
    }

    private val QWEN_3_6 = defineAiModel {
        tokens("qwen", "3", "6")
        visionInput()
        toolReasoningAbility()
    }

    private val QWEN_3_7 = defineAiModel {
        tokens("qwen", "3", "7")
        visionInput()
        toolReasoningAbility()
    }

    private val DOUBAO_1_6 = defineAiModel {
        tokens("doubao", "1", "6")
        visionInput()
        toolReasoningAbility()
    }

    private val DOUBAO_1_8 = defineAiModel {
        tokens("doubao", "1", "8")
        visionInput()
        toolReasoningAbility()
    }

    private val GROK_4 = defineAiModel {
        tokens("grok", "4")
        visionInput()
        toolReasoningAbility()
    }

    private val KIMI_K2 = defineAiModel {
        tokens("kimi", "k", "2")
        toolReasoningAbility()
    }

    private val KIMI_K2_5 = defineAiModel {
        tokens("kimi", "k", "2", "5")
        visionInput()
        toolReasoningAbility()
    }

    private val KIMI_K2_6 = defineAiModel {
        tokens("kimi", "k", "2", "6")
        visionInput()
        toolReasoningAbility()
    }

    private val STEP_3 = defineAiModel {
        tokens("step", "3")
        visionInput()
        toolReasoningAbility()
    }

    private val STEP_3_7_FLASH = defineAiModel {
        tokens("step", "3", "7", "flash")
        visionInput()
        toolReasoningAbility()
    }

    private val INTERN_S1 = defineAiModel {
        tokens("intern", "s", "1")
        visionInput()
        toolReasoningAbility()
    }

    private val GLM_4_5 = defineAiModel {
        tokens("glm", "4", "5")
        toolReasoningAbility()
    }

    private val GLM_4_6 = defineAiModel {
        tokens("glm", "4", "6")
        toolReasoningAbility()
    }

    private val GLM_4_7 = defineAiModel {
        tokens("glm", "4", "7")
        toolReasoningAbility()
    }

    private val GLM_5 = defineAiModel {
        tokens("glm", "5")
        toolReasoningAbility()
    }

    private val GLM_5_1 = defineAiModel {
        tokens("glm", "5", "1")
        toolReasoningAbility()
    }

    private val MINIMAX_M2 = defineAiModel {
        tokens("minimax", "m", "2")
        toolReasoningAbility()
    }

    private val MINIMAX_M2_5 = defineAiModel {
        tokens("minimax", "m", "2", "5")
        toolReasoningAbility()
    }

    private val MINIMAX_M2_7 = defineAiModel {
        tokens("minimax", "m", "2", "7")
        toolReasoningAbility()
    }

    private val MINIMAX_M3 = defineAiModel {
        tokens("minimax", "m", "3")
        visionInput()
        toolReasoningAbility()
    }

    private val XIAOMI_MIMO_V2 = defineAiModel {
        tokens("mimo", "v", "2")
        excludeXiaomiSpeechModels()
        toolReasoningAbility()
    }

    private val XIAOMI_MIMO_V2_PRO = defineAiModel {
        tokens("mimo", "v", "2", "pro")
        excludeXiaomiSpeechModels()
        toolReasoningAbility()
    }

    private val XIAOMI_MIMO_V2_5 = defineAiModel {
        tokens("mimo", "v", "2", "5")
        excludeXiaomiSpeechModels()
        visionInput()
        toolReasoningAbility()
    }

    private val XIAOMI_MIMO_V2_5_PRO = defineAiModel {
        tokens("mimo", "v", "2", "5", "pro")
        excludeXiaomiSpeechModels()
        toolReasoningAbility()
    }

    private val XIAOMI_MIMO_TTS = defineAiModel {
        tokens("mimo", "tts")
        input(AiModelModality.TEXT)
        output(AiModelModality.AUDIO)
        ttsAbility()
    }

    private val XIAOMI_MIMO_ASR = defineAiModel {
        tokens("mimo", "asr")
        input(AiModelModality.AUDIO)
        output(AiModelModality.TEXT)
        asrAbility()
    }

    private val QWEN_MT = defineAiModel {
        tokens("qwen", "mt")
    }

    private val ALL_MODELS = listOf(
        GPT4O,
        GPT_4_1,
        OPENAI_O_MODELS,
        GPT_OSS,
        GPT_5,
        GPT_5_1,
        GPT_5_2,
        GPT_5_3,
        GPT_5_4,
        GPT_5_4_MINI,
        GPT_5_4_NANO,
        GPT_5_5,
        GPT_5_6,
        GEMINI_20_FLASH,
        GEMINI_2_5_FLASH,
        GEMINI_2_5_PRO,
        GEMINI_2_5_IMAGE,
        GEMINI_3_PRO_IMAGE,
        GEMINI_NANO_BANANA,
        GEMINI_3_PRO,
        GEMINI_3_FLASH,
        GEMINI_3_1_PRO_PREVIEW,
        GEMINI_3_1_PRO_PREVIEW_CUSTOMTOOLS,
        GEMINI_3_1_FLASH_IMAGE,
        GEMINI_3_5,
        GEMINI_FLASH_LATEST,
        GEMINI_PRO_LATEST,
        CLAUDE_SONNET_3_5,
        CLAUDE_SONNET_3_7,
        CLAUDE_4,
        CLAUDE_4_5,
        CLAUDE_SONNET_4_6,
        CLAUDE_OPUS_4_6,
        CLAUDE_OPUS_4_7,
        CLAUDE_OPUS_4_8,
        DEEPSEEK_V3_MODEL,
        DEEPSEEK_CHAT,
        DEEPSEEK_R1_MODEL,
        DEEPSEEK_REASONER,
        DEEPSEEK_V4_FLASH,
        DEEPSEEK_V4_PRO,
        OCR_MODEL,
        DEEPSEEK_V3_1,
        DEEPSEEK_V3_2,
        QWEN_3,
        QWEN_3_5,
        QWEN_3_6,
        QWEN_3_7,
        DOUBAO_1_6,
        DOUBAO_1_8,
        GROK_4,
        KIMI_K2,
        KIMI_K2_5,
        KIMI_K2_6,
        STEP_3,
        STEP_3_7_FLASH,
        INTERN_S1,
        GLM_4_5,
        GLM_4_6,
        GLM_4_7,
        GLM_5,
        GLM_5_1,
        MINIMAX_M2,
        MINIMAX_M2_5,
        MINIMAX_M2_7,
        MINIMAX_M3,
        XIAOMI_MIMO_V2,
        XIAOMI_MIMO_V2_PRO,
        XIAOMI_MIMO_V2_5,
        XIAOMI_MIMO_V2_5_PRO,
        XIAOMI_MIMO_TTS,
        XIAOMI_MIMO_ASR,
        QWEN_MT
    )

    private val DEEPSEEK_REASONING_MODELS = setOf(
        DEEPSEEK_R1_MODEL,
        DEEPSEEK_REASONER,
        DEEPSEEK_V4_FLASH,
        DEEPSEEK_V4_PRO,
        DEEPSEEK_V3_1,
        DEEPSEEK_V3_2
    )

    private val XIAOMI_MIMO_MODELS = setOf(
        XIAOMI_MIMO_V2,
        XIAOMI_MIMO_V2_PRO,
        XIAOMI_MIMO_V2_5,
        XIAOMI_MIMO_V2_5_PRO
    )

    fun capabilities(modelId: String): AiModelCapabilities {
        val matches = resolveModels(modelId)
        val abilities = resolveAbilities(matches)
        return AiModelCapabilities(
            type = when {
                AiModelAbility.ASR in abilities -> AiModelType.ASR
                AiModelAbility.TTS in abilities -> AiModelType.TTS
                else -> null
            },
            inputModalities = resolveModalities(matches) { it.inputModalities },
            outputModalities = resolveModalities(matches) { it.outputModalities },
            abilities = abilities,
            reasoning = resolveReasoningOptions(matches)
        )
    }

    fun enrich(model: AiModel): AiModel {
        val inferred = capabilities(model.id)
        val inferredType = inferred.type
        return model.copy(
            type = inferredType ?: model.safeType(),
            inputModalities = if (inferredType != null) {
                inferred.inputModalities
            } else {
                mergeModalities(model.safeInputModalities() + inferred.inputModalities)
            },
            outputModalities = if (inferredType != null) {
                inferred.outputModalities
            } else {
                mergeModalities(model.safeOutputModalities() + inferred.outputModalities)
            },
            abilities = mergeAbilities(model.safeAbilities() + inferred.abilities)
        )
    }

    private fun resolveModels(modelId: String): List<AiModelDefinition> {
        var bestScore: Int? = null
        val matches = mutableListOf<AiModelDefinition>()
        for (model in ALL_MODELS) {
            val score = model.matchScore(modelId) ?: continue
            when {
                bestScore == null || score > bestScore -> {
                    bestScore = score
                    matches.clear()
                    matches.add(model)
                }

                score == bestScore -> matches.add(model)
            }
        }
        return matches
    }

    private fun resolveModalities(
        models: List<AiModelDefinition>,
        selector: (AiModelDefinition) -> Set<AiModelModality>
    ): List<AiModelModality> {
        return mergeModalities(models.flatMap { selector(it) })
    }

    private fun resolveAbilities(models: List<AiModelDefinition>): List<AiModelAbility> {
        return mergeAbilities(models.flatMap { it.abilities })
    }

    private fun resolveReasoningOptions(models: List<AiModelDefinition>): AiModelReasoningOptions {
        return when {
            models.any { it in DEEPSEEK_REASONING_MODELS } -> AiModelReasoningOptions(
                thinkingParam = "thinking",
                effortParam = "reasoning_effort",
                reasoningOutputField = "reasoning_content"
            )

            models.any { it in XIAOMI_MIMO_MODELS } -> AiModelReasoningOptions(
                thinkingParam = "thinking",
                reasoningOutputField = "reasoning_content"
            )

            else -> AiModelReasoningOptions()
        }
    }

    private fun mergeModalities(modalities: List<AiModelModality>): List<AiModelModality> {
        val values = modalities.toSet()
        return listOf(
            AiModelModality.TEXT,
            AiModelModality.IMAGE,
            AiModelModality.AUDIO
        ).filter { it in values }
    }

    private fun mergeAbilities(abilities: List<AiModelAbility>): List<AiModelAbility> {
        val values = abilities.toSet()
        return listOf(
            AiModelAbility.ASR,
            AiModelAbility.TTS,
            AiModelAbility.TOOL,
            AiModelAbility.REASONING
        ).filter { it in values }
    }

    private fun AiModel.safeInputModalities(): List<AiModelModality> {
        return runCatching { inputModalities }.getOrNull().orEmpty()
    }

    private fun AiModel.safeOutputModalities(): List<AiModelModality> {
        return runCatching { outputModalities }.getOrNull().orEmpty()
    }

    private fun AiModel.safeAbilities(): List<AiModelAbility> {
        return runCatching { abilities }.getOrNull().orEmpty()
    }

    private fun AiModel.safeType(): AiModelType {
        return runCatching { type }.getOrNull() ?: AiModelType.CHAT
    }

    private fun AiModelDefinitionBuilder.visionInput() {
        input(AiModelModality.TEXT, AiModelModality.IMAGE)
    }

    private fun AiModelDefinitionBuilder.imageOutput() {
        output(AiModelModality.TEXT, AiModelModality.IMAGE)
    }

    private fun AiModelDefinitionBuilder.toolAbility() {
        ability(AiModelAbility.TOOL)
    }

    private fun AiModelDefinitionBuilder.asrAbility() {
        ability(AiModelAbility.ASR)
    }

    private fun AiModelDefinitionBuilder.ttsAbility() {
        ability(AiModelAbility.TTS)
    }

    private fun AiModelDefinitionBuilder.reasoningAbility() {
        ability(AiModelAbility.REASONING)
    }

    private fun AiModelDefinitionBuilder.toolReasoningAbility() {
        ability(AiModelAbility.TOOL, AiModelAbility.REASONING)
    }

    private fun AiModelDefinitionBuilder.excludeXiaomiSpeechModels() {
        notTokens("tts")
        notTokens("asr")
        notTokens("voiceclone")
        notTokens("voicedesign")
    }
}
