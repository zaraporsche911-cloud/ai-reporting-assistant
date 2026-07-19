package io.github.zaraporsche911cloud.reportingassistant.ai.provider;

import io.github.zaraporsche911cloud.reportingassistant.ai.AiCompletionRequest;
import io.github.zaraporsche911cloud.reportingassistant.ai.AiProvider;
import io.github.zaraporsche911cloud.reportingassistant.ai.AiTextResult;
import io.github.zaraporsche911cloud.reportingassistant.config.AiProperties;
import io.github.zaraporsche911cloud.reportingassistant.exception.AiProviderException;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpClient;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OllamaProvider implements AiProvider {

    private final AiProperties properties;
    private final RestClient client;

    public OllamaProvider(AiProperties properties) {
        this.properties = properties;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(properties.timeout()).build());
        factory.setReadTimeout(properties.timeout());
        this.client = RestClient.builder().baseUrl(properties.ollamaBaseUrl()).requestFactory(factory).build();
    }

    @Override
    public String id() { return "ollama"; }

    @Override
    public AiTextResult complete(AiCompletionRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.model());
        body.put("stream", false);
        body.put("messages", List.of(
                Map.of("role", "system", "content", request.systemPrompt()),
                Map.of("role", "user", "content", request.userPrompt())));
        if (request.jsonResponse()) body.put("format", "json");
        try {
            JsonNode response = client.post().uri("/api/chat").body(body).retrieve().body(JsonNode.class);
            if (response == null) throw new AiProviderException("Ollama returned an empty response");
            return AiHttpSupport.ollamaResult(response, properties.model());
        } catch (RestClientException exception) {
            throw new AiProviderException("Ollama request failed", exception);
        }
    }

    @Override
    public ProviderHealth health() {
        try {
            JsonNode response = client.get().uri("/api/tags").retrieve().body(JsonNode.class);
            if (response == null || response.get("models") == null || !response.get("models").isArray()) {
                return new ProviderHealth(false, "Ollama responded without a valid model catalogue", properties.model());
            }
            boolean modelPresent = false;
            for (JsonNode model : response.get("models")) {
                String name = model.get("name") == null ? "" : model.get("name").stringValue();
                if (name.equals(properties.model()) || name.startsWith(properties.model() + ":")) {
                    modelPresent = true;
                    break;
                }
            }
            return modelPresent
                    ? new ProviderHealth(true, "Ollama is reachable and the configured model is available", properties.model())
                    : new ProviderHealth(false, "Ollama is reachable, but the configured model has not been pulled", properties.model());
        } catch (RestClientException exception) {
            return new ProviderHealth(false, "Ollama is not reachable at the configured endpoint", properties.model());
        }
    }
}
