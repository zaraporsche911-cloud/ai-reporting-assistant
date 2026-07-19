package io.github.zaraporsche911cloud.reportingassistant.report.validator;

import io.github.zaraporsche911cloud.reportingassistant.entity.UserRole;
import io.github.zaraporsche911cloud.reportingassistant.exception.UnsupportedReportException;
import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetDataSnapshot;
import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetOperationsGateway;
import io.github.zaraporsche911cloud.reportingassistant.report.FleetCapability;
import io.github.zaraporsche911cloud.reportingassistant.report.ReportCatalogue;
import io.github.zaraporsche911cloud.reportingassistant.report.model.DateRange;
import io.github.zaraporsche911cloud.reportingassistant.report.model.GroupingDimension;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportFilter;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportIntentValidatorTest {

    @Test
    void rejectsViewerReportExecution() {
        ReportIntentValidator validator = new ReportIntentValidator(new ReportCatalogue(), gateway(Set.of(FleetCapability.OVERVIEW)));
        assertThatThrownBy(() -> validator.validate(intent(ReportType.FLEET_OVERVIEW, 20), UserRole.VIEWER))
                .isInstanceOf(UnsupportedReportException.class)
                .hasMessageContaining("role");
    }

    @Test
    void rejectsResultLimitAboveCatalogueMaximum() {
        ReportIntentValidator validator = new ReportIntentValidator(new ReportCatalogue(), gateway(Set.of(FleetCapability.OVERVIEW)));
        assertThatThrownBy(() -> validator.validate(intent(ReportType.FLEET_OVERVIEW, 101), UserRole.ADMIN))
                .isInstanceOf(UnsupportedReportException.class)
                .hasMessageContaining("between 1 and 100");
    }

    @Test
    void rejectsReportWhenSourceCapabilityIsUnavailable() {
        ReportIntentValidator validator = new ReportIntentValidator(new ReportCatalogue(), gateway(Set.of(FleetCapability.VEHICLES)));
        assertThatThrownBy(() -> validator.validate(intent(ReportType.FUEL_CONSUMPTION, 20), UserRole.ANALYST))
                .isInstanceOf(UnsupportedReportException.class)
                .hasMessageContaining("unavailable in TEST mode");
    }

    private ReportIntent intent(ReportType type, int limit) {
        return new ReportIntent(type,
                DateRange.custom(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 19)),
                null, ReportFilter.empty(), GroupingDimension.NONE, List.of(), null, limit, null);
    }

    private FleetOperationsGateway gateway(Set<FleetCapability> capabilities) {
        return new FleetOperationsGateway() {
            @Override public FleetDataSnapshot loadSnapshot() { return null; }
            @Override public Set<FleetCapability> capabilities() { return capabilities; }
            @Override public GatewayHealth health() { return new GatewayHealth(true, "test"); }
            @Override public String mode() { return "TEST"; }
        };
    }
}
