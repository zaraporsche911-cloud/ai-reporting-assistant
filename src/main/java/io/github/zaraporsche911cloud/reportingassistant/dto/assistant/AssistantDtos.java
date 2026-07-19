package io.github.zaraporsche911cloud.reportingassistant.dto.assistant;

import io.github.zaraporsche911cloud.reportingassistant.dto.conversation.ConversationDtos;
import io.github.zaraporsche911cloud.reportingassistant.dto.report.ReportDtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class AssistantDtos {

    private AssistantDtos() {
    }

    public record QueryRequest(Long conversationId, @NotBlank @Size(max = 2000) String question) {
    }

    public record Response(
            ConversationDtos.Response conversation,
            ConversationDtos.MessageResponse message,
            ReportDtos.GeneratedResponse report,
            boolean clarificationRequired,
            List<String> suggestedQuestions
    ) {
    }
}
