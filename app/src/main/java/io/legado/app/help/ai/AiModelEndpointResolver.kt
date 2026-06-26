package io.legado.app.help.ai

object AiModelEndpointResolver {

    private val knownCompatSuffixes = listOf(
        "/api/claudecode",
        "/api/anthropic",
        "/apps/anthropic",
        "/api/coding",
        "/claudecode",
        "/anthropic",
        "/step_plan",
        "/coding",
        "/claude"
    )

    fun candidates(setting: AiProviderSetting): List<String> {
        if (setting.useCustomModelsUrl) {
            return runCatching {
                listOf(buildAiApiEndpoint(setting.baseUrl, setting.modelsUrl))
            }.getOrDefault(emptyList())
        }
        return candidates(setting.baseUrl, setting.type)
    }

    fun candidates(baseUrl: String, type: AiProviderType = AiProviderType.OPENAI): List<String> {
        val base = baseUrl.trimEndSlash()
        if (base.isBlank()) {
            return emptyList()
        }

        val candidates = linkedSetOf<String>()
        when {
            type == AiProviderType.CLAUDE -> {
                candidates += "$base/models"
            }
            base.contains("api.deepseek.com", ignoreCase = true) -> {
                candidates += "https://api.deepseek.com/models"
            }
            base.endsWithVersionSegment() -> {
                candidates += "$base/models"
                if (!base.endsWith("/v1", ignoreCase = true)) {
                    candidates += "$base/v1/models"
                }
            }
            else -> {
                candidates += "$base/v1/models"
            }
        }

        knownCompatSuffixes.forEach { suffix ->
            if (base.endsWith(suffix, ignoreCase = true)) {
                val root = base.dropLast(suffix.length).trimEndSlash()
                if (root.isNotBlank()) {
                    candidates += "$root/v1/models"
                    candidates += "$root/models"
                }
            }
        }

        return candidates.toList()
    }

    private fun String.endsWithVersionSegment(): Boolean {
        return Regex(""".*/v\d+$""", RegexOption.IGNORE_CASE).matches(this)
    }

}
