package io.github.zaraporsche911cloud.reportingassistant.report;

import io.github.zaraporsche911cloud.reportingassistant.entity.UserRole;
import io.github.zaraporsche911cloud.reportingassistant.exception.ResourceNotFoundException;
import io.github.zaraporsche911cloud.reportingassistant.report.model.GroupingDimension;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportMetric;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import io.github.zaraporsche911cloud.reportingassistant.report.model.VisualizationType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ReportCatalogue {

    private static final Set<UserRole> AUTHORS = EnumSet.of(
            UserRole.ADMIN, UserRole.FLEET_MANAGER, UserRole.OPERATIONS_MANAGER, UserRole.ANALYST);
    private static final Set<GroupingDimension> COMMON_GROUPS = EnumSet.of(
            GroupingDimension.NONE, GroupingDimension.VEHICLE, GroupingDimension.DRIVER,
            GroupingDimension.DAY, GroupingDimension.WEEK, GroupingDimension.MONTH,
            GroupingDimension.STATUS, GroupingDimension.SEVERITY);
    private static final Set<ReportMetric> ALL_METRICS = EnumSet.allOf(ReportMetric.class);
    private static final Set<String> COMMON_SORTS = Set.of(
            "value", "vehicle", "driver", "mileage", "distance", "fuel", "efficiency",
            "cost", "count", "date", "availability", "severity", "status");

    private final Map<ReportType, ReportDefinition> definitions = new EnumMap<>(ReportType.class);

    public ReportCatalogue() {
        add(ReportType.FLEET_OVERVIEW, "Fleet overview", "Current fleet readiness and risk KPIs",
                Set.of(FleetCapability.OVERVIEW), VisualizationType.KPI, "overview");
        add(ReportType.FUEL_CONSUMPTION, "Fuel consumption", "Fuel volume by vehicle and period",
                Set.of(FleetCapability.FUEL, FleetCapability.VEHICLES), VisualizationType.BAR_CHART, "telemetry");
        add(ReportType.FUEL_EFFICIENCY, "Fuel efficiency", "Distance-normalized fuel efficiency",
                Set.of(FleetCapability.FUEL, FleetCapability.VEHICLES), VisualizationType.BAR_CHART, "telemetry");
        add(ReportType.MILEAGE, "Vehicle mileage", "Current recorded odometer values",
                Set.of(FleetCapability.VEHICLES), VisualizationType.BAR_CHART, "vehicle");
        add(ReportType.DRIVER_PERFORMANCE, "Driver performance", "Distance and trip activity by driver",
                Set.of(FleetCapability.TRIPS, FleetCapability.DRIVERS), VisualizationType.BAR_CHART, "driver");
        add(ReportType.VEHICLE_AVAILABILITY, "Vehicle availability", "Available and unavailable operating hours",
                Set.of(FleetCapability.AVAILABILITY, FleetCapability.VEHICLES), VisualizationType.BAR_CHART, "telemetry");
        add(ReportType.MAINTENANCE_STATUS, "Maintenance status", "Current maintenance schedule and completion state",
                Set.of(FleetCapability.MAINTENANCE, FleetCapability.VEHICLES), VisualizationType.TABLE, "telemetry");
        add(ReportType.OVERDUE_MAINTENANCE, "Overdue maintenance", "Maintenance items beyond their due date",
                Set.of(FleetCapability.MAINTENANCE, FleetCapability.VEHICLES), VisualizationType.TABLE, "telemetry");
        add(ReportType.ANOMALIES, "Operational anomalies", "Anomalies by status, severity, and vehicle",
                Set.of(FleetCapability.ANOMALIES), VisualizationType.TABLE, "anomaly");
        add(ReportType.CRITICAL_ANOMALIES, "Critical anomalies", "High-priority unresolved operational risks",
                Set.of(FleetCapability.ANOMALIES), VisualizationType.TABLE, "anomaly");
        add(ReportType.TRIPS, "Trip activity", "Trip count, distance, and duration",
                Set.of(FleetCapability.TRIPS, FleetCapability.VEHICLES), VisualizationType.BAR_CHART, "telemetry");
        add(ReportType.OPERATING_COSTS, "Operating costs", "Fuel and maintenance cost by vehicle",
                Set.of(FleetCapability.COSTS, FleetCapability.VEHICLES), VisualizationType.BAR_CHART, "telemetry");
        add(ReportType.VEHICLE_UTILIZATION, "Vehicle utilization", "Distance and availability utilization indicators",
                Set.of(FleetCapability.TRIPS, FleetCapability.AVAILABILITY, FleetCapability.VEHICLES), VisualizationType.BAR_CHART, "telemetry");
        add(ReportType.MONTHLY_COMPARISON, "Monthly comparison", "Compare fleet metrics across months",
                Set.of(FleetCapability.FUEL, FleetCapability.TRIPS), VisualizationType.LINE_CHART, "telemetry");
        add(ReportType.PERIOD_COMPARISON, "Period comparison", "Compare two requested periods",
                Set.of(FleetCapability.FUEL, FleetCapability.TRIPS), VisualizationType.BAR_CHART, "telemetry");
        add(ReportType.TOP_VEHICLES, "Top vehicles", "Rank vehicles by current recorded mileage",
                Set.of(FleetCapability.VEHICLES), VisualizationType.BAR_CHART, "vehicle");
        add(ReportType.TOP_DRIVERS, "Top drivers", "Rank drivers by recorded trip distance",
                Set.of(FleetCapability.TRIPS, FleetCapability.DRIVERS), VisualizationType.BAR_CHART, "driver");
        add(ReportType.TREND_ANALYSIS, "Anomaly trend", "Operational anomaly volume over time",
                Set.of(FleetCapability.ANOMALIES), VisualizationType.LINE_CHART, "anomaly");
    }

    public ReportDefinition get(ReportType type) {
        ReportDefinition definition = definitions.get(type);
        if (definition == null) throw new ResourceNotFoundException("Report definition", type);
        return definition;
    }

    public List<ReportDefinition> list() {
        return List.copyOf(definitions.values());
    }

    private void add(
            ReportType type,
            String name,
            String description,
            Set<FleetCapability> capabilities,
            VisualizationType visualization,
            String executor
    ) {
        definitions.put(type, new ReportDefinition(
                type, name, description, capabilities, ALL_METRICS, COMMON_GROUPS, COMMON_SORTS,
                100, AUTHORS, visualization, executor));
    }
}
