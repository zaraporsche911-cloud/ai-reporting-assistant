package io.github.zaraporsche911cloud.reportingassistant.service;

import io.github.zaraporsche911cloud.reportingassistant.audit.AuditService;
import io.github.zaraporsche911cloud.reportingassistant.dto.common.PageResponse;
import io.github.zaraporsche911cloud.reportingassistant.dto.report.ReportDtos;
import io.github.zaraporsche911cloud.reportingassistant.entity.AppUser;
import io.github.zaraporsche911cloud.reportingassistant.entity.GeneratedReport;
import io.github.zaraporsche911cloud.reportingassistant.entity.SavedReport;
import io.github.zaraporsche911cloud.reportingassistant.entity.UserRole;
import io.github.zaraporsche911cloud.reportingassistant.exception.ResourceNotFoundException;
import io.github.zaraporsche911cloud.reportingassistant.mapper.ReportMapper;
import io.github.zaraporsche911cloud.reportingassistant.repository.SavedReportRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SavedReportService {

    private final SavedReportRepository savedReports;
    private final CurrentUserService currentUserService;
    private final ReportService reportService;
    private final ReportMapper mapper;
    private final AuditService auditService;

    public SavedReportService(
            SavedReportRepository savedReports,
            CurrentUserService currentUserService,
            ReportService reportService,
            ReportMapper mapper,
            AuditService auditService
    ) {
        this.savedReports = savedReports;
        this.currentUserService = currentUserService;
        this.reportService = reportService;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    public PageResponse<ReportDtos.SavedResponse> list(int page, int size) {
        if (page < 0) throw new IllegalArgumentException("page must not be negative");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
        AppUser user = currentUserService.requireCurrentUser();
        return PageResponse.from(savedReports.findByUserIdOrSharedInternallyTrue(
                user.getId(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"))).map(mapper::toResponse));
    }

    public ReportDtos.SavedResponse get(Long id) {
        return mapper.toResponse(requireVisible(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','OPERATIONS_MANAGER','ANALYST')")
    public ReportDtos.SavedResponse save(Long reportId, ReportDtos.SaveRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        GeneratedReport report = reportService.requireVisible(reportId);
        SavedReport saved = savedReports.save(new SavedReport(user, report, request.title(), request.description(), request.tags()));
        auditService.record(user.getEmail(), "REPORT_SAVED", "SAVED_REPORT", saved.getId(), "Generated report: " + reportId);
        return mapper.toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','OPERATIONS_MANAGER','ANALYST')")
    public ReportDtos.SavedResponse update(Long id, ReportDtos.SavedUpdateRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        SavedReport saved = requireOwnedOrAdmin(id, user);
        saved.updateMetadata(request.title(), request.description(), request.tags(), request.favorite(), request.pinned(), request.sharedInternally());
        auditService.record(user.getEmail(), "SAVED_REPORT_UPDATED", "SAVED_REPORT", id, null);
        return mapper.toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','OPERATIONS_MANAGER','ANALYST')")
    public ReportDtos.SavedResponse duplicate(Long id) {
        AppUser user = currentUserService.requireCurrentUser();
        SavedReport source = requireVisible(id);
        SavedReport duplicate = savedReports.save(new SavedReport(
                user, source.getGeneratedReport(), source.getTitle() + " copy", source.getDescription(), source.getTags()));
        auditService.record(user.getEmail(), "SAVED_REPORT_DUPLICATED", "SAVED_REPORT", duplicate.getId(), "Source: " + id);
        return mapper.toResponse(duplicate);
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = currentUserService.requireCurrentUser();
        SavedReport saved = requireOwnedOrAdmin(id, user);
        savedReports.delete(saved);
        auditService.record(user.getEmail(), "SAVED_REPORT_DELETED", "SAVED_REPORT", id, null);
    }

    private SavedReport requireVisible(Long id) {
        AppUser user = currentUserService.requireCurrentUser();
        SavedReport saved = savedReports.findById(id).orElseThrow(() -> new ResourceNotFoundException("Saved report", id));
        if (saved.getUser().getId().equals(user.getId()) || saved.isSharedInternally() || user.getRole() == UserRole.ADMIN) return saved;
        throw new ResourceNotFoundException("Saved report", id);
    }

    private SavedReport requireOwnedOrAdmin(Long id, AppUser user) {
        if (user.getRole() == UserRole.ADMIN) {
            return savedReports.findById(id).orElseThrow(() -> new ResourceNotFoundException("Saved report", id));
        }
        return savedReports.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Saved report", id));
    }
}
