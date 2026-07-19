package io.github.zaraporsche911cloud.reportingassistant.report.executor;

import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetDataSnapshot;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportResult;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import io.github.zaraporsche911cloud.reportingassistant.report.model.VisualizationType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Component
public class TelemetryReportExecutor implements ReportExecutor {

    private static final Set<ReportType> TYPES = Set.of(
            ReportType.FUEL_CONSUMPTION,
            ReportType.FUEL_EFFICIENCY,
            ReportType.VEHICLE_AVAILABILITY,
            ReportType.MAINTENANCE_STATUS,
            ReportType.OVERDUE_MAINTENANCE,
            ReportType.TRIPS,
            ReportType.OPERATING_COSTS,
            ReportType.VEHICLE_UTILIZATION,
            ReportType.MONTHLY_COMPARISON,
            ReportType.PERIOD_COMPARISON
    );

    @Override
    public Set<ReportType> supportedTypes() {
        return TYPES;
    }

    @Override
    public ReportResult execute(ReportIntent intent, FleetDataSnapshot data) {
        return switch (intent.reportType()) {
            case FUEL_CONSUMPTION, FUEL_EFFICIENCY -> fuel(intent, data);
            case VEHICLE_AVAILABILITY -> availability(intent, data);
            case MAINTENANCE_STATUS, OVERDUE_MAINTENANCE -> maintenance(intent, data);
            case TRIPS -> trips(intent, data);
            case OPERATING_COSTS -> costs(intent, data);
            case VEHICLE_UTILIZATION -> utilization(intent, data);
            case MONTHLY_COMPARISON, PERIOD_COMPARISON -> comparison(intent, data);
            default -> throw new IllegalArgumentException("Unsupported telemetry report: " + intent.reportType());
        };
    }

    private ReportResult fuel(ReportIntent intent, FleetDataSnapshot data) {
        Map<Long, Aggregate> totals = new HashMap<>();
        data.fuelEntries().stream()
                .filter(entry -> ReportSupport.inRange(entry.date(), intent.dateRange()))
                .filter(entry -> ReportSupport.vehicleAllowed(entry.vehicleId(), intent.filters()))
                .forEach(entry -> totals.computeIfAbsent(entry.vehicleId(), ignored -> new Aggregate())
                        .add(entry.liters(), entry.distanceKm(), entry.cost()));
        Map<Long, FleetDataSnapshot.Vehicle> vehicles = ReportSupport.vehiclesById(data);
        Comparator<Map.Entry<Long, Aggregate>> comparator = intent.reportType() == ReportType.FUEL_EFFICIENCY
                ? Comparator.comparingDouble(entry -> entry.getValue().efficiency())
                : Comparator.<Map.Entry<Long, Aggregate>>comparingDouble(entry -> entry.getValue().fuel).reversed();
        List<Map<String, Object>> rows = totals.entrySet().stream().sorted(comparator).limit(intent.limit()).map(entry ->
                ReportSupport.row(
                        "vehicle", ReportSupport.vehicleName(entry.getKey(), vehicles),
                        "fuel", ReportSupport.round(entry.getValue().fuel),
                        "distance", ReportSupport.round(entry.getValue().distance),
                        "efficiency", ReportSupport.round(entry.getValue().efficiency()),
                        "cost", ReportSupport.round(entry.getValue().cost))).toList();
        double fuel = totals.values().stream().mapToDouble(value -> value.fuel).sum();
        double distance = totals.values().stream().mapToDouble(value -> value.distance).sum();
        return report(intent, data,
                intent.reportType() == ReportType.FUEL_EFFICIENCY ? "Fuel efficiency by vehicle" : "Fuel consumption by vehicle",
                List.of(
                        new ReportResult.KpiValue("Fuel consumed", String.valueOf(ReportSupport.round(fuel)), "liters", null),
                        new ReportResult.KpiValue("Distance", String.valueOf(ReportSupport.round(distance)), "km", null),
                        new ReportResult.KpiValue("Fleet efficiency", String.valueOf(ReportSupport.round(distance == 0 ? 0 : fuel * 100 / distance)), "L/100km", null)),
                List.of(
                        column("vehicle", "Vehicle", null), column("fuel", "Fuel", "L"), column("distance", "Distance", "km"),
                        column("efficiency", "Efficiency", "L/100km"), column("cost", "Fuel cost", "currency units")),
                rows, "vehicle", List.of(intent.reportType() == ReportType.FUEL_EFFICIENCY ? "efficiency" : "fuel"));
    }

