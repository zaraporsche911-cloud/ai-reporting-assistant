package io.github.zaraporsche911cloud.reportingassistant.ai;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("aiProvider")
public class AiProviderHealthIndicator implements HealthIndicator {

    private final AiProviderRouter router;

    public AiProviderHealthIndicator(AiProviderRouter router) {
        this.router = router;
    }

    @Override
    public Health health() {
        try {
            AiProvider provider = router.active();
            AiProvider.ProviderHealth status = provider.health();
            return Health.up().withDetail("provider", provider.id()).withDetail("model", status.model())
                    .withDetail("message", status.message()).build();
        } catch (Exception exception) {
            return Health.down().withDetail("provider", router.activeId()).withDetail("message", exception.getMessage()).build();
        }
    }
}
