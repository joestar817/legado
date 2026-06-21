package io.legado.app.help.ai

import io.legado.app.constant.PreferKey
import io.legado.app.utils.getPrefBoolean
import io.legado.app.utils.getPrefInt
import io.legado.app.utils.getPrefString
import io.legado.app.utils.putPrefBoolean
import io.legado.app.utils.putPrefInt
import splitties.init.appCtx

object AiConfig {

    const val DEFAULT_PURIFY_PARAGRAPH_LIMIT = 4000
    const val MIN_PURIFY_PARAGRAPH_LIMIT = 500
    const val MAX_PURIFY_PARAGRAPH_LIMIT = 20000
    const val DEFAULT_PURIFY_CHAPTER_SEGMENT_LIMIT = 10000
    const val MIN_PURIFY_CHAPTER_SEGMENT_LIMIT = 1000
    const val MAX_PURIFY_CHAPTER_SEGMENT_LIMIT = 50000

    val temperature: Float?
        get() = appCtx.getPrefString(PreferKey.aiTemperature, "0.2")
            ?.toFloatOrNull()
            ?.coerceIn(0f, 2f)

    var purifyAutoApply: Boolean
        get() = appCtx.getPrefBoolean(PreferKey.aiPurifyAutoApply, false)
        set(value) {
            appCtx.putPrefBoolean(PreferKey.aiPurifyAutoApply, value)
        }

    var purifyExceptionIntercept: Boolean
        get() = appCtx.getPrefBoolean(PreferKey.aiPurifyExceptionIntercept, true)
        set(value) {
            appCtx.putPrefBoolean(PreferKey.aiPurifyExceptionIntercept, value)
        }

    var purifyParagraphLimit: Int
        get() = appCtx.getPrefInt(
            PreferKey.aiPurifyParagraphLimit,
            DEFAULT_PURIFY_PARAGRAPH_LIMIT
        ).coerceIn(MIN_PURIFY_PARAGRAPH_LIMIT, MAX_PURIFY_PARAGRAPH_LIMIT)
        set(value) {
            appCtx.putPrefInt(
                PreferKey.aiPurifyParagraphLimit,
                value.coerceIn(MIN_PURIFY_PARAGRAPH_LIMIT, MAX_PURIFY_PARAGRAPH_LIMIT)
            )
        }

    var purifyChapterAutoApply: Boolean
        get() = appCtx.getPrefBoolean(PreferKey.aiPurifyChapterAutoApply, false)
        set(value) {
            appCtx.putPrefBoolean(PreferKey.aiPurifyChapterAutoApply, value)
        }

    var purifyChapterExceptionIntercept: Boolean
        get() = appCtx.getPrefBoolean(PreferKey.aiPurifyChapterExceptionIntercept, true)
        set(value) {
            appCtx.putPrefBoolean(PreferKey.aiPurifyChapterExceptionIntercept, value)
        }

    var purifyChapterSegmentLimit: Int
        get() = appCtx.getPrefInt(
            PreferKey.aiPurifyChapterSegmentLimit,
            DEFAULT_PURIFY_CHAPTER_SEGMENT_LIMIT
        ).coerceIn(MIN_PURIFY_CHAPTER_SEGMENT_LIMIT, MAX_PURIFY_CHAPTER_SEGMENT_LIMIT)
        set(value) {
            appCtx.putPrefInt(
                PreferKey.aiPurifyChapterSegmentLimit,
                value.coerceIn(MIN_PURIFY_CHAPTER_SEGMENT_LIMIT, MAX_PURIFY_CHAPTER_SEGMENT_LIMIT)
            )
        }

    fun currentSetting(): AiProviderSetting {
        return AiProviderStore.activeProvider()
    }

    fun savedModels(): List<AiModel> {
        return currentSetting().models
    }

    fun saveModels(models: List<AiModel>) {
        val setting = currentSetting()
        AiProviderStore.saveProvider(setting.copy(models = models))
    }

    fun clearModels() {
        val setting = currentSetting()
        AiProviderStore.saveProvider(setting.copy(models = emptyList()))
    }
}
