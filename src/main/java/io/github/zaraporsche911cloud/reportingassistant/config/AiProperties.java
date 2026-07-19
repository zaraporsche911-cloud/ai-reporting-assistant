package io.github.zaraporsche911cloud.reportingassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        String provider,
        String model,
        String apiKey,
        String baseUrl,
        String ollamaBaseUrl,
        String azureEndpoint,
        String azureApiKey,
        String azureApiVersion,
        Duration timeout
) {
    public AiProperties {
        provider = provider == null || provider.isBlank() ? "mock" : provider.trim().toLowerCase();
        model = model == null || model.isBlank() ? "fleetops-rules-v1" : model.trim();
        timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
    }
}
