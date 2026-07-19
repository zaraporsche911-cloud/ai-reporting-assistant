package io.github.zaraporsche911cloud.reportingassistant.ai;

import io.github.zaraporsche911cloud.reportingassistant.ai.parser.ReportIntentParser;
import io.github.zaraporsche911cloud.reportingassistant.ai.prompt.PromptGuard;
import io.github.zaraporsche911cloud.reportingassistant.ai.prompt.PromptTemplateService;
import io.github.zaraporsche911cloud.reportingassistant.exception.AiProviderException;
import io.github.zaraporsche911cloud.reportingassistant.report.model.IntentEnvelope;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportResult;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class AiOrchestrator {

    private final AiProviderRouter providers;
    private final PromptTemplateService templates;
    private final PromptGuard promptGuard;
    private final ReportIntentParser intentParser;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AiOrchestrator(
            AiProviderRouter providers,
            PromptTemplateService templates,
            PromptGuard promptGuard,
            ReportIntentParser intentParser,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.providers = providers;
        this.templates = templates;
        this.promptGuard = promptGuard;
        this.intentParser = intentParser;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public ExtractedIntent extractIntent(String question, String previousIntentJson) {
        String safeQuestion = promptGuard.validate(question);
        String system = templates.content(PromptTemplateService.INTENT) + "\nSupported report types: "
                + Arrays.toString(ReportType.values()) + ".\n"
                + "Return an object with clarificationRequired, clarificationQuestion, and intent. "
                + "The intent fields are reportType, dateRange, comparisonDateRange, filters, groupBy, metrics, sort, limit, and visualization.";
        String user = "CURRENT_DATE: " + LocalDate.now(clock) + "\n"
                + "PREVIOUS_VALIDATED_INTENT: " + (previousIntentJson == null ? "none" : previousIntentJson) + "\n"
                + "USER_QUESTION:\n" + safeQuestion + "\nEND_USER_QUESTION";
        AiProvider provider = providers.active();
        AiTextResult raw = provider.complete(new AiCompletionRequest(AiCompletionRequest.Task.INTENT, system, user, true));
        return new ExtractedIntent(intentParser.parse(raw.content()), raw);
    }

    public AiTextResult summarize(String question, ReportResult result) {
        try {
            String resultJson = objectMapper.writeValueAsString(result);
            String user = "USER_QUESTION:\n" + promptGuard.validate(question) + "\nEND_USER_QUESTION\nREPORT_JSON:\n" + resultJson;
            return providers.active().complete(new AiCompletionRequest(
                    AiCompletionRequest.Task.SUMMARY,
                    templates.content(PromptTemplateService.SUMMARY),
                    user,
                    false));
        } catch (AiProviderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AiProviderException("Unable to prepare report summary", exception);
        }
    }

    public List<String> suggestedQuestions(ReportType reportType) {
        String prompt = "REPORT_TYPE: " + reportType + "\nReturn three short JSON-array follow-up questions.";
        try {
            AiTextResult result = providers.active().complete(new AiCompletionRequest(
                    AiCompletionRequest.Task.SUGGESTIONS,
                    "Generate safe follow-up fleet reporting questions. Return a JSON array of strings only.",
                    prompt,
                    false));
            String[] values = objectMapper.readValue(result.content(), String[].class);
            return Arrays.stream(values).filter(value -> value != null && !value.isBlank()).limit(3).toList();
        } catch (Exception exception) {
            return List.of(
                    "Compare this result with last month",
                    "Show the top five vehicles",
                    "Which critical anomalies need attention?"
            );
        }
    }

    public String conversationTitle(String question) {
        String safe = promptGuard.validate(question);
        try {
            AiTextResult result = providers.active().complete(new AiCompletionRequest(
                    AiCompletionRequest.Task.TITLE,
                    "Create a concise conversation title under 60 characters. Return plain text only.",
                    "USER_QUESTION:\n" + safe + "\nEND_USER_QUESTION",
                    false));
            String title = result.content().trim();
            return title.length() <= 60 ? title : title.substring(0, 57) + "...";
        } catch (Exception exception) {
            return safe.length() <= 60 ? safe : safe.substring(0, 57) + "...";
        }
    }

    public record ExtractedIntent(IntentEnvelope envelope, AiTextResult metadata) {
    }
}
