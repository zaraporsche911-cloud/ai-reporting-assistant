package io.github.zaraporsche911cloud.reportingassistant.service;

import io.github.zaraporsche911cloud.reportingassistant.ai.AiOrchestrator;
import io.github.zaraporsche911cloud.reportingassistant.ai.AiTextResult;
import io.github.zaraporsche911cloud.reportingassistant.audit.AuditService;
import io.github.zaraporsche911cloud.reportingassistant.conversation.ConversationService;
import io.github.zaraporsche911cloud.reportingassistant.dto.assistant.AssistantDtos;
import io.github.zaraporsche911cloud.reportingassistant.entity.AppUser;
import io.github.zaraporsche911cloud.reportingassistant.entity.Conversation;
import io.github.zaraporsche911cloud.reportingassistant.entity.GeneratedReport;
import io.github.zaraporsche911cloud.reportingassistant.entity.Message;
import io.github.zaraporsche911cloud.reportingassistant.entity.MessageAuthor;
import io.github.zaraporsche911cloud.reportingassistant.entity.ReportExecutionStatus;
import io.github.zaraporsche911cloud.reportingassistant.mapper.ReportMapper;
import io.github.zaraporsche911cloud.reportingassistant.report.engine.ReportingEngine;
import io.github.zaraporsche911cloud.reportingassistant.report.model.IntentEnvelope;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import io.github.zaraporsche911cloud.reportingassistant.repository.GeneratedReportRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class AssistantService {

    private final CurrentUserService currentUserService;
    private final ConversationService conversationService;
    private final GeneratedReportRepository reports;
    private final AiOrchestrator ai;
    private final ReportingEngine engine;
    private final ReportMapper reportMapper;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public AssistantService(
            CurrentUserService currentUserService,
            ConversationService conversationService,
            GeneratedReportRepository reports,
            AiOrchestrator ai,
            ReportingEngine engine,
            ReportMapper reportMapper,
            ObjectMapper objectMapper,
            AuditService auditService
    ) {
        this.currentUserService = currentUserService;
        this.conversationService = conversationService;
        this.reports = reports;
        this.ai = ai;
        this.engine = engine;
        this.reportMapper = reportMapper;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','OPERATIONS_MANAGER','ANALYST')")
    public AssistantDtos.Response query(AssistantDtos.QueryRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        Conversation conversation = request.conversationId() == null
                ? conversationService.create(user, ai.conversationTitle(request.question()))
                : conversationService.findOwned(request.conversationId(), user);
        conversationService.addMessage(conversation, MessageAuthor.USER, request.question(), null);

        String previousIntent = reports.findTopByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .map(GeneratedReport::getIntentJson).orElse(null);
        AiOrchestrator.ExtractedIntent extracted = ai.extractIntent(request.question(), previousIntent);
        IntentEnvelope envelope = extracted.envelope();
        if (envelope.clarificationRequired()) {
            Message message = conversationService.addMessage(
                    conversation, MessageAuthor.ASSISTANT, envelope.clarificationQuestion(), null);
            return new AssistantDtos.Response(
                    conversationService.response(conversation), conversationService.messageResponse(message), null, true, List.of());
        }

        long started = System.nanoTime();
        try {
            ReportingEngine.ExecutedReport executed = engine.execute(envelope.intent(), user.getRole());
            AiTextResult summaryMetadata;
            String summary;
            try {
                summaryMetadata = ai.summarize(request.question(), executed.result());
                summary = summaryMetadata.content();
            } catch (RuntimeException exception) {
                summaryMetadata = extracted.metadata();
                summary = "The structured report was generated successfully, but the AI explanation is temporarily unavailable. "
                        + "The KPIs and table remain valid because they were calculated by the reporting engine.";
            }
            long durationMs = elapsedMillis(started);
            GeneratedReport report = reports.save(new GeneratedReport(
                    user, conversation, request.question(), executed.intent().reportType(),
                    objectMapper.writeValueAsString(executed.intent()), objectMapper.writeValueAsString(executed.result()), summary,
                    ReportExecutionStatus.SUCCEEDED, durationMs, summaryMetadata.provider(), summaryMetadata.model(), null));
            Message message = conversationService.addMessage(conversation, MessageAuthor.ASSISTANT, summary, report.getId());
            auditService.record(user.getEmail(), "REPORT_GENERATED", "GENERATED_REPORT", report.getId(),
                    "Type: " + report.getReportType() + ", durationMs: " + durationMs);
            return new AssistantDtos.Response(
                    conversationService.response(conversation), conversationService.messageResponse(message), reportMapper.toResponse(report),
                    false, ai.suggestedQuestions(report.getReportType()));
        } catch (RuntimeException exception) {
            persistFailure(user, conversation, request.question(), envelope.intent(), extracted.metadata(), started, exception);
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to serialize report execution", exception);
        }
    }

    private void persistFailure(
            AppUser user,
            Conversation conversation,
            String question,
            ReportIntent intent,
            AiTextResult metadata,
            long started,
            RuntimeException exception
    ) {
        try {
            String safeError = exception.getMessage() == null ? "Report execution failed" : exception.getMessage();
            if (safeError.length() > 500) safeError = safeError.substring(0, 500);
            GeneratedReport failed = reports.save(new GeneratedReport(
                    user, conversation, question, intent.reportType(), objectMapper.writeValueAsString(intent), null, null,
                    ReportExecutionStatus.FAILED, elapsedMillis(started), metadata.provider(), metadata.model(), safeError));
            auditService.record(user.getEmail(), "REPORT_FAILED", "GENERATED_REPORT", failed.getId(), "Type: " + intent.reportType());
        } catch (Exception ignored) {
            // Preserve the original exception; failure auditing must never obscure the user-facing cause.
        }
    }

    private long elapsedMillis(long started) {
        return Math.max(1, (System.nanoTime() - started) / 1_000_000);
    }
}