    private ReportResult availability(ReportIntent intent, FleetDataSnapshot data) {
        Map<Long, AvailabilityAggregate> totals = new HashMap<>();
        data.availability().stream()
                .filter(entry -> ReportSupport.inRange(entry.date(), intent.dateRange()))
                .filter(entry -> ReportSupport.vehicleAllowed(entry.vehicleId(), intent.filters()))
                .forEach(entry -> totals.computeIfAbsent(entry.vehicleId(), ignored -> new AvailabilityAggregate())
                        .add(entry.availableHours(), entry.unavailableHours()));
        Map<Long, FleetDataSnapshot.Vehicle> vehicles = ReportSupport.vehiclesById(data);
        List<Map<String, Object>> rows = totals.entrySet().stream()
                .sorted(Comparator.comparingDouble(entry -> entry.getValue().rate()))
                .limit(intent.limit())
                .map(entry -> ReportSupport.row(
                        "vehicle", ReportSupport.vehicleName(entry.getKey(), vehicles),
                        "availability", ReportSupport.round(entry.getValue().rate()),
                        "availableHours", ReportSupport.round(entry.getValue().available),
                        "unavailableHours", ReportSupport.round(entry.getValue().unavailable)))
                .toList();
        double rate = totals.values().stream().mapToDouble(AvailabilityAggregate::rate).average().orElse(0);
        return report(intent, data, "Vehicle availability",
                List.of(new ReportResult.KpiValue("Average availability", String.valueOf(ReportSupport.round(rate)), "%", null)),
                List.of(column("vehicle", "Vehicle", null), column("availability", "Availability", "%"),
                        column("availableHours", "Available", "hours"), column("unavailableHours", "Unavailable", "hours")),
                rows, "vehicle", List.of("availability"));
    }

    private ReportResult maintenance(ReportIntent intent, FleetDataSnapshot data) {
        Map<Long, FleetDataSnapshot.Vehicle> vehicles = ReportSupport.vehiclesById(data);
        List<FleetDataSnapshot.Maintenance> records = data.maintenance().stream()
                .filter(item -> ReportSupport.vehicleAllowed(item.vehicleId(), intent.filters()))
                .filter(item -> intent.reportType() != ReportType.OVERDUE_MAINTENANCE || "OVERDUE".equals(item.status()))
                .filter(item -> intent.filters().statuses().isEmpty() || intent.filters().statuses().contains(item.status()))
                .sorted(Comparator.comparing(FleetDataSnapshot.Maintenance::dueDate))
                .limit(intent.limit()).toList();
        List<Map<String, Object>> rows = records.stream().map(item -> ReportSupport.row(
                "vehicle", ReportSupport.vehicleName(item.vehicleId(), vehicles),
                "description", item.description(),
                "dueDate", item.dueDate(),
                "status", item.status(),
                "cost", item.cost())).toList();
        return new ReportResult(
                intent.reportType(), intent.reportType() == ReportType.OVERDUE_MAINTENANCE ? "Overdue maintenance" : "Maintenance status",
                data.source(), intent.dateRange().from(), intent.dateRange().to(), VisualizationType.TABLE,
                List.of(new ReportResult.KpiValue("Maintenance items", String.valueOf(records.size()), "items", null)),
                List.of(column("vehicle", "Vehicle", null), column("description", "Work", null), column("dueDate", "Due", null),
                        column("status", "Status", null), column("cost", "Expected cost", "currency units")),
                rows, null, ReportSupport.notices(data));
    }

    private ReportResult trips(ReportIntent intent, FleetDataSnapshot data) {
        Map<Long, Aggregate> totals = new HashMap<>();
        data.trips().stream()
                .filter(trip -> ReportSupport.inRange(trip.date(), intent.dateRange()))
                .filter(trip -> ReportSupport.vehicleAllowed(trip.vehicleId(), intent.filters()))
                .filter(trip -> ReportSupport.driverAllowed(trip.driverId(), intent.filters()))
                .forEach(trip -> totals.computeIfAbsent(trip.vehicleId(), ignored -> new Aggregate())
                        .addTrip(trip.distanceKm(), trip.durationHours()));
        Map<Long, FleetDataSnapshot.Vehicle> vehicles = ReportSupport.vehiclesById(data);
        List<Map<String, Object>> rows = totals.entrySet().stream()
                .sorted(Map.Entry.<Long, Aggregate>comparingByValue(Comparator.comparingDouble(value -> value.distance)).reversed())
                .limit(intent.limit())
                .map(entry -> ReportSupport.row(
                        "vehicle", ReportSupport.vehicleName(entry.getKey(), vehicles),
                        "trips", entry.getValue().count,
                        "distance", ReportSupport.round(entry.getValue().distance),
                        "duration", ReportSupport.round(entry.getValue().hours)))
                .toList();
        return report(intent, data, "Trip activity",
                List.of(new ReportResult.KpiValue("Trips", String.valueOf(totals.values().stream().mapToLong(value -> value.count).sum()), "trips", null)),
                List.of(column("vehicle", "Vehicle", null), column("trips", "Trips", null), column("distance", "Distance", "km"),
                        column("duration", "Duration", "hours")), rows, "vehicle", List.of("distance", "trips"));
    }

