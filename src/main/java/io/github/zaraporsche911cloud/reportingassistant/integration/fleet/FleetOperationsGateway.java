package io.github.zaraporsche911cloud.reportingassistant.integration.fleet;

import io.github.zaraporsche911cloud.reportingassistant.report.FleetCapability;

import java.util.Set;

public interface FleetOperationsGateway {

    FleetDataSnapshot loadSnapshot();

    Set<FleetCapability> capabilities();

    GatewayHealth health();

    String mode();

    record GatewayHealth(boolean available, String message) {
    }
}
