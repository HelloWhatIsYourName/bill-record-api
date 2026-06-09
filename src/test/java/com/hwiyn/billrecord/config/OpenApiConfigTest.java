package com.hwiyn.billrecord.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

class OpenApiConfigTest {

    @Test
    void documentsBearerJwtAuthentication() {
        OpenAPI openAPI = new OpenApiConfig().billRecordOpenApi();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("bill-record-api");
        assertThat(openAPI.getComponents().getSecuritySchemes())
                .containsKey("bearerAuth");
        assertThat(openAPI.getComponents().getSecuritySchemes().get("bearerAuth"))
                .extracting(SecurityScheme::getType, SecurityScheme::getScheme, SecurityScheme::getBearerFormat)
                .containsExactly(SecurityScheme.Type.HTTP, "bearer", "JWT");
    }
}
