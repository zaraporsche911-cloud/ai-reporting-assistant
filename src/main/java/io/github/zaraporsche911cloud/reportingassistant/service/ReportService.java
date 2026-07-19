package io.github.zaraporsche911cloud.reportingassistant.service;

import io.github.zaraporsche911cloud.reportingassistant.ai.AiOrchestrator;
import io.github.zaraporsche911cloud.reportingassistant.ai.AiTextResult;
import io.github.zaraporsche911cloud.reportingassistant.audit.AuditService;
import io.github.zaraporsche911cloud.reportingassistant.dto.common.PageResponse;
import io.github.zaraporsche911cloud.reportingassistant.dto.report.ReportDtos;
import io.github.zaraporsche911cloud.reportingassistant.entity.AppUser;
import io.github.zaraporsche911cloud.reportingassistant.entity.GeneratedReport;
import io.github.zaraporsche911cloud.reportingassistant.entity.ReportExecutionStatus;
import io.github.zaraporsche911cloud.reportingassistant.entity.UserRole;
import io.github.zaraporsche911cloud.reportingassistant.exception.ResourceNotFoundException;
import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetOperationsGateway;
import io.github.zaraporsche911cloud.reportingassistant.mapper.ReportMapper;
import io.github.zaraporsche911cloud.reportingassistant.report.ReportCatalogue;
import io.github.zaraporsche911cloud.reportingassistant.report.ReportDefinition;
import io.github.zaraporsche911cloud.reportingassistant.report.engine.ReportingEngine;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportIntent;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import io.github.zaraporsche911cloud.reportingassistant.repository.GeneratedReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final GeneratedReportRepository reports;
    private final CurrentUserService currentUserService;
    private final PageRequestFactory pageRequestFactory;
    private final ReportMapper mapper;
    private final ReportCatalogue catalogue;
    private final FleetOperationsGateway fleetGateway;
    private final ReportingEngine engine;
    private final AiOrchestrator ai;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public ReportService(
            GeneratedReportRepository reports,
            CurrentUserService currentUserService,
            PageRequestFactory pageRequestFactory,
            ReportMapper mapper,
            ReportCatalogue catalogue,
            FleetOperationsGateway fleetGateway,
            ReportingEngine engine,
            AiOrchestrator ai,
            ObjectMapper objectMapper,
            AuditService auditService
    ) {
        this.reports = reports;
        this.currentUserService = currentUserService;
        this.pageRequestFactory = pageRequestFactory;
        this.mapper = mapper;
        this.catalogue = catalogue;
        this.fleetGateway = fleetGateway;
        this.engine = engine;
        this.ai = ai;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    public List<ReportDtos.CatalogueResponse> catalogue() {
        return catalogue.list().stream().map(this::catalogueResponse).toList();
    }

    public ReportDtos.GeneratedResponse get(Long id) {
        return mapper.toResponse(requireVisible(id));
    }

    public GeneratedReport requireVisible(Long id) {
        AppUser user = currentUserService.requireCurrentUser();
        if (user.getRole() == UserRole.ADMIN) {
            return reports.findById(id).orElseThrow(() -> new ResourceNotFoundException("Generated report", id));
        }
        return reports.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Generated report", id));
    }

    public PageResponse<ReportDtos.GeneratedResponse> history(
            ReportType type,
            ReportExecutionStatus status,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        AppUser user = currentUserService.requireCurrentUser();
        var pageable = pageRequestFactory.create(page, size, "createdAt", Sort.Direction.DESC, Set.of("createdAt"));
        Page<GeneratedReport> result;
        if (type != null) result = reports.findByUserIdAndReportType(user.getId(), type, pageable);
        else if (status != null) result = reports.findByUserIdAndStatus(user.getId(), status, pageable);
        else if (from != null || to != null) {
            Instant start = (from == null ? LocalDate.of(2000, 1, 1) : from).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant end = (to == null ? LocalDate.now() : to.plusDays(1)).atStartOfDay().toInstant(ZoneOffset.UTC);
            result = reports.findByUserIdAndCreatedAtBetween(user.getId(), start, end, pageable);
        } else result = reports.findByUserId(user.getId(), pageable);
        return PageResponse.from(result.map(mapper::toResponse));
    }

    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','OPERATIONS_MANAGER','ANALYST')")
    public ReportDtos.GeneratedResponse rerun(Long id) {
        AppUser user = currentUserService.requireCurrentUser();
        GeneratedReport source = requireVisible(id);
        try {
            ReportIntent intent = objectMapper.readValue(source.getIntentJson(), ReportIntent.class);
            long started = System.nanoTime();
            ReportingEngine.ExecutedReport executed = engine.execute(intent, user.getRole());
            AiTextResult summary = ai.summarize(source.getQuestion(), executed.result());
            GeneratedReport report = reports.save(new GeneratedReport(
                    user, source.getConversation(), source.getQuestion(), executed.intent().reportType(),
                    objectMapper.writeValueAsString(executed.intent()), objectMapper.writeValueAsString(executed.result()), summary.content(),
                    ReportExecutionStatus.SUCCEEDED, Math.max(1, (System.nanoTime() - started) / 1_000_000),
                    summary.provider(), summary.model(), null));
            auditService.record(user.getEmail(), "REPORT_RERUN", "GENERATED_REPORT", report.getId(), "Source: " + id);
            return mapper.toResponse(report);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to rerun stored report", exception);
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','OPERATIONS_MANAGER','ANALYST')")
    public ReportDtos.GeneratedResponse regenerateSummary(Long id) {
        AppUser user = currentUserService.requireCurrentUser();
        GeneratedReport report = requireVisible(id);
        ReportDtos.GeneratedResponse response = mapper.toResponse(report);
        if (response.result() == null) throw new IllegalArgumentException("A failed report has no result to summarize");
        AiTextResult summary = ai.summarize(report.getQuestion(), response.result());
        report.replaceSummary(summary.content(), summary.provider(), summary.model());
        auditService.record(user.getEmail(), "REPORT_SUMMARY_REGENERATED", "GENERATED_REPORT", id, null);
        return mapper.toResponse(report);
    }

    private ReportDtos.CatalogueResponse catalogueResponse(ReportDefinition definition) {
        boolean available = fleetGateway.capabilities().containsAll(definition.requiredCapabilities());
        return new ReportDtos.CatalogueResponse(
                definition.type(), definition.displayName(), definition.description(),
                definition.requiredCapabilities().stream().map(Enum::name).sorted().toList(), definition.maximumResultSize(),
                definition.defaultVisualization().name(), available,
                available ? "Available in " + fleetGateway.mode() + " mode" : "Requires source data not exposed in " + fleetGateway.mode() + " mode");
    }
}
