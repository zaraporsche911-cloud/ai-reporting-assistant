package io.github.zaraporsche911cloud.reportingassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        String jwtSecret,
        Duration accessTokenTtl,
        List<String> allowedOrigins,
        int bcryptStrength
) {
    public SecurityProperties {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("app.security.jwt-secret must contain at least 32 bytes");
        }
        if (accessTokenTtl == null || accessTokenTtl.isZero() || accessTokenTtl.isNegative()) {
            throw new IllegalArgumentException("app.security.access-token-ttl must be positive");
        }
        if (bcryptStrength < 10 || bcryptStrength > 16) {
            throw new IllegalArgumentException("app.security.bcrypt-strength must be between 10 and 16");
        }
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }
}
