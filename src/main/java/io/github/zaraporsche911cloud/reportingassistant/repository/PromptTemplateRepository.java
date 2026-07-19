package io.github.zaraporsche911cloud.reportingassistant.repository;

import io.github.zaraporsche911cloud.reportingassistant.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {
    Optional<PromptTemplate> findByKeyAndEnabledTrue(String key);
}
