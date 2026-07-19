package io.github.zaraporsche911cloud.reportingassistant.report.executor;

import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetDataSnapshot;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportResult;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class VehicleReportExecutor implements ReportExecutor {

    @Override
    public Set<ReportType> supportedTypes() {
        return Set.of(ReportType.MILEAGE, ReportType.TOP_VEHICLES);
    }

    @Override
    public ReportResult execute(ReportIntent intent, FleetDataSnapshot data) {
        List<FleetDataSnapshot.Vehicle> vehicles = data.vehicles().stream()
                .filter(vehicle -> ReportSupport.vehicleAllowed(vehicle.id(), intent.filters()))
                .filter(vehicle -> intent.filters().statuses().isEmpty()
                        || intent.filters().statuses().contains(vehicle.status().toUpperCase()))
                .sorted(Comparator.comparingLong(FleetDataSnapshot.Vehicle::mileage).reversed())
                .limit(intent.limit())
                .toList();
        List<Map<String, Object>> rows = vehicles.stream().map(vehicle -> ReportSupport.row(
                "vehicle", vehicle.registrationNumber(),
                "brandModel", vehicle.brand() + " " + vehicle.model(),
                "status", vehicle.status(),
                "mileage", vehicle.mileage())).toList();
        long totalMileage = vehicles.stream().mapToLong(FleetDataSnapshot.Vehicle::mileage).sum();
        return new ReportResult(
                intent.reportType(), intent.reportType() == ReportType.TOP_VEHICLES ? "Vehicles with highest mileage" : "Current vehicle mileage",
                data.source(), intent.dateRange().from(), intent.dateRange().to(), intent.visualization(),
                List.of(
                        new ReportResult.KpiValue("Vehicles", String.valueOf(vehicles.size()), "vehicles", null),
                        new ReportResult.KpiValue("Combined mileage", String.valueOf(totalMileage), "km", null)),
                List.of(
                        new ReportResult.ReportColumn("vehicle", "Vehicle", null),
                        new ReportResult.ReportColumn("brandModel", "Brand / model", null),
                        new ReportResult.ReportColumn("status", "Status", null),
                        new ReportResult.ReportColumn("mileage", "Current mileage", "km")),
                rows, new ReportResult.ChartData("vehicle", List.of("mileage"), rows),
                ReportSupport.notices(data, "Mileage is the current recorded odometer value, not distance travelled during the selected period."));
    }
}
