package io.github.zaraporsche911cloud.reportingassistant.controller;

import io.github.zaraporsche911cloud.reportingassistant.dto.common.PageResponse;
import io.github.zaraporsche911cloud.reportingassistant.dto.report.ReportDtos;
import io.github.zaraporsche911cloud.reportingassistant.entity.ReportExecutionStatus;
import io.github.zaraporsche911cloud.reportingassistant.export.ExportService;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import io.github.zaraporsche911cloud.reportingassistant.service.ReportService;
import io.github.zaraporsche911cloud.reportingassistant.service.SavedReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reports;
    private final SavedReportService savedReports;
    private final ExportService exports;

    public ReportController(ReportService reports, SavedReportService savedReports, ExportService exports) {
        this.reports = reports;
        this.savedReports = savedReports;
        this.exports = exports;
    }

    @GetMapping("/catalogue")
    public List<ReportDtos.CatalogueResponse> catalogue() { return reports.catalogue(); }

    @GetMapping
    public PageResponse<ReportDtos.GeneratedResponse> history(
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) ReportExecutionStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return reports.history(type, status, from, to, page, size);
    }

    @GetMapping("/{id}")
    public ReportDtos.GeneratedResponse get(@PathVariable Long id) { return reports.get(id); }

    @PostMapping("/{id}/rerun")
    public ReportDtos.GeneratedResponse rerun(@PathVariable Long id) { return reports.rerun(id); }

    @PostMapping("/{id}/regenerate-summary")
    public ReportDtos.GeneratedResponse regenerate(@PathVariable Long id) { return reports.regenerateSummary(id); }

    @PostMapping("/{id}/save")
    public ResponseEntity<ReportDtos.SavedResponse> save(
            @PathVariable Long id,
            @Valid @RequestBody ReportDtos.SaveRequest request
    ) {
        return ResponseEntity.ok(savedReports.save(id, request));
    }

    @GetMapping("/{id}/export/csv")
    public ResponseEntity<byte[]> csv(@PathVariable Long id) { return export(exports.csv(id)); }

    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) { return export(exports.pdf(id)); }

    private ResponseEntity<byte[]> export(ExportService.ExportFile file) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, file.contentType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .body(file.content());
    }
}
