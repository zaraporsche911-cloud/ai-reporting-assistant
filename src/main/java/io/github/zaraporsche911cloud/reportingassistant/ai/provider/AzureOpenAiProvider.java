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
public class AzureOpenAiProvider implements AiProvider {

    private final AiProperties properties;
    private final RestClient client;

    public AzureOpenAiProvider(AiProperties properties) {
        this.properties = properties;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(properties.timeout()).build());
        factory.setReadTimeout(properties.timeout());
        String endpoint = properties.azureEndpoint() == null || properties.azureEndpoint().isBlank()
                ? "http://localhost" : properties.azureEndpoint();
        this.client = RestClient.builder().baseUrl(endpoint).requestFactory(factory).build();
    }

    @Override
    public String id() { return "azure"; }

    @Override
    public AiTextResult complete(AiCompletionRequest request) {
        if (!configured()) throw new AiProviderException("Azure OpenAI is not configured");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("temperature", 0.1);
        body.put("messages", List.of(
                Map.of("role", "system", "content", request.systemPrompt()),
                Map.of("role", "user", "content", request.userPrompt())));
        if (request.jsonResponse()) body.put("response_format", Map.of("type", "json_object"));
        String path = "/openai/deployments/" + properties.model() + "/chat/completions?api-version=" + properties.azureApiVersion();
        try {
            JsonNode response = client.post().uri(path).header("api-key", properties.azureApiKey())
                    .body(body).retrieve().body(JsonNode.class);
            if (response == null) throw new AiProviderException("Azure OpenAI returned an empty response");
            return AiHttpSupport.openAiResult(response, id(), properties.model());
        } catch (RestClientException exception) {
            throw new AiProviderException("Azure OpenAI request failed", exception);
        }
    }

    @Override
    public ProviderHealth health() {
        return configured()
                ? new ProviderHealth(true, "Azure OpenAI credentials are configured", properties.model())
                : new ProviderHealth(false, "Set Azure endpoint and API key before selecting Azure OpenAI", properties.model());
    }

    private boolean configured() {
        return properties.azureEndpoint() != null && !properties.azureEndpoint().isBlank()
                && properties.azureApiKey() != null && !properties.azureApiKey().isBlank();
    }
}
