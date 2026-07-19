package io.github.zaraporsche911cloud.reportingassistant.service;

import io.github.zaraporsche911cloud.reportingassistant.conversation.ConversationService;
import io.github.zaraporsche911cloud.reportingassistant.dto.dashboard.DashboardResponse;
import io.github.zaraporsche911cloud.reportingassistant.entity.AppUser;
import io.github.zaraporsche911cloud.reportingassistant.entity.ReportExecutionStatus;
import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetDataSnapshot;
import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetOperationsGateway;
import io.github.zaraporsche911cloud.reportingassistant.mapper.ReportMapper;
import io.github.zaraporsche911cloud.reportingassistant.repository.ConversationRepository;
import io.github.zaraporsche911cloud.reportingassistant.repository.GeneratedReportRepository;
import io.github.zaraporsche911cloud.reportingassistant.repository.SavedReportRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final CurrentUserService currentUserService;
    private final FleetOperationsGateway fleetGateway;
    private final GeneratedReportRepository reports;
    private final SavedReportRepository savedReports;
    private final ConversationRepository conversations;
    private final ReportMapper reportMapper;
    private final ConversationService conversationService;

    public DashboardService(
            CurrentUserService currentUserService,
            FleetOperationsGateway fleetGateway,
            GeneratedReportRepository reports,
            SavedReportRepository savedReports,
            ConversationRepository conversations,
            ReportMapper reportMapper,
            ConversationService conversationService
    ) {
        this.currentUserService = currentUserService;
        this.fleetGateway = fleetGateway;
        this.reports = reports;
        this.savedReports = savedReports;
        this.conversations = conversations;
        this.reportMapper = reportMapper;
        this.conversationService = conversationService;
    }

    public DashboardResponse get() {
        AppUser user = currentUserService.requireCurrentUser();
        FleetDataSnapshot snapshot = fleetGateway.loadSnapshot();
        FleetDataSnapshot.Overview overview = snapshot.overview();
        List<io.github.zaraporsche911cloud.reportingassistant.entity.GeneratedReport> recent =
                reports.findTop5ByUserIdOrderByCreatedAtDesc(user.getId());
        Map<String, Long> usage = recent.stream().collect(Collectors.groupingBy(
                report -> report.getReportType().name(), LinkedHashMap::new, Collectors.counting()));
        List<Map<String, Object>> usageRows = usage.entrySet().stream().map(entry -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("reportType", entry.getKey());
            row.put("count", entry.getValue());
            return row;
        }).toList();
        var recentConversations = conversations.findByUserId(user.getId(),
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "updatedAt"))).getContent();
        return new DashboardResponse(
                fleetGateway.mode(),
                new DashboardResponse.FleetKpis(
                        overview.totalVehicles(), overview.activeVehicles(), overview.vehiclesInMaintenance(), overview.activeDrivers(),
                        overview.openAnomalies(), overview.criticalOpenAnomalies()),
                new DashboardResponse.UsageKpis(
                        reports.countByUserId(user.getId()), reports.countByUserIdAndStatus(user.getId(), ReportExecutionStatus.SUCCEEDED),
                        savedReports.countByUserId(user.getId()), conversations.countByUserId(user.getId())),
                recent.stream().map(reportMapper::toResponse).toList(),
                savedReports.findTop5ByUserIdAndPinnedTrueOrderByUpdatedAtDesc(user.getId()).stream().map(reportMapper::toResponse).toList(),
                recentConversations.stream().map(conversationService::response).toList(),
                usageRows,
                List.of(
                        "Summarize fleet activity for the current month",
                        "Which vehicles consumed the most fuel this month?",
                        "Show critical anomalies from the last 30 days",
                        "Which vehicles have overdue maintenance?"));
    }
}
