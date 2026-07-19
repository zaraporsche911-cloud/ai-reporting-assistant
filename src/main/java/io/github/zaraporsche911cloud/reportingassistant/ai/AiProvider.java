package io.github.zaraporsche911cloud.reportingassistant.ai;

public interface AiProvider {

    String id();

    AiTextResult complete(AiCompletionRequest request);

    ProviderHealth health();

    record ProviderHealth(boolean available, String message, String model) {
    }
}
