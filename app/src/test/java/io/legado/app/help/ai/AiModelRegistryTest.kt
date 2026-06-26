package io.legado.app.help.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiModelRegistryTest {

    @Test
    fun inferDeepSeekReasoningFromModelId() {
        val capabilities = AiModelRegistry.capabilities("deepseek-v4-pro")

        assertTrue(AiModelAbility.REASONING in capabilities.abilities)
        assertTrue(AiModelAbility.TOOL in capabilities.abilities)
        assertEquals("thinking", capabilities.reasoning.thinkingParam)
        assertEquals("reasoning_effort", capabilities.reasoning.effortParam)
        assertEquals("reasoning_content", capabilities.reasoning.reasoningOutputField)
    }

    @Test
    fun inferDeepSeekFlashAsVisionReasoningModel() {
        val model = AiModelRegistry.enrich(AiModel(id = "deepseek-v4-flash"))

        assertTrue(AiModelModality.IMAGE in model.inputModalities)
        assertTrue(AiModelAbility.REASONING in model.abilities)
        assertTrue(AiModelAbility.TOOL in model.abilities)
    }

    @Test
    fun inferMimoReasoningWithoutEffortFromModelId() {
        val capabilities = AiModelRegistry.capabilities("mimo-v2.5-pro")

        assertTrue(AiModelAbility.REASONING in capabilities.abilities)
        assertTrue(AiModelAbility.TOOL in capabilities.abilities)
        assertEquals("thinking", capabilities.reasoning.thinkingParam)
        assertEquals("", capabilities.reasoning.effortParam)
    }

    @Test
    fun excludeMimoSpeechModelsFromChatCapabilities() {
        val ttsModel = AiModelRegistry.enrich(AiModel(id = "mimo-v2.5-tts"))
        val voiceCloneModel = AiModelRegistry.enrich(AiModel(id = "mimo-v2.5-tts-voiceclone"))
        val asrModel = AiModelRegistry.enrich(AiModel(id = "mimo-v2.5-asr"))

        listOf(ttsModel, voiceCloneModel).forEach { model ->
            assertEquals(AiModelType.TTS, model.type)
            assertEquals(listOf(AiModelModality.TEXT), model.inputModalities)
            assertEquals(listOf(AiModelModality.AUDIO), model.outputModalities)
            assertFalse(AiModelModality.IMAGE in model.inputModalities)
            assertTrue(AiModelAbility.TTS in model.abilities)
            assertFalse(AiModelAbility.TOOL in model.abilities)
            assertFalse(AiModelAbility.REASONING in model.abilities)
        }
        assertEquals(AiModelType.ASR, asrModel.type)
        assertEquals(listOf(AiModelModality.AUDIO), asrModel.inputModalities)
        assertEquals(listOf(AiModelModality.TEXT), asrModel.outputModalities)
        assertTrue(AiModelAbility.ASR in asrModel.abilities)
        assertFalse(AiModelAbility.TTS in asrModel.abilities)
        assertFalse(AiModelAbility.TOOL in asrModel.abilities)
        assertFalse(AiModelAbility.REASONING in asrModel.abilities)
    }

    @Test
    fun inferGeminiVisionAndReasoningFromModelId() {
        val model = AiModelRegistry.enrich(AiModel(id = "gemini-2.5-pro"))

        assertTrue(AiModelModality.IMAGE in model.inputModalities)
        assertTrue(AiModelAbility.REASONING in model.abilities)
        assertTrue(AiModelAbility.TOOL in model.abilities)
    }

    @Test
    fun inferClaudeVisionAndReasoningFromModelIdOnly() {
        val model = AiModelRegistry.enrich(AiModel(id = "claude-sonnet-4-5"))

        assertTrue(AiModelModality.IMAGE in model.inputModalities)
        assertTrue(AiModelAbility.REASONING in model.abilities)
        assertTrue(AiModelAbility.TOOL in model.abilities)
    }

    @Test
    fun inferGeminiImageOutputWithoutToolFromBestModelMatch() {
        val model = AiModelRegistry.enrich(AiModel(id = "gemini-2.5-flash-image"))

        assertTrue(AiModelModality.IMAGE in model.inputModalities)
        assertTrue(AiModelModality.IMAGE in model.outputModalities)
        assertFalse(AiModelAbility.TOOL in model.abilities)
        assertFalse(AiModelAbility.REASONING in model.abilities)
    }

    @Test
    fun excludeGpt5ChatFromGenericGpt5ReasoningRule() {
        val model = AiModelRegistry.enrich(AiModel(id = "gpt-5-chat-latest"))

        assertFalse(AiModelAbility.TOOL in model.abilities)
        assertFalse(AiModelAbility.REASONING in model.abilities)
    }

    @Test
    fun inferDeepSeekChatAsToolOnly() {
        val model = AiModelRegistry.enrich(AiModel(id = "deepseek-chat"))

        assertTrue(AiModelAbility.TOOL in model.abilities)
        assertFalse(AiModelAbility.REASONING in model.abilities)
    }

    @Test
    fun inferOcrAsVisionInputAndTextOutput() {
        val model = AiModelRegistry.enrich(AiModel(id = "deepseek-ai/DeepSeek-OCR"))

        assertEquals(AiModelType.CHAT, model.type)
        assertTrue(AiModelModality.TEXT in model.inputModalities)
        assertTrue(AiModelModality.IMAGE in model.inputModalities)
        assertEquals(listOf(AiModelModality.TEXT), model.outputModalities)
        assertFalse(AiModelAbility.TOOL in model.abilities)
        assertFalse(AiModelAbility.REASONING in model.abilities)
    }

    @Test
    fun unknownModelDoesNotPretendTextCapability() {
        val model = AiModelRegistry.enrich(AiModel(id = "vendor/unknown-model"))

        assertEquals(emptyList<AiModelModality>(), model.inputModalities)
        assertEquals(emptyList<AiModelModality>(), model.outputModalities)
        assertEquals(emptyList<AiModelAbility>(), model.abilities)
    }
}
