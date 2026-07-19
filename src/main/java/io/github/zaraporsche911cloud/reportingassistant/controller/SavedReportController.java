package io.github.zaraporsche911cloud.reportingassistant.controller;

import io.github.zaraporsche911cloud.reportingassistant.dto.common.PageResponse;
import io.github.zaraporsche911cloud.reportingassistant.dto.report.ReportDtos;
import io.github.zaraporsche911cloud.reportingassistant.service.SavedReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/saved-reports")
public class SavedReportController {

    private final SavedReportService service;

    public SavedReportController(SavedReportService service) { this.service = service; }

    @GetMapping
    public PageResponse<ReportDtos.SavedResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) { return service.list(page, size); }

    @GetMapping("/{id}")
    public ReportDtos.SavedResponse get(@PathVariable Long id) { return service.get(id); }

    @PutMapping("/{id}")
    public ReportDtos.SavedResponse update(@PathVariable Long id, @Valid @RequestBody ReportDtos.SavedUpdateRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/duplicate")
    public ReportDtos.SavedResponse duplicate(@PathVariable Long id) { return service.duplicate(id); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
