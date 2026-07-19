package io.github.zaraporsche911cloud.reportingassistant.repository;

import io.github.zaraporsche911cloud.reportingassistant.entity.GeneratedReport;
import io.github.zaraporsche911cloud.reportingassistant.entity.ReportExecutionStatus;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, Long> {
    Optional<GeneratedReport> findByIdAndUserId(Long id, Long userId);
    Page<GeneratedReport> findByUserId(Long userId, Pageable pageable);
    Page<GeneratedReport> findByUserIdAndReportType(Long userId, ReportType reportType, Pageable pageable);
    Page<GeneratedReport> findByUserIdAndStatus(Long userId, ReportExecutionStatus status, Pageable pageable);
    Page<GeneratedReport> findByUserIdAndCreatedAtBetween(Long userId, Instant from, Instant to, Pageable pageable);
    List<GeneratedReport> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<GeneratedReport> findTopByConversationIdOrderByCreatedAtDesc(Long conversationId);
    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, ReportExecutionStatus status);
}
