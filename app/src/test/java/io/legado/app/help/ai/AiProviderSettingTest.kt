package io.legado.app.help.ai

import io.legado.app.utils.GSON
import org.junit.Assert.assertEquals
import org.junit.Test

class AiProviderSettingTest {

    @Test
    fun parseObfuscatedReleaseProviderCache() {
        val json = """
            {
              "a": "deepseek",
              "b": "OPENAI",
              "c": true,
              "d": true,
              "e": "DeepSeek",
              "f": "sk-test",
              "g": "https://api.deepseek.com",
              "h": "deepseek-v4-pro",
              "i": [
                {
                  "a": "deepseek-v4-pro",
                  "b": "DeepSeek V4 Pro",
                  "c": "deepseek"
                }
              ],
              "j": 60,
              "k": "/chat/completions",
              "l": "https://api.deepseek.com/models",
              "m": true,
              "n": true,
              "o": "thinking",
              "p": "reasoning_effort",
              "q": "reasoning_content"
            }
        """.trimIndent()

        val provider = GSON.fromJson(json, AiProviderSetting::class.java)

        assertEquals("deepseek", provider.id)
        assertEquals(AiProviderType.OPENAI, provider.type)
        assertEquals("sk-test", provider.apiKey)
        assertEquals("https://api.deepseek.com", provider.baseUrl)
        assertEquals("deepseek-v4-pro", provider.model)
        assertEquals("deepseek-v4-pro", provider.models.single().id)
        assertEquals("DeepSeek V4 Pro", provider.models.single().name)
        assertEquals("deepseek", provider.models.single().ownedBy)
        assertEquals("https://api.deepseek.com/models", provider.modelsUrl)
        assertEquals("thinking", provider.thinkingParam)
        assertEquals("reasoning_effort", provider.effortParam)
        assertEquals("reasoning_content", provider.reasoningOutputField)
    }
}
