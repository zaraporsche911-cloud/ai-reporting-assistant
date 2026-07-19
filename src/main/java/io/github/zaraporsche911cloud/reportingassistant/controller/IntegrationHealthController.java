package io.github.zaraporsche911cloud.reportingassistant.controller;

import io.github.zaraporsche911cloud.reportingassistant.service.AdministrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class IntegrationHealthController {

    private final AdministrationService administration;

    public IntegrationHealthController(AdministrationService administration) {
        this.administration = administration;
    }

    @GetMapping("/fleet-api")
    public AdministrationService.FleetStatus fleetApi() { return administration.fleetStatus(); }
}
