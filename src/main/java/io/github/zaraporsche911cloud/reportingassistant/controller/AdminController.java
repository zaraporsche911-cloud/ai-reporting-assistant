package io.github.zaraporsche911cloud.reportingassistant.controller;

import io.github.zaraporsche911cloud.reportingassistant.ai.AiProviderRouter;
import io.github.zaraporsche911cloud.reportingassistant.audit.AuditService;
import io.github.zaraporsche911cloud.reportingassistant.dto.common.PageResponse;
import io.github.zaraporsche911cloud.reportingassistant.service.AdministrationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdministrationService administration;
    private final AuditService auditService;

    public AdminController(AdministrationService administration, AuditService auditService) {
        this.administration = administration;
        this.auditService = auditService;
    }

    @GetMapping("/ai-providers")
    public List<AiProviderRouter.ProviderStatus> providers() { return administration.providerStatuses(); }

    @PutMapping("/ai-providers/active")
    public AiProviderRouter.ProviderStatus changeProvider(@Valid @RequestBody AdministrationService.ProviderRequest request) {
        return administration.changeProvider(request.providerId());
    }

    @GetMapping("/prompt-templates")
    public List<AdministrationService.PromptResponse> prompts() { return administration.promptTemplates(); }

    @PutMapping("/prompt-templates/{id}")
    public AdministrationService.PromptResponse updatePrompt(
            @PathVariable Long id,
            @Valid @RequestBody AdministrationService.PromptUpdateRequest request
    ) { return administration.updatePrompt(id, request); }

    @GetMapping("/audit-logs")
    public PageResponse<AuditService.AuditResponse> auditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) { return auditService.list(action, page, size); }

    @GetMapping("/system-status")
    public AdministrationService.SystemStatus systemStatus() { return administration.systemStatus(); }
}
