package io.github.zaraporsche911cloud.reportingassistant.report.engine;

import io.github.zaraporsche911cloud.reportingassistant.entity.UserRole;
import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetDataSnapshot;
import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetOperationsGateway;
import io.github.zaraporsche911cloud.reportingassistant.report.executor.ReportExecutorRegistry;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportResult;
import io.github.zaraporsche911cloud.reportingassistant.report.validator.ReportIntentPlanner;
import org.springframework.stereotype.Service;

@Service
public class ReportingEngine {

    private final ReportIntentPlanner planner;
    private final ReportExecutorRegistry executors;
    private final FleetOperationsGateway fleetGateway;

    public ReportingEngine(ReportIntentPlanner planner, ReportExecutorRegistry executors, FleetOperationsGateway fleetGateway) {
        this.planner = planner;
        this.executors = executors;
        this.fleetGateway = fleetGateway;
    }

    public ExecutedReport execute(ReportIntent rawIntent, UserRole role) {
        ReportIntent intent = planner.prepare(rawIntent, role);
        FleetDataSnapshot snapshot = fleetGateway.loadSnapshot();
        ReportResult result = executors.get(intent.reportType()).execute(intent, snapshot);
        return new ExecutedReport(intent, result);
    }

    public record ExecutedReport(ReportIntent intent, ReportResult result) {
    }
}
