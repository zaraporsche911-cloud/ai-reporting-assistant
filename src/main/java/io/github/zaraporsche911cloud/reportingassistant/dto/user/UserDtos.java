package io.github.zaraporsche911cloud.reportingassistant.dto.user;

import io.github.zaraporsche911cloud.reportingassistant.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class UserDtos {

    private UserDtos() {
    }

    public record CreateRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 12, max = 72) String password,
            @NotNull UserRole role
    ) {
    }

    public record UpdateRequest(@NotNull UserRole role, boolean enabled) {
    }

    public record ProfileRequest(@NotBlank @Size(max = 120) String fullName) {
    }

    public record PreferenceRequest(boolean darkMode, @NotBlank @Size(max = 64) String timezone) {
    }

    public record Response(
            Long id,
            String fullName,
            String email,
            UserRole role,
            boolean enabled,
            Instant createdAt
    ) {
    }

    public record PreferenceResponse(boolean darkMode, String timezone) {
    }
}
