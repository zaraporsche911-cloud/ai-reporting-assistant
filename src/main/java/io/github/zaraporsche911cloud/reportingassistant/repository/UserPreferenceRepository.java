package io.github.zaraporsche911cloud.reportingassistant.repository;

import io.github.zaraporsche911cloud.reportingassistant.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUserId(Long userId);
}
