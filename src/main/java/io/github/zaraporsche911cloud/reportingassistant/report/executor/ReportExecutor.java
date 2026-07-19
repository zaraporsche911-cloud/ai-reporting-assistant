package io.github.zaraporsche911cloud.reportingassistant.report.executor;

import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetDataSnapshot;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportResult;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;

import java.util.Set;

public interface ReportExecutor {
    Set<ReportType> supportedTypes();
    ReportResult execute(ReportIntent intent, FleetDataSnapshot data);
}
