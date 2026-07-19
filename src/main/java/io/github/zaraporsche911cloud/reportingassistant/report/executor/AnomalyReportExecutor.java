package io.github.zaraporsche911cloud.reportingassistant.report.executor;

import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetDataSnapshot;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportResult;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import io.github.zaraporsche911cloud.reportingassistant.report.model.VisualizationType;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Component
public class AnomalyReportExecutor implements ReportExecutor {

    @Override
    public Set<ReportType> supportedTypes() {
        return Set.of(ReportType.ANOMALIES, ReportType.CRITICAL_ANOMALIES, ReportType.TREND_ANALYSIS);
    }

    @Override
    public ReportResult execute(ReportIntent intent, FleetDataSnapshot data) {
        List<FleetDataSnapshot.Anomaly> anomalies = data.anomalies().stream()
                .filter(anomaly -> ReportSupport.inRange(anomaly.reportedAt(), intent.dateRange()))
                .filter(anomaly -> ReportSupport.vehicleAllowed(anomaly.vehicleId(), intent.filters()))
                .filter(anomaly -> intent.filters().statuses().isEmpty()
                        || intent.filters().statuses().contains(anomaly.status().toUpperCase()))
                .filter(anomaly -> intent.filters().severityLevels().isEmpty()
                        || intent.filters().severityLevels().contains(anomaly.severity().toUpperCase()))
                .filter(anomaly -> intent.reportType() != ReportType.CRITICAL_ANOMALIES
                        || ("CRITICAL".equals(anomaly.severity()) && !"RESOLVED".equals(anomaly.status())))
                .sorted(Comparator.comparing(FleetDataSnapshot.Anomaly::reportedAt).reversed())
                .toList();
        if (intent.reportType() == ReportType.TREND_ANALYSIS) return trend(intent, data, anomalies);
        List<Map<String, Object>> rows = anomalies.stream().limit(intent.limit()).map(anomaly -> ReportSupport.row(
                "reportedAt", anomaly.reportedAt(),
                "vehicle", anomaly.vehicleRegistration(),
                "title", anomaly.title(),
                "type", anomaly.type(),
                "severity", anomaly.severity(),
                "status", anomaly.status())).toList();
        long critical = anomalies.stream().filter(anomaly -> "CRITICAL".equals(anomaly.severity())).count();
        return new ReportResult(
                intent.reportType(), intent.reportType() == ReportType.CRITICAL_ANOMALIES ? "Critical operational anomalies" : "Operational anomalies",
                data.source(), intent.dateRange().from(), intent.dateRange().to(), VisualizationType.TABLE,
                List.of(
                        new ReportResult.KpiValue("Anomalies", String.valueOf(anomalies.size()), "records", null),
                        new ReportResult.KpiValue("Critical", String.valueOf(critical), "records", null)),
                List.of(
                        new ReportResult.ReportColumn("reportedAt", "Reported", null),
                        new ReportResult.ReportColumn("vehicle", "Vehicle", null),
                        new ReportResult.ReportColumn("title", "Issue", null),
                        new ReportResult.ReportColumn("severity", "Severity", null),
                        new ReportResult.ReportColumn("status", "Status", null)),
                rows, null, ReportSupport.notices(data));
    }

    private ReportResult trend(ReportIntent intent, FleetDataSnapshot data, List<FleetDataSnapshot.Anomaly> anomalies) {
        Map<YearMonth, long[]> grouped = new TreeMap<>();
        anomalies.forEach(anomaly -> {
            YearMonth month = YearMonth.from(anomaly.reportedAt().atZone(ZoneOffset.UTC));
            long[] values = grouped.computeIfAbsent(month, ignored -> new long[2]);
            values[0]++;
            if ("CRITICAL".equals(anomaly.severity())) values[1]++;
        });
        List<Map<String, Object>> rows = grouped.entrySet().stream().map(entry -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("period", entry.getKey().toString());
            row.put("anomalies", entry.getValue()[0]);
            row.put("critical", entry.getValue()[1]);
            return row;
        }).toList();
        return new ReportResult(
                intent.reportType(), "Anomaly trend analysis", data.source(), intent.dateRange().from(), intent.dateRange().to(),
                VisualizationType.LINE_CHART,
                List.of(new ReportResult.KpiValue("Total anomalies", String.valueOf(anomalies.size()), "records", null)),
                List.of(
                        new ReportResult.ReportColumn("period", "Month", null),
                        new ReportResult.ReportColumn("anomalies", "Anomalies", null),
                        new ReportResult.ReportColumn("critical", "Critical", null)),
                rows, new ReportResult.ChartData("period", List.of("anomalies", "critical"), rows), ReportSupport.notices(data));
    }
}
