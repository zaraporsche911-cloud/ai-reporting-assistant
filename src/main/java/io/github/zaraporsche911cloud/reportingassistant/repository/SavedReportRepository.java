package io.github.zaraporsche911cloud.reportingassistant.repository;

import io.github.zaraporsche911cloud.reportingassistant.entity.SavedReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedReportRepository extends JpaRepository<SavedReport, Long> {
    Optional<SavedReport> findByIdAndUserId(Long id, Long userId);
    Page<SavedReport> findByUserIdOrSharedInternallyTrue(Long userId, Pageable pageable);
    List<SavedReport> findTop5ByUserIdAndPinnedTrueOrderByUpdatedAtDesc(Long userId);
    List<SavedReport> findTop5ByUserIdOrderByUpdatedAtDesc(Long userId);
    long countByUserId(Long userId);
}
