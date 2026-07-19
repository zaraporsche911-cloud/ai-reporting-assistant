package io.github.zaraporsche911cloud.reportingassistant.dto.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class ConversationDtos {

    private ConversationDtos() {
    }

    public record RenameRequest(@NotBlank @Size(max = 160) String title) {
    }

    public record Response(Long id, String title, Instant createdAt, Instant updatedAt) {
    }

    public record MessageResponse(
            Long id,
            String author,
            String content,
            Long generatedReportId,
            Instant createdAt
    ) {
    }

    public record Details(Response conversation, List<MessageResponse> messages) {
    }
}
