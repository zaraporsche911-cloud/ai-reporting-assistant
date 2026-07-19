package io.github.zaraporsche911cloud.reportingassistant.ai;

public record AiCompletionRequest(
        Task task,
        String systemPrompt,
        String userPrompt,
        boolean jsonResponse
) {
    public enum Task {
        INTENT,
        SUMMARY,
        SUGGESTIONS,
        TITLE
    }
}
