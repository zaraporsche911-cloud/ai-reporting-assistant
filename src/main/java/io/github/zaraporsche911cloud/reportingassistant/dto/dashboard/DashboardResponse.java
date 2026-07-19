package io.github.zaraporsche911cloud.reportingassistant.dto.dashboard;

import io.github.zaraporsche911cloud.reportingassistant.dto.conversation.ConversationDtos;
import io.github.zaraporsche911cloud.reportingassistant.dto.report.ReportDtos;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        String fleetMode,
        FleetKpis fleet,
        UsageKpis usage,
        List<ReportDtos.GeneratedResponse> recentReports,
        List<ReportDtos.SavedResponse> pinnedReports,
        List<ConversationDtos.Response> recentConversations,
        List<Map<String, Object>> reportUsageByType,
        List<String> quickQuestions
) {
    public record FleetKpis(
            long totalVehicles,
            long activeVehicles,
            long vehiclesInMaintenance,
            long activeDrivers,
            long openAnomalies,
            long criticalOpenAnomalies
    ) {
    }

    public record UsageKpis(long generatedReports, long successfulReports, long savedReports, long conversations) {
    }
}
