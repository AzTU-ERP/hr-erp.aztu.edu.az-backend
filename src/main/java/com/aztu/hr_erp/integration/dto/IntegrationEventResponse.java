package com.aztu.hr_erp.integration.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationEventResponse(
        UUID eventId,
        String targetSystem,
        String eventType,
        UUID employeeId,
        String payload,
        String status,
        Integer attempts,
        String lastError,
        LocalDateTime createdAt,
        LocalDateTime sentAt) {}
