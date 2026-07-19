package io.github.zaraporsche911cloud.reportingassistant.ai.provider;

import io.github.zaraporsche911cloud.reportingassistant.ai.AiCompletionRequest;
import io.github.zaraporsche911cloud.reportingassistant.ai.AiProvider;
import io.github.zaraporsche911cloud.reportingassistant.ai.AiTextResult;
import io.github.zaraporsche911cloud.reportingassistant.report.model.DateRange;
import io.github.zaraporsche911cloud.reportingassistant.report.model.GroupingDimension;
import io.github.zaraporsche911cloud.reportingassistant.report.model.IntentEnvelope;
import io.github.zaraporsche911cloud.reportingassistant.report.model.RelativePeriod;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportFilter;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportMetric;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import io.github.zaraporsche911cloud.reportingassistant.report.model.SortDirection;
import io.github.zaraporsche911cloud.reportingassistant.report.model.SortSpecification;
import io.github.zaraporsche911cloud.reportingassistant.report.model.VisualizationType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Locale;

@Component
public class MockAiProvider implements AiProvider {

    private final ObjectMapper objectMapper;

    public MockAiProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String id() {
        return "mock";
    }

    @Override
    public AiTextResult complete(AiCompletionRequest request) {
        String content = switch (request.task()) {
            case INTENT -> intent(request.userPrompt());
            case SUMMARY -> summary(request.userPrompt());
            case SUGGESTIONS -> suggestions(request.userPrompt());
            case TITLE -> title(request.userPrompt());
        };
        return new AiTextResult(content, id(), "fleetops-rules-v1", null, null);
    }

    @Override
    public ProviderHealth health() {
        return new ProviderHealth(true, "Deterministic local reporting provider is ready", "fleetops-rules-v1");
    }

    private String intent(String prompt) {
        String question = extractQuestion(prompt);
        String lower = question.toLowerCase(Locale.ROOT);
        if (lower.length() < 8 || lower.matches(".*\\b(something|anything|stuff)\\b.*")) {
            return write(IntentEnvelope.clarification(
                    "Which fleet area should I report on: vehicles, drivers, fuel, maintenance, trips, costs, or anomalies?"));
        }
        ReportType type = classify(lower);
        RelativePeriod period = period(lower);
        int limit = limit(lower);
        ReportFilter filters = new ReportFilter(
                List.of(), List.of(),
                lower.contains("open") ? List.of("OPEN", "IN_PROGRESS") : List.of(),
                lower.contains("critical") ? List.of("CRITICAL") : List.of());
        GroupingDimension groupBy = grouping(type);
        List<ReportMetric> metrics = metrics(type);
        String sortField = sortField(type);
        ReportIntent intent = new ReportIntent(
                type,
                DateRange.relative(period),
                type == ReportType.PERIOD_COMPARISON ? DateRange.relative(RelativePeriod.LAST_MONTH) : null,
                filters,
                groupBy,
                metrics,
                new SortSpecification(sortField, SortDirection.DESC),
                limit,
                visualization(type));
        return write(IntentEnvelope.intent(intent));
    }

