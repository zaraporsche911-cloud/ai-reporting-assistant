package io.github.zaraporsche911cloud.reportingassistant.report;

import io.github.zaraporsche911cloud.reportingassistant.entity.UserRole;
import io.github.zaraporsche911cloud.reportingassistant.report.model.GroupingDimension;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportMetric;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import io.github.zaraporsche911cloud.reportingassistant.report.model.VisualizationType;

import java.util.Set;

public record ReportDefinition(
        ReportType type,
        String displayName,
        String description,
        Set<FleetCapability> requiredCapabilities,
        Set<ReportMetric> supportedMetrics,
        Set<GroupingDimension> supportedGroupings,
        Set<String> supportedSortFields,
        int maximumResultSize,
        Set<UserRole> allowedRoles,
        VisualizationType defaultVisualization,
        String executorKey
) {
}
