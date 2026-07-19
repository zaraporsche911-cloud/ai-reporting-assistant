package io.github.zaraporsche911cloud.reportingassistant.report.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReportIntent(
        @NotNull ReportType reportType,
        @Valid DateRange dateRange,
        @Valid DateRange comparisonDateRange,
        @Valid ReportFilter filters,
        GroupingDimension groupBy,
        List<ReportMetric> metrics,
        @Valid SortSpecification sort,
        @Min(1) @Max(100) Integer limit,
        VisualizationType visualization
) {
    public ReportIntent {
        filters = filters == null ? ReportFilter.empty() : filters;
        groupBy = groupBy == null ? GroupingDimension.NONE : groupBy;
        metrics = metrics == null ? List.of() : List.copyOf(metrics);
        limit = limit == null ? 20 : limit;
    }
}
