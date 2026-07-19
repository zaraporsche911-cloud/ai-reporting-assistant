package io.github.zaraporsche911cloud.reportingassistant.report.validator;

import io.github.zaraporsche911cloud.reportingassistant.entity.UserRole;
import io.github.zaraporsche911cloud.reportingassistant.report.ReportCatalogue;
import io.github.zaraporsche911cloud.reportingassistant.report.ReportDefinition;
import io.github.zaraporsche911cloud.reportingassistant.report.model.DateRange;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import org.springframework.stereotype.Component;

@Component
public class ReportIntentPlanner {

    private final DateRangeResolver dateRangeResolver;
    private final ReportIntentValidator validator;
    private final ReportCatalogue catalogue;

    public ReportIntentPlanner(DateRangeResolver dateRangeResolver, ReportIntentValidator validator, ReportCatalogue catalogue) {
        this.dateRangeResolver = dateRangeResolver;
        this.validator = validator;
        this.catalogue = catalogue;
    }

    public ReportIntent prepare(ReportIntent raw, UserRole role) {
        ReportDefinition definition = catalogue.get(raw.reportType());
        DateRange dateRange = dateRangeResolver.resolve(raw.dateRange());
        DateRange comparison = raw.comparisonDateRange() == null ? null : dateRangeResolver.resolve(raw.comparisonDateRange());
        ReportIntent prepared = new ReportIntent(
                raw.reportType(), dateRange, comparison, raw.filters(), raw.groupBy(), raw.metrics(), raw.sort(),
                raw.limit(), raw.visualization() == null ? definition.defaultVisualization() : raw.visualization());
        validator.validate(prepared, role);
        return prepared;
    }
}
