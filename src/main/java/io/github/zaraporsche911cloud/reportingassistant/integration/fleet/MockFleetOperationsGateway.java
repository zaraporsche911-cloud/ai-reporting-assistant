package io.github.zaraporsche911cloud.reportingassistant.integration.fleet;

import io.github.zaraporsche911cloud.reportingassistant.report.FleetCapability;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(prefix = "app.fleet", name = "mode", havingValue = "MOCK", matchIfMissing = true)
public class MockFleetOperationsGateway implements FleetOperationsGateway {

    private final Clock clock;

    public MockFleetOperationsGateway(Clock clock) {
        this.clock = clock;
    }

    @Override
    public FleetDataSnapshot loadSnapshot() {
        LocalDate today = LocalDate.now(clock);
        List<FleetDataSnapshot.Vehicle> vehicles = vehicles();
        List<FleetDataSnapshot.Driver> drivers = drivers(today);
        List<FleetDataSnapshot.Anomaly> anomalies = anomalies(today);
        return new FleetDataSnapshot(
                "Fleet Control Tower mock adapter",
                new FleetDataSnapshot.Overview(6, 4, 1, 5, 4, 5, 2),
                vehicles,
                drivers,
                anomalies,
                fuel(today, vehicles),
                trips(today, vehicles, drivers),
                maintenance(today),
                availability(today, vehicles),
                List.of("Demonstration data from MOCK mode; no live Fleet Control Tower records were queried.")
        );
    }

    @Override
    public Set<FleetCapability> capabilities() {
        return EnumSet.allOf(FleetCapability.class);
    }

    @Override
    public GatewayHealth health() {
        return new GatewayHealth(true, "Mock Fleet integration is ready");
    }

    @Override
    public String mode() {
        return "MOCK";
    }

    private List<FleetDataSnapshot.Vehicle> vehicles() {
        return List.of(
                new FleetDataSnapshot.Vehicle(1L, "TRK-101", "Volvo", "FH16", "ACTIVE", 186_420),
                new FleetDataSnapshot.Vehicle(2L, "TRK-204", "Scania", "R500", "ACTIVE", 142_810),
                new FleetDataSnapshot.Vehicle(3L, "VAN-312", "Mercedes", "Sprinter", "MAINTENANCE", 98_540),
                new FleetDataSnapshot.Vehicle(4L, "TRK-407", "MAN", "TGX", "ACTIVE", 211_230),
                new FleetDataSnapshot.Vehicle(5L, "VAN-518", "Ford", "Transit", "INACTIVE", 123_670),
                new FleetDataSnapshot.Vehicle(6L, "TRK-629", "DAF", "XF", "ACTIVE", 167_900)
        );
    }

    private List<FleetDataSnapshot.Driver> drivers(LocalDate today) {
        return List.of(
                new FleetDataSnapshot.Driver(1L, "DRV-001", "Samira Khan", "ACTIVE", 1L, today.plusMonths(14)),
                new FleetDataSnapshot.Driver(2L, "DRV-002", "Youssef Amrani", "ACTIVE", 2L, today.plusMonths(3)),
                new FleetDataSnapshot.Driver(3L, "DRV-003", "Lina Haddad", "ACTIVE", 4L, today.plusMonths(9)),
                new FleetDataSnapshot.Driver(4L, "DRV-004", "Omar Benali", "ON_LEAVE", null, today.plusDays(24)),
                new FleetDataSnapshot.Driver(5L, "DRV-005", "Nadia El Idrissi", "ACTIVE", 6L, today.plusMonths(18))
        );
    }

    private List<FleetDataSnapshot.Anomaly> anomalies(LocalDate today) {
        return List.of(
                anomaly(1, 1, 1, "Brake pressure warning", "SAFETY", "CRITICAL", "OPEN", today.minusDays(2), null),
                anomaly(2, 3, null, "Cooling system inspection", "MECHANICAL", "HIGH", "IN_PROGRESS", today.minusDays(5), null),
                anomaly(3, 4, 3, "Route deviation", "OPERATIONAL", "MEDIUM", "OPEN", today.minusDays(9), null),
                anomaly(4, 6, 5, "Tire wear threshold", "SAFETY", "CRITICAL", "OPEN", today.minusDays(19), null),
                anomaly(5, 2, 2, "Late inspection record", "COMPLIANCE", "LOW", "OPEN", today.minusDays(35), null),
                anomaly(6, 1, 1, "Sensor calibration", "MECHANICAL", "MEDIUM", "RESOLVED", today.minusDays(68), today.minusDays(64))
        );
    }

