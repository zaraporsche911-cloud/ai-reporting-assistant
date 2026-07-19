package io.github.zaraporsche911cloud.reportingassistant.ai.prompt;

import io.github.zaraporsche911cloud.reportingassistant.exception.UnsafePromptException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class PromptGuard {

    private static final int MAXIMUM_LENGTH = 2_000;
    private static final List<String> BLOCKED_PHRASES = List.of(
            "ignore previous instructions",
            "reveal your system prompt",
            "show hidden prompt",
            "print api key",
            "reveal api key",
            "execute sql",
            "drop table",
            "read the database directly"
    );

    public String validate(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("question must not be blank");
        }
        String normalized = question.trim();
        if (normalized.length() > MAXIMUM_LENGTH) {
            throw new UnsafePromptException("Question must not exceed " + MAXIMUM_LENGTH + " characters");
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (BLOCKED_PHRASES.stream().anyMatch(lower::contains)) {
            throw new UnsafePromptException("The question contains instructions outside the reporting assistant's permitted scope");
        }
        return normalized;
    }
}