    private ReportType classify(String question) {
        if ((question.contains("trend") || question.contains("six months")) && question.contains("anomal")) return ReportType.TREND_ANALYSIS;
        if (question.contains("critical") && (question.contains("problem") || question.contains("anomal"))) return ReportType.CRITICAL_ANOMALIES;
        if (question.contains("anomal")) return ReportType.ANOMALIES;
        if (question.contains("overdue") && question.contains("maintenance")) return ReportType.OVERDUE_MAINTENANCE;
        if (question.contains("maintenance")) return ReportType.MAINTENANCE_STATUS;
        if (question.contains("unavailable") || question.contains("availability")) return ReportType.VEHICLE_AVAILABILITY;
        if (question.contains("operating cost") || question.contains("highest cost") || question.contains("cost")) return ReportType.OPERATING_COSTS;
        if (question.contains("utilization") || question.contains("utilisation")) return ReportType.VEHICLE_UTILIZATION;
        if (question.contains("abnormal fuel") || question.contains("fuel efficien") || question.contains("liters per")) return ReportType.FUEL_EFFICIENCY;
        if (question.contains("fuel")) return ReportType.FUEL_CONSUMPTION;
        if (question.contains("compare") && (question.contains("month") || question.contains("june") || question.contains("july"))) return ReportType.MONTHLY_COMPARISON;
        if (question.contains("compare")) return ReportType.PERIOD_COMPARISON;
        if (question.contains("driver") && (question.contains("top") || question.contains("most") || question.contains("exceeded"))) return ReportType.TOP_DRIVERS;
        if (question.contains("driver") && (question.contains("performance") || question.contains("distance") || question.contains("mileage"))) return ReportType.DRIVER_PERFORMANCE;
        if (question.contains("trip") || question.contains("activity")) return ReportType.TRIPS;
        if (question.contains("highest mileage") || question.contains("top vehicle")) return ReportType.TOP_VEHICLES;
        if (question.contains("mileage") || question.contains("odometer")) return ReportType.MILEAGE;
        return ReportType.FLEET_OVERVIEW;
    }

    private RelativePeriod period(String question) {
        if (question.contains("today")) return RelativePeriod.TODAY;
        if (question.contains("yesterday")) return RelativePeriod.YESTERDAY;
        if (question.contains("last week")) return RelativePeriod.LAST_WEEK;
        if (question.contains("this week")) return RelativePeriod.THIS_WEEK;
        if (question.contains("last month")) return RelativePeriod.LAST_MONTH;
        if (question.contains("this month") || question.contains("current month")) return RelativePeriod.THIS_MONTH;
        if (question.contains("last quarter")) return RelativePeriod.LAST_QUARTER;
        if (question.contains("this quarter")) return RelativePeriod.THIS_QUARTER;
        if (question.contains("last year")) return RelativePeriod.LAST_YEAR;
        if (question.contains("six months")) return RelativePeriod.LAST_90_DAYS;
        if (question.contains("90 days")) return RelativePeriod.LAST_90_DAYS;
        if (question.contains("30 days")) return RelativePeriod.LAST_30_DAYS;
        if (question.contains("7 days")) return RelativePeriod.LAST_7_DAYS;
        return RelativePeriod.THIS_MONTH;
    }

    private int limit(String question) {
        if (question.contains("five") || question.matches(".*\\b5\\b.*")) return 5;
        if (question.contains("ten") || question.matches(".*\\b10\\b.*")) return 10;
        return 20;
    }

    private GroupingDimension grouping(ReportType type) {
        return switch (type) {
            case DRIVER_PERFORMANCE, TOP_DRIVERS -> GroupingDimension.DRIVER;
            case TREND_ANALYSIS, MONTHLY_COMPARISON, PERIOD_COMPARISON -> GroupingDimension.MONTH;
            case FLEET_OVERVIEW -> GroupingDimension.STATUS;
            default -> GroupingDimension.VEHICLE;
        };
    }

    private List<ReportMetric> metrics(ReportType type) {
        return switch (type) {
            case FUEL_CONSUMPTION -> List.of(ReportMetric.TOTAL_FUEL, ReportMetric.TOTAL_DISTANCE);
            case FUEL_EFFICIENCY -> List.of(ReportMetric.AVERAGE_CONSUMPTION, ReportMetric.TOTAL_DISTANCE);
            case DRIVER_PERFORMANCE, TOP_DRIVERS, MILEAGE, TOP_VEHICLES -> List.of(ReportMetric.TOTAL_DISTANCE);
            case VEHICLE_AVAILABILITY -> List.of(ReportMetric.AVAILABILITY_RATE, ReportMetric.UNAVAILABLE_HOURS);
            case ANOMALIES, TREND_ANALYSIS -> List.of(ReportMetric.ANOMALY_COUNT);
            case CRITICAL_ANOMALIES -> List.of(ReportMetric.CRITICAL_COUNT);
            case TRIPS -> List.of(ReportMetric.TRIP_COUNT, ReportMetric.TOTAL_DISTANCE);
            case OPERATING_COSTS -> List.of(ReportMetric.TOTAL_COST);
            case VEHICLE_UTILIZATION -> List.of(ReportMetric.UTILIZATION_RATE, ReportMetric.TOTAL_DISTANCE);
            case MAINTENANCE_STATUS, OVERDUE_MAINTENANCE -> List.of(ReportMetric.MAINTENANCE_COUNT);
            default -> List.of(ReportMetric.TOTAL_VEHICLES, ReportMetric.ACTIVE_VEHICLES);
        };
    }

