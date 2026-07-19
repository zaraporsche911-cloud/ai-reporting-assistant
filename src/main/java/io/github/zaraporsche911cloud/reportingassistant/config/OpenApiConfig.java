package io.github.zaraporsche911cloud.reportingassistant.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI reportingAssistantOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FleetOps AI Reporting Assistant API")
                        .version("v1")
                        .description("Safe, typed AI-assisted reporting over Fleet Control Tower APIs"))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
