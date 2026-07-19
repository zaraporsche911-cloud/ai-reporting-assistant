package io.github.zaraporsche911cloud.reportingassistant.ai.parser;

import io.github.zaraporsche911cloud.reportingassistant.exception.AiProviderException;
import io.github.zaraporsche911cloud.reportingassistant.report.model.IntentEnvelope;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

@Component
public class ReportIntentParser {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ReportIntentParser(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public IntentEnvelope parse(String raw) {
        if (raw == null || raw.isBlank()) throw new AiProviderException("AI provider returned an empty intent response");
        String json = stripCodeFence(raw.trim());
        try {
            IntentEnvelope envelope = objectMapper.readValue(json, IntentEnvelope.class);
            if (envelope.clarificationRequired()) {
                if (envelope.clarificationQuestion() == null || envelope.clarificationQuestion().isBlank()) {
                    throw new AiProviderException("AI requested clarification without a clarification question");
                }
                return envelope;
            }
            if (envelope.intent() == null) throw new AiProviderException("AI response did not contain a report intent");
            Set<ConstraintViolation<io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent>> violations =
                    validator.validate(envelope.intent());
            if (!violations.isEmpty()) {
                throw new AiProviderException("AI response failed schema validation: " + violations.iterator().next().getMessage());
            }
            return envelope;
        } catch (AiProviderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AiProviderException("AI provider returned malformed structured intent", exception);
        }
    }

    private String stripCodeFence(String value) {
        if (!value.startsWith("```")) return value;
        int firstLine = value.indexOf('\n');
        int lastFence = value.lastIndexOf("```");
        if (firstLine < 0 || lastFence <= firstLine) return value;
        return value.substring(firstLine + 1, lastFence).trim();
    }
}
