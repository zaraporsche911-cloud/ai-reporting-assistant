package io.github.zaraporsche911cloud.reportingassistant.dto.auth;

import io.github.zaraporsche911cloud.reportingassistant.entity.UserRole;

import java.time.Instant;

public record AuthResponse(String accessToken, String tokenType, Instant expiresAt, UserSummary user) {
    public record UserSummary(Long id, String fullName, String email, UserRole role, boolean enabled) {
    }
}