    private ReportResult costs(ReportIntent intent, FleetDataSnapshot data) {
        Map<Long, double[]> totals = new HashMap<>();
        data.fuelEntries().stream().filter(entry -> ReportSupport.inRange(entry.date(), intent.dateRange()))
                .filter(entry -> ReportSupport.vehicleAllowed(entry.vehicleId(), intent.filters()))
                .forEach(entry -> totals.computeIfAbsent(entry.vehicleId(), ignored -> new double[2])[0] += entry.cost().doubleValue());
        data.maintenance().stream().filter(item -> ReportSupport.inRange(item.dueDate(), intent.dateRange()))
                .filter(item -> ReportSupport.vehicleAllowed(item.vehicleId(), intent.filters()))
                .forEach(item -> totals.computeIfAbsent(item.vehicleId(), ignored -> new double[2])[1] += item.cost().doubleValue());
        Map<Long, FleetDataSnapshot.Vehicle> vehicles = ReportSupport.vehiclesById(data);
        List<Map<String, Object>> rows = totals.entrySet().stream()
                .sorted((left, right) -> Double.compare(right.getValue()[0] + right.getValue()[1], left.getValue()[0] + left.getValue()[1]))
                .limit(intent.limit()).map(entry -> ReportSupport.row(
                        "vehicle", ReportSupport.vehicleName(entry.getKey(), vehicles),
                        "fuelCost", ReportSupport.round(entry.getValue()[0]),
                        "maintenanceCost", ReportSupport.round(entry.getValue()[1]),
                        "totalCost", ReportSupport.round(entry.getValue()[0] + entry.getValue()[1]))).toList();
        double total = totals.values().stream().mapToDouble(values -> values[0] + values[1]).sum();
        return report(intent, data, "Vehicle operating costs",
                List.of(new ReportResult.KpiValue("Operating cost", String.valueOf(ReportSupport.round(total)), "currency units", null)),
                List.of(column("vehicle", "Vehicle", null), column("fuelCost", "Fuel", "currency units"),
                        column("maintenanceCost", "Maintenance", "currency units"), column("totalCost", "Total", "currency units")),
                rows, "vehicle", List.of("fuelCost", "maintenanceCost"));
    }

    private ReportResult utilization(ReportIntent intent, FleetDataSnapshot data) {
        Map<Long, Aggregate> trips = new HashMap<>();
        data.trips().stream().filter(trip -> ReportSupport.inRange(trip.date(), intent.dateRange()))
                .filter(trip -> ReportSupport.vehicleAllowed(trip.vehicleId(), intent.filters()))
                .forEach(trip -> trips.computeIfAbsent(trip.vehicleId(), ignored -> new Aggregate()).addTrip(trip.distanceKm(), trip.durationHours()));
        Map<Long, AvailabilityAggregate> availability = new HashMap<>();
        data.availability().stream().filter(entry -> ReportSupport.inRange(entry.date(), intent.dateRange()))
                .filter(entry -> ReportSupport.vehicleAllowed(entry.vehicleId(), intent.filters()))
                .forEach(entry -> availability.computeIfAbsent(entry.vehicleId(), ignored -> new AvailabilityAggregate())
                        .add(entry.availableHours(), entry.unavailableHours()));
        Map<Long, FleetDataSnapshot.Vehicle> vehicles = ReportSupport.vehiclesById(data);
        List<Map<String, Object>> rows = vehicles.keySet().stream().filter(id -> trips.containsKey(id) || availability.containsKey(id))
                .map(id -> ReportSupport.row(
                        "vehicle", ReportSupport.vehicleName(id, vehicles),
                        "distance", ReportSupport.round(trips.getOrDefault(id, new Aggregate()).distance),
                        "trips", trips.getOrDefault(id, new Aggregate()).count,
                        "availability", ReportSupport.round(availability.getOrDefault(id, new AvailabilityAggregate()).rate())))
                .sorted((left, right) -> Double.compare(((Number) right.get("distance")).doubleValue(), ((Number) left.get("distance")).doubleValue()))
                .limit(intent.limit()).toList();
        return report(intent, data, "Vehicle utilization",
                List.of(new ReportResult.KpiValue("Vehicles measured", String.valueOf(rows.size()), "vehicles", null)),
                List.of(column("vehicle", "Vehicle", null), column("distance", "Distance", "km"),
                        column("trips", "Trips", null), column("availability", "Availability", "%")),
                rows, "vehicle", List.of("distance", "availability"));
    }

