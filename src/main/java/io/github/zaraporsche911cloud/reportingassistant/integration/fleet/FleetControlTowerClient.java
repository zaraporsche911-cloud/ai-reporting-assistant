package io.github.zaraporsche911cloud.reportingassistant.integration.fleet;

import io.github.zaraporsche911cloud.reportingassistant.config.FleetApiProperties;
import io.github.zaraporsche911cloud.reportingassistant.report.FleetCapability;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(prefix = "app.fleet", name = "mode", havingValue = "LIVE")
public class FleetControlTowerClient implements FleetOperationsGateway {

    private final RestClient client;
    private final FleetApiErrorDecoder errorDecoder;

    public FleetControlTowerClient(
            FleetApiProperties properties,
            FleetApiTokenProvider tokenProvider,
            FleetApiErrorDecoder errorDecoder
    ) {
        this.errorDecoder = errorDecoder;
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.readTimeout());
        this.client = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(tokenProvider.token());
                    return execution.execute(request, body);
                })
                .build();
    }

    @Override
    public FleetDataSnapshot loadSnapshot() {
        try {
            LiveOverview overview = client.get().uri("/api/v1/dashboard/summary").retrieve().body(LiveOverview.class);
            FleetPage<LiveVehicle> vehicles = client.get()
                    .uri("/api/v1/vehicles?page=0&size=100&sortBy=createdAt&direction=DESC")
                    .retrieve().body(new ParameterizedTypeReference<>() { });
            FleetPage<LiveDriver> drivers = client.get()
                    .uri("/api/v1/drivers?page=0&size=100&sortBy=createdAt&direction=DESC")
                    .retrieve().body(new ParameterizedTypeReference<>() { });
            FleetPage<LiveAnomaly> anomalies = client.get()
                    .uri("/api/v1/anomalies?page=0&size=100&sortBy=reportedAt&direction=DESC")
                    .retrieve().body(new ParameterizedTypeReference<>() { });
            return new FleetDataSnapshot(
                    "Fleet Control Tower LIVE API",
                    overview == null ? new FleetDataSnapshot.Overview(0, 0, 0, 0, 0, 0, 0) : overview.toSnapshot(),
                    content(vehicles).stream().map(LiveVehicle::toSnapshot).toList(),
                    content(drivers).stream().map(LiveDriver::toSnapshot).toList(),
                    content(anomalies).stream().map(LiveAnomaly::toSnapshot).toList(),
                    List.of(), List.of(), List.of(), List.of(),
                    List.of("LIVE mode exposes current vehicles, drivers, anomalies, and overview data. Telemetry reports require future source API capabilities."));
        } catch (RestClientException exception) {
            throw errorDecoder.decode(exception);
        }
    }

    private static <T> List<T> content(FleetPage<T> page) {
        return page == null || page.content() == null ? List.of() : page.content();
    }

    @Override
    public Set<FleetCapability> capabilities() {
        return EnumSet.of(FleetCapability.OVERVIEW, FleetCapability.VEHICLES, FleetCapability.DRIVERS, FleetCapability.ANOMALIES);
    }

    @Override
    public GatewayHealth health() {
        try {
            client.get().uri("/api/v1/dashboard/summary").retrieve().toBodilessEntity();
            return new GatewayHealth(true, "Fleet Control Tower API is reachable");
        } catch (RestClientException exception) {
            return new GatewayHealth(false, "Fleet Control Tower API is unavailable");
        }
    }

    @Override
    public String mode() {
        return "LIVE";
    }

    private record FleetPage<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    }

    private record LiveOverview(
            long totalVehicles,
            long activeVehicles,
            long vehiclesInMaintenance,
            long totalDrivers,
            long activeDrivers,
            long openAnomalies,
            long criticalOpenAnomalies
    ) {
        FleetDataSnapshot.Overview toSnapshot() {
            return new FleetDataSnapshot.Overview(totalVehicles, activeVehicles, vehiclesInMaintenance, totalDrivers,
                    activeDrivers, openAnomalies, criticalOpenAnomalies);
        }
    }

    private record LiveVehicle(Long id, String registrationNumber, String brand, String model, String status, long mileage) {
        FleetDataSnapshot.Vehicle toSnapshot() {
            return new FleetDataSnapshot.Vehicle(id, registrationNumber, brand, model, status, mileage);
        }
    }

    private record LiveDriver(
            Long id,
            String employeeNumber,
            String firstName,
            String lastName,
            String status,
            AssignedVehicle assignedVehicle,
            LocalDate licenseExpiry
    ) {
        FleetDataSnapshot.Driver toSnapshot() {
            return new FleetDataSnapshot.Driver(id, employeeNumber, firstName + " " + lastName, status,
                    assignedVehicle == null ? null : assignedVehicle.id(), licenseExpiry);
        }
    }

    private record AssignedVehicle(Long id) {
    }

    private record LiveAnomaly(
            Long id,
            RelatedVehicle vehicle,
            RelatedDriver driver,
            String title,
            String type,
            String severity,
            String status,
            java.time.Instant reportedAt,
            java.time.Instant resolvedAt
    ) {
        FleetDataSnapshot.Anomaly toSnapshot() {
            return new FleetDataSnapshot.Anomaly(id, vehicle.id(), vehicle.registrationNumber(),
                    driver == null ? null : driver.id(), title, type, severity, status, reportedAt, resolvedAt);
        }
    }

    private record RelatedVehicle(Long id, String registrationNumber) {
    }

    private record RelatedDriver(Long id) {
    }
}
