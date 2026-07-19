package io.github.zaraporsche911cloud.reportingassistant.integration.fleet;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record FleetDataSnapshot(
        String source,
        Overview overview,
        List<Vehicle> vehicles,
        List<Driver> drivers,
        List<Anomaly> anomalies,
        List<FuelEntry> fuelEntries,
        List<Trip> trips,
        List<Maintenance> maintenance,
        List<Availability> availability,
        List<String> notices
) {
    public FleetDataSnapshot {
        vehicles = safe(vehicles);
        drivers = safe(drivers);
        anomalies = safe(anomalies);
        fuelEntries = safe(fuelEntries);
        trips = safe(trips);
        maintenance = safe(maintenance);
        availability = safe(availability);
        notices = safe(notices);
    }

    private static <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    public record Overview(
            long totalVehicles,
            long activeVehicles,
            long vehiclesInMaintenance,
            long totalDrivers,
            long activeDrivers,
            long openAnomalies,
            long criticalOpenAnomalies
    ) {
    }

    public record Vehicle(Long id, String registrationNumber, String brand, String model, String status, long mileage) {
    }

    public record Driver(
            Long id,
            String employeeNumber,
            String fullName,
            String status,
            Long assignedVehicleId,
            LocalDate licenseExpiry
    ) {
    }

    public record Anomaly(
            Long id,
            Long vehicleId,
            String vehicleRegistration,
            Long driverId,
            String title,
            String type,
            String severity,
            String status,
            Instant reportedAt,
            Instant resolvedAt
    ) {
    }

    public record FuelEntry(
            Long id,
            Long vehicleId,
            LocalDate date,
            BigDecimal liters,
            BigDecimal distanceKm,
            BigDecimal cost
    ) {
    }

    public record Trip(
            Long id,
            Long vehicleId,
            Long driverId,
            LocalDate date,
            BigDecimal distanceKm,
            BigDecimal durationHours
    ) {
    }

    public record Maintenance(
            Long id,
            Long vehicleId,
            LocalDate dueDate,
            LocalDate completedDate,
            String status,
            String description,
            BigDecimal cost
    ) {
    }

    public record Availability(
            Long vehicleId,
            LocalDate date,
            BigDecimal availableHours,
            BigDecimal unavailableHours
    ) {
    }
}