    private String sortField(ReportType type) {
        return switch (type) {
            case FUEL_CONSUMPTION, MONTHLY_COMPARISON, PERIOD_COMPARISON -> "fuel";
            case FUEL_EFFICIENCY -> "efficiency";
            case MILEAGE, TOP_VEHICLES -> "mileage";
            case DRIVER_PERFORMANCE, TOP_DRIVERS, TRIPS, VEHICLE_UTILIZATION -> "distance";
            case VEHICLE_AVAILABILITY -> "availability";
            case OPERATING_COSTS -> "cost";
            case ANOMALIES, CRITICAL_ANOMALIES, TREND_ANALYSIS -> "count";
            default -> "value";
        };
    }

    private VisualizationType visualization(ReportType type) {
        return switch (type) {
            case FLEET_OVERVIEW -> VisualizationType.KPI;
            case ANOMALIES, CRITICAL_ANOMALIES, MAINTENANCE_STATUS, OVERDUE_MAINTENANCE -> VisualizationType.TABLE;
            case TREND_ANALYSIS, MONTHLY_COMPARISON -> VisualizationType.LINE_CHART;
            default -> VisualizationType.BAR_CHART;
        };
    }

    private String summary(String prompt) {
        try {
            String json = prompt.substring(prompt.indexOf("REPORT_JSON:") + "REPORT_JSON:".length()).trim();
            JsonNode report = objectMapper.readTree(json);
            String title = report.get("title").stringValue();
            JsonNode kpis = report.get("kpis");
            StringBuilder text = new StringBuilder(title).append(" covers the requested period. ");
            if (kpis != null && kpis.isArray()) {
                for (int index = 0; index < Math.min(3, kpis.size()); index++) {
                    JsonNode kpi = kpis.get(index);
                    text.append(kpi.get("label").stringValue()).append(": ")
                            .append(kpi.get("value").stringValue());
                    if (kpi.get("unit") != null && !kpi.get("unit").isNull()) text.append(' ').append(kpi.get("unit").stringValue());
                    text.append(index + 1 < Math.min(3, kpis.size()) ? "; " : ". ");
                }
            }
            JsonNode rows = report.get("rows");
            text.append(rows == null || rows.size() == 0
                    ? "No matching operational records were found."
                    : "The table and chart contain " + rows.size() + " grounded result rows.");
            return text.toString();
        } catch (Exception exception) {
            return "The report was calculated successfully from structured FleetOps data. Review the KPIs, table, and applied filters for details.";
        }
    }

    private String suggestions(String prompt) {
        return write(List.of(
                "Compare this result with last month",
                "Show the top five vehicles",
                "Which critical anomalies need attention?"
        ));
    }

    private String title(String prompt) {
        String question = extractQuestion(prompt);
        return question.length() <= 60 ? question : question.substring(0, 57) + "...";
    }

    private String extractQuestion(String prompt) {
        int marker = prompt.indexOf("USER_QUESTION:");
        if (marker < 0) return prompt.trim();
        String value = prompt.substring(marker + "USER_QUESTION:".length());
        int end = value.indexOf("\nEND_USER_QUESTION");
        return (end < 0 ? value : value.substring(0, end)).trim();
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to serialize deterministic AI response", exception);
        }
    }
}
