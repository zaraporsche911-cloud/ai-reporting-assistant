package io.github.zaraporsche911cloud.reportingassistant.entity;

import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "generated_reports")
public class GeneratedReport extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Column(nullable = false, length = 2000)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 48)
    private ReportType reportType;

    @Column(name = "intent_json", nullable = false, columnDefinition = "TEXT")
    private String intentJson;

    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReportExecutionStatus status;

    @Column(name = "execution_time_ms", nullable = false)
    private long executionTimeMs;

    @Column(name = "ai_provider", length = 32)
    private String aiProvider;

    @Column(name = "ai_model", length = 120)
    private String aiModel;

    @Column(name = "error_detail", length = 500)
    private String errorDetail;

    protected GeneratedReport() {
    }

    public GeneratedReport(
            AppUser user,
            Conversation conversation,
            String question,
            ReportType reportType,
            String intentJson,
            String resultJson,
            String summary,
            ReportExecutionStatus status,
            long executionTimeMs,
            String aiProvider,
            String aiModel,
            String errorDetail
    ) {
        this.user = user;
        this.conversation = conversation;
        this.question = DomainText.require(question, "question", 2000);
        this.reportType = reportType;
        this.intentJson = intentJson;
        this.resultJson = resultJson;
        this.summary = summary;
        this.status = status;
        this.executionTimeMs = executionTimeMs;
        this.aiProvider = DomainText.optional(aiProvider, 32);
        this.aiModel = DomainText.optional(aiModel, 120);
        this.errorDetail = DomainText.optional(errorDetail, 500);
    }

    public void replaceSummary(String summary, String provider, String model) {
        this.summary = DomainText.require(summary, "summary", 10_000);
        this.aiProvider = DomainText.optional(provider, 32);
        this.aiModel = DomainText.optional(model, 120);
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public Conversation getConversation() { return conversation; }
    public String getQuestion() { return question; }
    public ReportType getReportType() { return reportType; }
    public String getIntentJson() { return intentJson; }
    public String getResultJson() { return resultJson; }
    public String getSummary() { return summary; }
    public ReportExecutionStatus getStatus() { return status; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public String getAiProvider() { return aiProvider; }
    public String getAiModel() { return aiModel; }
    public String getErrorDetail() { return errorDetail; }
}
