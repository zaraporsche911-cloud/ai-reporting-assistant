package io.github.zaraporsche911cloud.reportingassistant.report.model;

public record IntentEnvelope(
        boolean clarificationRequired,
        String clarificationQuestion,
        ReportIntent intent
) {
    public static IntentEnvelope intent(ReportIntent intent) {
        return new IntentEnvelope(false, null, intent);
    }

    public static IntentEnvelope clarification(String question) {
        return new IntentEnvelope(true, question, null);
    }
}
