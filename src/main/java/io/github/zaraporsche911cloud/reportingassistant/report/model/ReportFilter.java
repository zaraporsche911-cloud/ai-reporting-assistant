package io.github.zaraporsche911cloud.reportingassistant.report.model;

import java.util.List;

public record ReportFilter(
        List<Long> vehicleIds,
        List<Long> driverIds,
        List<String> statuses,
        List<String> severityLevels
) {
    public ReportFilter {
        vehicleIds = vehicleIds == null ? List.of() : List.copyOf(vehicleIds);
        driverIds = driverIds == null ? List.of() : List.copyOf(driverIds);
        statuses = statuses == null ? List.of() : statuses.stream().map(String::toUpperCase).toList();
        severityLevels = severityLevels == null ? List.of() : severityLevels.stream().map(String::toUpperCase).toList();
    }

    public static ReportFilter empty() {
        return new ReportFilter(List.of(), List.of(), List.of(), List.of());
    }
}
