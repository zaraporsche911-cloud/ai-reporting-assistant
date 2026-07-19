package io.github.zaraporsche911cloud.reportingassistant.ai.provider;

import io.github.zaraporsche911cloud.reportingassistant.ai.AiCompletionRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class MockAiProviderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MockAiProvider provider = new MockAiProvider(objectMapper);

    @Test
    void prioritizesFuelSubjectOverGenericComparisonLanguage() throws Exception {
        String response = provider.complete(intentRequest("Compare fuel consumption by vehicle this month")).content();
        JsonNode json = objectMapper.readTree(response);
        assertThat(json.get("intent").get("reportType").stringValue()).isEqualTo("FUEL_CONSUMPTION");
        assertThat(json.get("intent").get("groupBy").stringValue()).isEqualTo("VEHICLE");
    }

    @Test
    void returnsClarificationForAmbiguousRequest() throws Exception {
        String response = provider.complete(intentRequest("Show me anything about stuff")).content();
        JsonNode json = objectMapper.readTree(response);
        assertThat(json.get("clarificationRequired").booleanValue()).isTrue();
        assertThat(json.get("clarificationQuestion").stringValue()).contains("fleet area");
    }

    private AiCompletionRequest intentRequest(String question) {
        return new AiCompletionRequest(AiCompletionRequest.Task.INTENT, "system",
                "USER_QUESTION:\n" + question + "\nEND_USER_QUESTION", true);
    }
}
