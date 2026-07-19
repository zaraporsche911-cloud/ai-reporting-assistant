package io.github.zaraporsche911cloud.reportingassistant.report.executor;

import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetDataSnapshot;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportResult;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DriverReportExecutor implements ReportExecutor {

    @Override
    public Set<ReportType> supportedTypes() {
        return Set.of(ReportType.DRIVER_PERFORMANCE, ReportType.TOP_DRIVERS);
    }

    @Override
    public ReportResult execute(ReportIntent intent, FleetDataSnapshot data) {
        Map<Long, DriverActivity> activity = new HashMap<>();
        data.trips().stream()
                .filter(trip -> ReportSupport.inRange(trip.date(), intent.dateRange()))
                .filter(trip -> trip.driverId() != null && ReportSupport.driverAllowed(trip.driverId(), intent.filters()))
                .forEach(trip -> activity.computeIfAbsent(trip.driverId(), ignored -> new DriverActivity())
                        .add(trip.distanceKm(), trip.durationHours()));
        Map<Long, FleetDataSnapshot.Driver> drivers = ReportSupport.driversById(data);
        List<Map<String, Object>> rows = activity.entrySet().stream()
                .sorted(Map.Entry.<Long, DriverActivity>comparingByValue(Comparator.comparingDouble(DriverActivity::distance)).reversed())
                .limit(intent.limit())
                .map(entry -> ReportSupport.row(
                        "driver", ReportSupport.driverName(entry.getKey(), drivers),
                        "employeeNumber", drivers.containsKey(entry.getKey()) ? drivers.get(entry.getKey()).employeeNumber() : "—",
                        "distance", ReportSupport.round(entry.getValue().distance()),
                        "trips", entry.getValue().trips,
                        "drivingHours", ReportSupport.round(entry.getValue().hours())))
                .toList();
        double totalDistance = activity.values().stream().mapToDouble(DriverActivity::distance).sum();
        return new ReportResult(
                intent.reportType(), intent.reportType() == ReportType.TOP_DRIVERS ? "Top drivers by distance" : "Driver activity performance",
                data.source(), intent.dateRange().from(), intent.dateRange().to(), intent.visualization(),
                List.of(
                        new ReportResult.KpiValue("Active drivers", String.valueOf(activity.size()), "drivers", null),
                        new ReportResult.KpiValue("Total distance", String.valueOf(ReportSupport.round(totalDistance)), "km", null)),
                List.of(
                        new ReportResult.ReportColumn("driver", "Driver", null),
                        new ReportResult.ReportColumn("employeeNumber", "Employee", null),
                        new ReportResult.ReportColumn("distance", "Distance", "km"),
                        new ReportResult.ReportColumn("trips", "Trips", null),
                        new ReportResult.ReportColumn("drivingHours", "Driving time", "hours")),
                rows, new ReportResult.ChartData("driver", List.of("distance", "trips"), rows),
                ReportSupport.notices(data, "Performance is limited to objective trip activity; it does not infer driver quality or causation."));
    }

    private static final class DriverActivity {
        private double distance;
        private double hours;
        private long trips;

        void add(BigDecimal distance, BigDecimal hours) {
            this.distance += distance.doubleValue();
            this.hours += hours.doubleValue();
            this.trips++;
        }

        double distance() { return distance; }
        double hours() { return hours; }
    }
}
