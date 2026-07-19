package io.github.zaraporsche911cloud.reportingassistant.controller;

import io.github.zaraporsche911cloud.reportingassistant.dto.assistant.AssistantDtos;
import io.github.zaraporsche911cloud.reportingassistant.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    private final AssistantService service;

    public AssistantController(AssistantService service) { this.service = service; }

    @PostMapping("/query")
    public AssistantDtos.Response query(@Valid @RequestBody AssistantDtos.QueryRequest request) {
        return service.query(request);
    }
}
