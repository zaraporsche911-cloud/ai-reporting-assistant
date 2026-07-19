package io.github.zaraporsche911cloud.reportingassistant.service;

import io.github.zaraporsche911cloud.reportingassistant.ai.AiProviderRouter;
import io.github.zaraporsche911cloud.reportingassistant.ai.prompt.PromptTemplateService;
import io.github.zaraporsche911cloud.reportingassistant.audit.AuditService;
import io.github.zaraporsche911cloud.reportingassistant.config.AiProperties;
import io.github.zaraporsche911cloud.reportingassistant.entity.AppUser;
import io.github.zaraporsche911cloud.reportingassistant.entity.PromptTemplate;
import io.github.zaraporsche911cloud.reportingassistant.integration.fleet.FleetOperationsGateway;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

@Service
public class AdministrationService {

    private final AiProviderRouter providers;
    private final PromptTemplateService promptTemplates;
    private final FleetOperationsGateway fleetGateway;
    private final DataSource dataSource;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final AiProperties aiProperties;

    public AdministrationService(
            AiProviderRouter providers,
            PromptTemplateService promptTemplates,
            FleetOperationsGateway fleetGateway,
            DataSource dataSource,
            CurrentUserService currentUserService,
            AuditService auditService,
            AiProperties aiProperties
    ) {
        this.providers = providers;
        this.promptTemplates = promptTemplates;
        this.fleetGateway = fleetGateway;
        this.dataSource = dataSource;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
        this.aiProperties = aiProperties;
    }

    public List<AiProviderRouter.ProviderStatus> providerStatuses() {
        return providers.statuses();
    }

    public AiProviderRouter.ProviderStatus changeProvider(String id) {
        AppUser actor = currentUserService.requireCurrentUser();
        AiProviderRouter.ProviderStatus status = providers.changeActive(id);
        auditService.record(actor.getEmail(), "AI_PROVIDER_CHANGED", "CONFIGURATION", id, "Model: " + status.model());
        return status;
    }

    public List<PromptResponse> promptTemplates() {
        return promptTemplates.list().stream().map(this::promptResponse).toList();
    }

    public PromptResponse updatePrompt(Long id, PromptUpdateRequest request) {
        AppUser actor = currentUserService.requireCurrentUser();
        PromptTemplate template = promptTemplates.update(id, request.displayName(), request.content(), request.enabled());
        auditService.record(actor.getEmail(), "PROMPT_TEMPLATE_UPDATED", "PROMPT_TEMPLATE", id, "Key: " + template.getKey());
        return promptResponse(template);
    }

    public SystemStatus systemStatus() {
        FleetOperationsGateway.GatewayHealth fleet = fleetGateway.health();
        boolean database = databaseReady();
        return new SystemStatus(
                database,
                fleetGateway.mode(),
                fleet.available(),
                fleet.message(),
                providers.activeId(),
                aiProperties.model(),
                providers.statuses(),
                "Secrets are supplied through environment variables and are never returned by this endpoint");
    }

    public FleetStatus fleetStatus() {
        FleetOperationsGateway.GatewayHealth health = fleetGateway.health();
        return new FleetStatus(fleetGateway.mode(), health.available(), health.message(),
                fleetGateway.capabilities().stream().map(Enum::name).sorted().toList());
    }

    private boolean databaseReady() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception exception) {
            return false;
        }
    }

    private PromptResponse promptResponse(PromptTemplate template) {
        return new PromptResponse(template.getId(), template.getKey(), template.getDisplayName(), template.getContent(),
                template.isEnabled(), template.getUpdatedAt());
    }

    public record ProviderRequest(@NotBlank String providerId) {
    }

    public record PromptUpdateRequest(
            @NotBlank @Size(max = 120) String displayName,
            @NotBlank @Size(max = 30_000) String content,
            boolean enabled
    ) {
    }

    public record PromptResponse(
            Long id, String key, String displayName, String content, boolean enabled, java.time.Instant updatedAt
    ) {
    }

    public record SystemStatus(
            boolean databaseAvailable,
            String fleetMode,
            boolean fleetAvailable,
            String fleetMessage,
            String activeAiProvider,
            String configuredModel,
            List<AiProviderRouter.ProviderStatus> providers,
            String securityNotice
    ) {
    }

    public record FleetStatus(String mode, boolean available, String message, List<String> capabilities) {
    }
}
