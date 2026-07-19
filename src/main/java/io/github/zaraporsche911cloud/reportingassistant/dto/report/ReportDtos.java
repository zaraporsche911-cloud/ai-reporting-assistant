package io.github.zaraporsche911cloud.reportingassistant.dto.report;

import io.github.zaraporsche911cloud.reportingassistant.entity.ReportExecutionStatus;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportResult;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class ReportDtos {

    private ReportDtos() {
    }

    public record GeneratedResponse(
            Long id,
            Long conversationId,
            String question,
            ReportType reportType,
            ReportIntent intent,
            ReportResult result,
            String summary,
            ReportExecutionStatus status,
            long executionTimeMs,
            String aiProvider,
            String aiModel,
            String errorDetail,
            Instant createdAt
    ) {
    }

    public record SaveRequest(
            @NotBlank @Size(max = 160) String title,
            @Size(max = 1000) String description,
            @Size(max = 500) String tags
    ) {
    }

    public record SavedUpdateRequest(
            @NotBlank @Size(max = 160) String title,
            @Size(max = 1000) String description,
            @Size(max = 500) String tags,
            boolean favorite,
            boolean pinned,
            boolean sharedInternally
    ) {
    }

    public record SavedResponse(
            Long id,
            String title,
            String description,
            String tags,
            boolean favorite,
            boolean pinned,
            boolean sharedInternally,
            GeneratedResponse report,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record CatalogueResponse(
            ReportType type,
            String displayName,
            String description,
            List<String> requiredCapabilities,
            int maximumResultSize,
            String defaultVisualization,
            boolean available,
            String availabilityMessage
    ) {
    }
}
