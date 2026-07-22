package com.aztu.hr_erp.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TemplateResponse(
        UUID templateId,
        String type,
        String name,
        String subject,
        String body,
        UUID createdBy,
        boolean isActive,
        LocalDateTime createdAt) {}
