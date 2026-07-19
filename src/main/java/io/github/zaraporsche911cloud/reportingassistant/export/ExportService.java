package io.github.zaraporsche911cloud.reportingassistant.export;

import io.github.zaraporsche911cloud.reportingassistant.audit.AuditService;
import io.github.zaraporsche911cloud.reportingassistant.dto.report.ReportDtos;
import io.github.zaraporsche911cloud.reportingassistant.entity.AppUser;
import io.github.zaraporsche911cloud.reportingassistant.mapper.ReportMapper;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportResult;
import io.github.zaraporsche911cloud.reportingassistant.service.CurrentUserService;
import io.github.zaraporsche911cloud.reportingassistant.service.ReportService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExportService {

    private final ReportService reportService;
    private final ReportMapper reportMapper;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final Clock clock;

    public ExportService(
            ReportService reportService,
            ReportMapper reportMapper,
            CurrentUserService currentUserService,
            AuditService auditService,
            Clock clock
    ) {
        this.reportService = reportService;
        this.reportMapper = reportMapper;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','OPERATIONS_MANAGER','ANALYST')")
    public ExportFile csv(Long reportId) {
        AppUser user = currentUserService.requireCurrentUser();
        ReportDtos.GeneratedResponse report = requireResult(reportId);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("FleetOps AI Reporting Assistant\r\n");
        csv.append("Report,").append(cell(report.result().title())).append("\r\n");
        csv.append("Generated,").append(cell(String.valueOf(LocalDate.now(clock)))).append("\r\n");
        csv.append("Question,").append(cell(report.question())).append("\r\n");
        csv.append("Date range,").append(cell(report.result().from() + " to " + report.result().to())).append("\r\n");
        csv.append("Data source,").append(cell(report.result().dataSource())).append("\r\n\r\n");
        List<ReportResult.ReportColumn> columns = report.result().columns();
        csv.append(columns.stream().map(column -> cell(column.label() + (column.unit() == null ? "" : " (" + column.unit() + ")")))
                .reduce((left, right) -> left + "," + right).orElse("")).append("\r\n");
        for (Map<String, Object> row : report.result().rows()) {
            csv.append(columns.stream().map(column -> cell(String.valueOf(row.getOrDefault(column.key(), ""))))
                    .reduce((left, right) -> left + "," + right).orElse("")).append("\r\n");
        }
        auditService.record(user.getEmail(), "REPORT_EXPORTED_CSV", "GENERATED_REPORT", reportId, null);
        return new ExportFile(filename(report, "csv"), "text/csv; charset=UTF-8", csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','OPERATIONS_MANAGER','ANALYST')")
    public ExportFile pdf(Long reportId) {
        AppUser user = currentUserService.requireCurrentUser();
        ReportDtos.GeneratedResponse report = requireResult(reportId);
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(document);
            writer.heading("FLEETOPS · AI REPORTING ASSISTANT");
            writer.title(report.result().title());
            writer.line("Generated: " + LocalDate.now(clock));
            writer.line("Date range: " + report.result().from() + " to " + report.result().to());
            writer.line("Data source: " + report.result().dataSource());
            writer.space();
            writer.subheading("Requested question");
            writer.paragraph(report.question());
            writer.subheading("Business summary");
            writer.paragraph(report.summary());
            writer.subheading("Key metrics");
            for (ReportResult.KpiValue kpi : report.result().kpis()) {
                writer.line(kpi.label() + ": " + kpi.value() + (kpi.unit() == null ? "" : " " + kpi.unit()));
            }
            writer.subheading("Report table");
            List<ReportResult.ReportColumn> columns = report.result().columns().stream().limit(5).toList();
            writer.line(joinColumns(columns.stream().map(ReportResult.ReportColumn::label).toList()));
            for (Map<String, Object> row : report.result().rows()) {
                writer.line(joinColumns(columns.stream().map(column -> String.valueOf(row.getOrDefault(column.key(), ""))).toList()));
            }
            if (!report.result().notices().isEmpty()) {
                writer.subheading("Data notes");
                for (String notice : report.result().notices()) writer.paragraph("• " + notice);
            }
            writer.finish();
            document.save(output);
            auditService.record(user.getEmail(), "REPORT_EXPORTED_PDF", "GENERATED_REPORT", reportId, null);
            return new ExportFile(filename(report, "pdf"), "application/pdf", output.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("PDF export failed", exception);
        }
    }

    private ReportDtos.GeneratedResponse requireResult(Long reportId) {
        ReportDtos.GeneratedResponse report = reportMapper.toResponse(reportService.requireVisible(reportId));
        if (report.result() == null) throw new IllegalArgumentException("Failed reports cannot be exported");
        return report;
    }

    private String cell(String raw) {
        String value = raw == null ? "" : raw;
        if (!value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0) value = "'" + value;
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private String filename(ReportDtos.GeneratedResponse report, String extension) {
        String base = report.reportType().name().toLowerCase().replace('_', '-');
        return "fleetops-" + base + "-" + LocalDate.now(clock) + "." + extension;
    }

    private String joinColumns(List<String> values) {
        return values.stream().map(value -> truncate(value, 22)).reduce((left, right) -> left + " | " + right).orElse("");
    }

    private String truncate(String value, int length) {
        if (value == null) return "";
        return value.length() <= length ? value : value.substring(0, length - 1) + "…";
    }

    public record ExportFile(String filename, String contentType, byte[] content) {
    }

    private static final class PdfWriter {
        private static final float MARGIN = 48;
        private static final float LINE_HEIGHT = 15;
        private final PDDocument document;
        private final PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private final List<PDPage> pages = new ArrayList<>();
        private PDPage page;
        private PDPageContentStream stream;
        private float y;

        PdfWriter(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        void heading(String value) throws IOException { text(value, bold, 10, 18); }
        void title(String value) throws IOException { text(value, bold, 20, 30); }
        void subheading(String value) throws IOException { space(); text(value, bold, 12, 20); }
        void line(String value) throws IOException { text(value, regular, 9, LINE_HEIGHT); }

        void paragraph(String value) throws IOException {
            if (value == null) return;
            String[] words = value.replace('\n', ' ').split("\\s+");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                if (line.length() + word.length() > 105) {
                    line(line.toString());
                    line.setLength(0);
                }
                if (!line.isEmpty()) line.append(' ');
                line.append(word);
            }
            if (!line.isEmpty()) line(line.toString());
        }

        void space() { y -= 8; }

        void finish() throws IOException {
            stream.close();
            int total = pages.size();
            for (int index = 0; index < total; index++) {
                try (PDPageContentStream footer = new PDPageContentStream(
                        document, pages.get(index), PDPageContentStream.AppendMode.APPEND, true)) {
                    footer.beginText();
                    footer.setFont(regular, 8);
                    footer.newLineAtOffset(MARGIN, 24);
                    footer.showText("FleetOps Suite · Page " + (index + 1) + " of " + total);
                    footer.endText();
                }
            }
        }

        private void text(String value, PDType1Font font, float size, float advance) throws IOException {
            if (y < 55) newPage();
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(MARGIN, y);
            stream.showText(ascii(value));
            stream.endText();
            y -= advance;
        }

        private void newPage() throws IOException {
            if (stream != null) stream.close();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            pages.add(page);
            stream = new PDPageContentStream(document, page);
            y = PDRectangle.A4.getHeight() - MARGIN;
        }

        private String ascii(String value) {
            if (value == null) return "";
            String normalized = value.replace('·', '-').replace('–', '-').replace('—', '-').replace('…', '.');
            return normalized.replaceAll("[^\\x20-\\x7E]", "?");
        }
    }
}
