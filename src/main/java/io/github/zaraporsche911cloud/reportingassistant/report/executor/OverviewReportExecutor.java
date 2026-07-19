package io.github.zaraporsche911cloud.reportingassistant.report.executor;

import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetDataSnapshot;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportResult;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import io.github.zaraporsche911cloud.reportingassistant.report.model.VisualizationType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class OverviewReportExecutor implements ReportExecutor {

    @Override
    public Set<ReportType> supportedTypes() {
        return Set.of(ReportType.FLEET_OVERVIEW);
    }

    @Override
    public ReportResult execute(ReportIntent intent, FleetDataSnapshot data) {
        FleetDataSnapshot.Overview overview = data.overview();
        List<ReportResult.KpiValue> kpis = List.of(
                new ReportResult.KpiValue("Total vehicles", String.valueOf(overview.totalVehicles()), "vehicles", null),
                new ReportResult.KpiValue("Active vehicles", String.valueOf(overview.activeVehicles()), "vehicles", null),
                new ReportResult.KpiValue("Open anomalies", String.valueOf(overview.openAnomalies()), "anomalies", null),
                new ReportResult.KpiValue("Critical risks", String.valueOf(overview.criticalOpenAnomalies()), "anomalies", null)
        );
        List<Map<String, Object>> rows = List.of(
                ReportSupport.row("metric", "Active vehicles", "value", overview.activeVehicles()),
                ReportSupport.row("metric", "In maintenance", "value", overview.vehiclesInMaintenance()),
                ReportSupport.row("metric", "Active drivers", "value", overview.activeDrivers()),
                ReportSupport.row("metric", "Open anomalies", "value", overview.openAnomalies())
        );
        return new ReportResult(
                intent.reportType(), "Fleet operations overview", data.source(), intent.dateRange().from(), intent.dateRange().to(),
                VisualizationType.KPI, kpis,
                List.of(new ReportResult.ReportColumn("metric", "Metric", null), new ReportResult.ReportColumn("value", "Value", null)),
                rows, new ReportResult.ChartData("metric", List.of("value"), rows), ReportSupport.notices(data));
    }
}
