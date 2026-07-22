package com.aztu.hr_erp.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTemplateRequest(
        @NotBlank String type,
        @NotBlank String name,
        @NotBlank String subject,
        @NotBlank String body) {}
