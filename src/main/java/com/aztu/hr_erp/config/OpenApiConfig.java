package com.aztu.hr_erp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI metadata.
 *
 * <p>Declares the SSO bearer token as a global security scheme so the Swagger UI
 * "Authorize" dialog sends {@code Authorization: Bearer <token>} on protected
 * {@code /api/**} calls (see {@code SsoAuthenticationFilter}).
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "HR ERP API",
                version = "v1",
                description = "AZTU HR / karyera backend API"),
        security = @SecurityRequirement(name = "bearer-jwt"))
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
