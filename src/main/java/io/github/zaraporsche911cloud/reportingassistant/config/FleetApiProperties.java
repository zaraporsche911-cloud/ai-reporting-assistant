package io.github.zaraporsche911cloud.reportingassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.fleet")
public record FleetApiProperties(
        Mode mode,
        String baseUrl,
        String username,
        String password,
        String token,
        Duration connectTimeout,
        Duration readTimeout
) {
    public FleetApiProperties {
        mode = mode == null ? Mode.MOCK : mode;
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(3) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(10) : readTimeout;
        if (mode == Mode.LIVE && (baseUrl == null || baseUrl.isBlank())) {
            throw new IllegalArgumentException("app.fleet.base-url is required in LIVE mode");
        }
    }

    public enum Mode {
        LIVE,
        MOCK
    }
}
