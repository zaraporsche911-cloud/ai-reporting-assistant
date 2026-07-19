package io.github.zaraporsche911cloud.reportingassistant.ai.provider;

import io.github.zaraporsche911cloud.reportingassistant.ai.AiCompletionRequest;
import io.github.zaraporsche911cloud.reportingassistant.ai.AiProvider;
import io.github.zaraporsche911cloud.reportingassistant.ai.AiTextResult;
import io.github.zaraporsche911cloud.reportingassistant.config.AiProperties;
import io.github.zaraporsche911cloud.reportingassistant.exception.AiProviderException;
import org.springframework.http.HttpHeaders;
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
public class OpenAiProvider implements AiProvider {

    private final AiProperties properties;
    private final RestClient client;

    public OpenAiProvider(AiProperties properties) {
        this.properties = properties;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(properties.timeout()).build());
        factory.setReadTimeout(properties.timeout());
        this.client = RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(factory).build();
    }

    @Override
    public String id() { return "openai"; }

    @Override
    public AiTextResult complete(AiCompletionRequest request) {
        if (!configured()) throw new AiProviderException("OpenAI is not configured");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.model());
        body.put("temperature", 0.1);
        body.put("messages", List.of(
                Map.of("role", "system", "content", request.systemPrompt()),
                Map.of("role", "user", "content", request.userPrompt())));
        if (request.jsonResponse()) body.put("response_format", Map.of("type", "json_object"));
        try {
            JsonNode response = client.post().uri("/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .body(body).retrieve().body(JsonNode.class);
            if (response == null) throw new AiProviderException("OpenAI returned an empty response");
            return AiHttpSupport.openAiResult(response, id(), properties.model());
        } catch (RestClientException exception) {
            throw new AiProviderException("OpenAI request failed", exception);
        }
    }

    @Override
    public ProviderHealth health() {
        return configured()
                ? new ProviderHealth(true, "OpenAI credentials are configured", properties.model())
                : new ProviderHealth(false, "Set AI_API_KEY before selecting OpenAI", properties.model());
    }

    private boolean configured() {
        return properties.apiKey() != null && !properties.apiKey().isBlank();
    }
}
