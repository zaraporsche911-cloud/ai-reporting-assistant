package io.github.zaraporsche911cloud.reportingassistant.entity;

public enum UserRole {
    ADMIN,
    FLEET_MANAGER,
    OPERATIONS_MANAGER,
    ANALYST,
    VIEWER;

    public boolean canGenerateReports() {
        return this != VIEWER;
    }
}
