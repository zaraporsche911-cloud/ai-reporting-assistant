package io.github.zaraporsche911cloud.reportingassistant.integration.fleet;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("fleetControlTower")
public class FleetApiHealthIndicator implements HealthIndicator {

    private final FleetOperationsGateway gateway;

    public FleetApiHealthIndicator(FleetOperationsGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public Health health() {
        FleetOperationsGateway.GatewayHealth status = gateway.health();
        Health.Builder builder = status.available() ? Health.up() : Health.down();
        return builder.withDetail("mode", gateway.mode()).withDetail("message", status.message()).build();
    }
}
