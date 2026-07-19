package io.github.zaraporsche911cloud.reportingassistant.ai.provider;

import io.github.zaraporsche911cloud.reportingassistant.ai.AiTextResult;
import io.github.zaraporsche911cloud.reportingassistant.exception.AiProviderException;
import tools.jackson.databind.JsonNode;

final class AiHttpSupport {

    private AiHttpSupport() {
    }

    static AiTextResult openAiResult(JsonNode root, String provider, String fallbackModel) {
        try {
            JsonNode choice = root.get("choices").get(0);
            String content = choice.get("message").get("content").stringValue();
            String model = root.get("model") == null ? fallbackModel : root.get("model").stringValue();
            JsonNode usage = root.get("usage");
            Integer input = usage == null || usage.get("prompt_tokens") == null ? null : usage.get("prompt_tokens").intValue();
            Integer output = usage == null || usage.get("completion_tokens") == null ? null : usage.get("completion_tokens").intValue();
            return new AiTextResult(content, provider, model, input, output);
        } catch (Exception exception) {
            throw new AiProviderException(provider + " returned an unexpected response shape", exception);
        }
    }

    static AiTextResult ollamaResult(JsonNode root, String model) {
        try {
            String content = root.get("message").get("content").stringValue();
            Integer input = root.get("prompt_eval_count") == null ? null : root.get("prompt_eval_count").intValue();
            Integer output = root.get("eval_count") == null ? null : root.get("eval_count").intValue();
            return new AiTextResult(content, "ollama", model, input, output);
        } catch (Exception exception) {
            throw new AiProviderException("Ollama returned an unexpected response shape", exception);
        }
    }
}
