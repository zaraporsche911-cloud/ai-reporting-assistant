package io.github.zaraporsche911cloud.reportingassistant.report.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ReportResult(
        ReportType reportType,
        String title,
        String dataSource,
        LocalDate from,
        LocalDate to,
        VisualizationType visualization,
        List<KpiValue> kpis,
        List<ReportColumn> columns,
        List<Map<String, Object>> rows,
        ChartData chart,
        List<String> notices
) {
    public ReportResult {
        kpis = kpis == null ? List.of() : List.copyOf(kpis);
        columns = columns == null ? List.of() : List.copyOf(columns);
        rows = rows == null ? List.of() : List.copyOf(rows);
        notices = notices == null ? List.of() : List.copyOf(notices);
    }

    public record KpiValue(String label, String value, String unit, String trend) {
    }

    public record ReportColumn(String key, String label, String unit) {
    }

    public record ChartData(String categoryKey, List<String> valueKeys, List<Map<String, Object>> points) {
        public ChartData {
            valueKeys = valueKeys == null ? List.of() : List.copyOf(valueKeys);
            points = points == null ? List.of() : List.copyOf(points);
        }
    }
}
