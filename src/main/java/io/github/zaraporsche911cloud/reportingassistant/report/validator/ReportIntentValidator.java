package io.github.zaraporsche911cloud.reportingassistant.report.validator;

import io.github.zaraporsche911cloud.reportingassistant.entity.UserRole;
import io.github.zaraporsche911cloud.reportingassistant.exception.UnsupportedReportException;
import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetOperationsGateway;
import io.github.zaraporsche911cloud.reportingassistant.report.ReportCatalogue;
import io.github.zaraporsche911cloud.reportingassistant.report.ReportDefinition;
import io.github.zaraporsche911cloud.reportingassistant.report.model.DateRange;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
public class ReportIntentValidator {

    private final ReportCatalogue catalogue;
    private final FleetOperationsGateway fleetGateway;

    public ReportIntentValidator(ReportCatalogue catalogue, FleetOperationsGateway fleetGateway) {
        this.catalogue = catalogue;
        this.fleetGateway = fleetGateway;
    }

    public void validate(ReportIntent intent, UserRole role) {
        if (intent == null || intent.reportType() == null) {
            throw new UnsupportedReportException("The AI response did not contain a supported report type");
        }
        ReportDefinition definition = catalogue.get(intent.reportType());
        if (!definition.allowedRoles().contains(role)) {
            throw new UnsupportedReportException("Your role is not permitted to generate this report type");
        }
        if (!fleetGateway.capabilities().containsAll(definition.requiredCapabilities())) {
            throw new UnsupportedReportException(
                    intent.reportType() + " is unavailable in " + fleetGateway.mode()
                            + " mode because Fleet Control Tower does not expose the required source data");
        }
        if (intent.limit() < 1 || intent.limit() > definition.maximumResultSize()) {
            throw new UnsupportedReportException("Report result limit must be between 1 and " + definition.maximumResultSize());
        }
        validateRange(intent.dateRange());
        if (intent.comparisonDateRange() != null) validateRange(intent.comparisonDateRange());
        if (intent.groupBy() != null && !definition.supportedGroupings().contains(intent.groupBy())) {
            throw new UnsupportedReportException("Unsupported grouping for " + intent.reportType() + ": " + intent.groupBy());
        }
        if (intent.sort() != null && intent.sort().field() != null
                && !definition.supportedSortFields().contains(intent.sort().field().toLowerCase())) {
            throw new UnsupportedReportException("Unsupported sort field: " + intent.sort().field());
        }
        if (!definition.supportedMetrics().containsAll(intent.metrics())) {
            throw new UnsupportedReportException("One or more requested metrics are not supported");
        }
    }

    private void validateRange(DateRange range) {
        if (range == null || range.from() == null || range.to() == null) {
            throw new UnsupportedReportException("A valid report date range is required");
        }
        if (range.from().isAfter(range.to())) {
            throw new UnsupportedReportException("Report start date must not be after the end date");
        }
        if (ChronoUnit.DAYS.between(range.from(), range.to()) > 730) {
            throw new UnsupportedReportException("Report date range cannot exceed 730 days");
        }
    }
}
