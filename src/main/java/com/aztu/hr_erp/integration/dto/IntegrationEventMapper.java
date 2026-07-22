package com.aztu.hr_erp.integration.dto;

import com.aztu.hr_erp.integration.domain.IntegrationEvent;

public final class IntegrationEventMapper {
    private IntegrationEventMapper() {}

    public static IntegrationEventResponse toResponse(IntegrationEvent e) {
        return new IntegrationEventResponse(
                e.getEventId(), e.getTargetSystem(), e.getEventType(), e.getEmployeeId(),
                e.getPayload(), e.getStatus(), e.getAttempts(), e.getLastError(),
                e.getCreatedAt(), e.getSentAt());
    }
}