    private FleetDataSnapshot.Anomaly anomaly(
            long id, long vehicleId, Integer driverId, String title, String type, String severity,
            String status, LocalDate reported, LocalDate resolved
    ) {
        String registration = vehicles().stream().filter(vehicle -> vehicle.id() == vehicleId)
                .findFirst().map(FleetDataSnapshot.Vehicle::registrationNumber).orElse("Unknown");
        return new FleetDataSnapshot.Anomaly(
                id,
                vehicleId,
                registration,
                driverId == null ? null : driverId.longValue(),
                title,
                type,
                severity,
                status,
                reported.atStartOfDay().toInstant(ZoneOffset.UTC),
                resolved == null ? null : resolved.atStartOfDay().toInstant(ZoneOffset.UTC)
        );
    }

    private List<FleetDataSnapshot.FuelEntry> fuel(LocalDate today, List<FleetDataSnapshot.Vehicle> vehicles) {
        List<FleetDataSnapshot.FuelEntry> result = new ArrayList<>();
        long id = 1;
        for (int monthOffset = 0; monthOffset < 7; monthOffset++) {
            LocalDate month = today.minusMonths(monthOffset).withDayOfMonth(6);
            for (FleetDataSnapshot.Vehicle vehicle : vehicles) {
                if ("INACTIVE".equals(vehicle.status())) continue;
                for (int entry = 0; entry < 2; entry++) {
                    BigDecimal distance = BigDecimal.valueOf(1180 + vehicle.id() * 145 + monthOffset * 31 + entry * 90);
                    BigDecimal efficiency = BigDecimal.valueOf(25.5 + vehicle.id() * 1.7 + (vehicle.id() == 4 ? 7.5 : 0));
                    BigDecimal liters = distance.multiply(efficiency).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal cost = liters.multiply(BigDecimal.valueOf(1.62)).setScale(2, RoundingMode.HALF_UP);
                    result.add(new FleetDataSnapshot.FuelEntry(id++, vehicle.id(), month.plusDays(entry * 12L), liters, distance, cost));
                }
            }
        }
        return result;
    }

    private List<FleetDataSnapshot.Trip> trips(
            LocalDate today,
            List<FleetDataSnapshot.Vehicle> vehicles,
            List<FleetDataSnapshot.Driver> drivers
    ) {
        List<FleetDataSnapshot.Trip> result = new ArrayList<>();
        long id = 1;
        for (int dayOffset = 1; dayOffset <= 120; dayOffset += 3) {
            for (FleetDataSnapshot.Vehicle vehicle : vehicles) {
                if (!"ACTIVE".equals(vehicle.status())) continue;
                Long driverId = drivers.stream().filter(driver -> vehicle.id().equals(driver.assignedVehicleId()))
                        .findFirst().map(FleetDataSnapshot.Driver::id).orElse(null);
                BigDecimal distance = BigDecimal.valueOf(180 + vehicle.id() * 24 + dayOffset % 43);
                result.add(new FleetDataSnapshot.Trip(
                        id++, vehicle.id(), driverId, today.minusDays(dayOffset), distance,
                        distance.divide(BigDecimal.valueOf(58), 2, RoundingMode.HALF_UP)));
            }
        }
        return result;
    }

    private List<FleetDataSnapshot.Maintenance> maintenance(LocalDate today) {
        return List.of(
                new FleetDataSnapshot.Maintenance(1L, 3L, today.minusDays(12), null, "OVERDUE", "Cooling system service", BigDecimal.valueOf(1280)),
                new FleetDataSnapshot.Maintenance(2L, 1L, today.plusDays(18), null, "SCHEDULED", "Preventive inspection", BigDecimal.valueOf(420)),
                new FleetDataSnapshot.Maintenance(3L, 4L, today.minusDays(40), today.minusDays(38), "COMPLETED", "Brake pad replacement", BigDecimal.valueOf(960)),
                new FleetDataSnapshot.Maintenance(4L, 6L, today.plusDays(7), null, "SCHEDULED", "Tire replacement", BigDecimal.valueOf(1420))
        );
    }

    private List<FleetDataSnapshot.Availability> availability(LocalDate today, List<FleetDataSnapshot.Vehicle> vehicles) {
        List<FleetDataSnapshot.Availability> result = new ArrayList<>();
        for (int dayOffset = 0; dayOffset < 90; dayOffset++) {
            for (FleetDataSnapshot.Vehicle vehicle : vehicles) {
                double unavailable = switch (vehicle.status()) {
                    case "MAINTENANCE" -> dayOffset < 14 ? 24 : 4;
                    case "INACTIVE" -> 24;
                    default -> (dayOffset + vehicle.id()) % 17 == 0 ? 5 : 0;
                };
                result.add(new FleetDataSnapshot.Availability(
                        vehicle.id(), today.minusDays(dayOffset),
                        BigDecimal.valueOf(24 - unavailable), BigDecimal.valueOf(unavailable)));
            }
        }
        return result;
    }
}
