package io.github.zaraporsche911cloud.reportingassistant.mapper;

import io.github.zaraporsche911cloud.reportingassistant.dto.report.ReportDtos;
import io.github.zaraporsche911cloud.reportingassistant.entity.GeneratedReport;
import io.github.zaraporsche911cloud.reportingassistant.entity.SavedReport;
import io.github.zaraporsche911cloud.reportingassistant.exception.AiProviderException;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class ReportMapper {

    private final ObjectMapper objectMapper;

    public ReportMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReportDtos.GeneratedResponse toResponse(GeneratedReport report) {
        try {
            ReportIntent intent = objectMapper.readValue(report.getIntentJson(), ReportIntent.class);
            ReportResult result = report.getResultJson() == null ? null : objectMapper.readValue(report.getResultJson(), ReportResult.class);
            return new ReportDtos.GeneratedResponse(
                    report.getId(), report.getConversation() == null ? null : report.getConversation().getId(), report.getQuestion(),
                    report.getReportType(), intent, result, report.getSummary(), report.getStatus(), report.getExecutionTimeMs(),
                    report.getAiProvider(), report.getAiModel(), report.getErrorDetail(), report.getCreatedAt());
        } catch (Exception exception) {
            throw new AiProviderException("Stored report data could not be read", exception);
        }
    }

    public ReportDtos.SavedResponse toResponse(SavedReport saved) {
        return new ReportDtos.SavedResponse(
                saved.getId(), saved.getTitle(), saved.getDescription(), saved.getTags(), saved.isFavorite(), saved.isPinned(),
                saved.isSharedInternally(), toResponse(saved.getGeneratedReport()), saved.getCreatedAt(), saved.getUpdatedAt());
    }
}
