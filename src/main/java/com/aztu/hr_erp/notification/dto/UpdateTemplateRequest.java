package com.aztu.hr_erp.notification.dto;

public record UpdateTemplateRequest(
        String name,
        String subject,
        String body,
        Boolean isActive) {}