    private ReportResult comparison(ReportIntent intent, FleetDataSnapshot data) {
        Map<YearMonth, Aggregate> monthly = new TreeMap<>();
        data.fuelEntries().stream().filter(entry -> ReportSupport.inRange(entry.date(), intent.dateRange()))
                .filter(entry -> ReportSupport.vehicleAllowed(entry.vehicleId(), intent.filters()))
                .forEach(entry -> monthly.computeIfAbsent(YearMonth.from(entry.date()), ignored -> new Aggregate())
                        .add(entry.liters(), entry.distanceKm(), entry.cost()));
        data.trips().stream().filter(trip -> ReportSupport.inRange(trip.date(), intent.dateRange()))
                .filter(trip -> ReportSupport.vehicleAllowed(trip.vehicleId(), intent.filters()))
                .forEach(trip -> monthly.computeIfAbsent(YearMonth.from(trip.date()), ignored -> new Aggregate()).count++);
        List<Map<String, Object>> rows = monthly.entrySet().stream().map(entry -> ReportSupport.row(
                "period", entry.getKey().toString(),
                "fuel", ReportSupport.round(entry.getValue().fuel),
                "distance", ReportSupport.round(entry.getValue().distance),
                "cost", ReportSupport.round(entry.getValue().cost),
                "trips", entry.getValue().count)).toList();
        return report(intent, data, intent.reportType() == ReportType.MONTHLY_COMPARISON ? "Monthly fleet comparison" : "Fleet period comparison",
                List.of(new ReportResult.KpiValue("Periods", String.valueOf(rows.size()), "months", null)),
                List.of(column("period", "Period", null), column("fuel", "Fuel", "L"), column("distance", "Distance", "km"),
                        column("cost", "Cost", "currency units"), column("trips", "Trips", null)),
                rows, "period", List.of("fuel", "distance", "cost"));
    }

    private ReportResult report(
            ReportIntent intent,
            FleetDataSnapshot data,
            String title,
            List<ReportResult.KpiValue> kpis,
            List<ReportResult.ReportColumn> columns,
            List<Map<String, Object>> rows,
            String category,
            List<String> values
    ) {
        return new ReportResult(intent.reportType(), title, data.source(), intent.dateRange().from(), intent.dateRange().to(),
                intent.visualization(), kpis, columns, rows, new ReportResult.ChartData(category, values, rows), ReportSupport.notices(data));
    }

    private ReportResult.ReportColumn column(String key, String label, String unit) {
        return new ReportResult.ReportColumn(key, label, unit);
    }

    private static final class Aggregate {
        private double fuel;
        private double distance;
        private double cost;
        private double hours;
        private long count;

        void add(BigDecimal fuel, BigDecimal distance, BigDecimal cost) {
            this.fuel += fuel.doubleValue();
            this.distance += distance.doubleValue();
            this.cost += cost.doubleValue();
            this.count++;
        }

        void addTrip(BigDecimal distance, BigDecimal duration) {
            this.distance += distance.doubleValue();
            this.hours += duration.doubleValue();
            this.count++;
        }

        double efficiency() {
            return distance == 0 ? 0 : fuel * 100 / distance;
        }
    }

    private static final class AvailabilityAggregate {
        private double available;
        private double unavailable;

        void add(BigDecimal available, BigDecimal unavailable) {
            this.available += available.doubleValue();
            this.unavailable += unavailable.doubleValue();
        }

        double rate() {
            double total = available + unavailable;
            return total == 0 ? 0 : available * 100 / total;
        }
    }
}
