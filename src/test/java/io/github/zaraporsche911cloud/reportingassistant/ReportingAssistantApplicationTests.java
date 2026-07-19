package io.github.zaraporsche911cloud.reportingassistant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "DB_PASSWORD=test-only-password",
        "app.security.jwt-secret=test-only-signing-secret-with-at-least-32-bytes",
        "app.fleet.mode=MOCK",
        "app.ai.provider=mock"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ReportingAssistantApplicationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"))
            .withDatabaseName("reporting_test")
            .withUsername("reporting_test")
            .withPassword("reporting_test");

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    ReportingAssistantApplicationTests(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void runsSecuredNaturalLanguageReportingWorkflow() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));

        String token = registerAdministrator();

        MvcResult result = mockMvc.perform(post("/api/v1/assistant/query")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"Compare fuel consumption by vehicle this month"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clarificationRequired").value(false))
                .andExpect(jsonPath("$.report.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.report.reportType").value("FUEL_CONSUMPTION"))
                .andExpect(jsonPath("$.report.result.dataSource").value("Fleet Control Tower mock adapter"))
                .andExpect(jsonPath("$.report.result.rows").isArray())
                .andReturn();

        long reportId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("report").get("id").asLong();

        mockMvc.perform(get("/api/v1/reports/{id}/export/csv", reportId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/reports/{id}/export/pdf", reportId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String registerAdministrator() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName":"Reporting Administrator",
                                  "email":"admin@reporting.local",
                                  "password":"SecurePassword123!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").stringValue();
    }
}
