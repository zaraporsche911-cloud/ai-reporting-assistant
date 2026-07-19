package io.github.zaraporsche911cloud.reportingassistant.report.executor;

import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetDataSnapshot;
import io.github.zaraporsche911cloud.reportingassistant.report.model.DateRange;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportFilter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

final class ReportSupport {

    private ReportSupport() {
    }

    static boolean inRange(LocalDate date, DateRange range) {
        return date != null && !date.isBefore(range.from()) && !date.isAfter(range.to());
    }

    static boolean inRange(java.time.Instant instant, DateRange range) {
        return instant != null && inRange(instant.atZone(ZoneOffset.UTC).toLocalDate(), range);
    }

    static boolean vehicleAllowed(Long vehicleId, ReportFilter filter) {
        return filter.vehicleIds().isEmpty() || filter.vehicleIds().contains(vehicleId);
    }

    static boolean driverAllowed(Long driverId, ReportFilter filter) {
        return filter.driverIds().isEmpty() || filter.driverIds().contains(driverId);
    }

    static double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    static Map<Long, FleetDataSnapshot.Vehicle> vehiclesById(FleetDataSnapshot data) {
        return data.vehicles().stream().collect(Collectors.toMap(FleetDataSnapshot.Vehicle::id, Function.identity()));
    }

    static Map<Long, FleetDataSnapshot.Driver> driversById(FleetDataSnapshot data) {
        return data.drivers().stream().collect(Collectors.toMap(FleetDataSnapshot.Driver::id, Function.identity()));
    }

    static String vehicleName(Long id, Map<Long, FleetDataSnapshot.Vehicle> vehicles) {
        FleetDataSnapshot.Vehicle vehicle = vehicles.get(id);
        return vehicle == null ? "Vehicle " + id : vehicle.registrationNumber();
    }

    static String driverName(Long id, Map<Long, FleetDataSnapshot.Driver> drivers) {
        FleetDataSnapshot.Driver driver = drivers.get(id);
        return driver == null ? "Unassigned" : driver.fullName();
    }

    static Map<String, Object> row(Object... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            row.put(String.valueOf(values[index]), values[index + 1]);
        }
        return row;
    }

    static List<String> notices(FleetDataSnapshot data, String... additional) {
        java.util.ArrayList<String> notices = new java.util.ArrayList<>(data.notices());
        for (String notice : additional) if (notice != null && !notice.isBlank()) notices.add(notice);
        return List.copyOf(notices);
    }
}
