package com.aztu.hr_erp.integration.service;

import com.aztu.hr_erp.common.Codes;
import com.aztu.hr_erp.common.PageResponse;
import com.aztu.hr_erp.common.enums.EventType;
import com.aztu.hr_erp.common.enums.IntegrationStatus;
import com.aztu.hr_erp.common.enums.TargetSystem;
import com.aztu.hr_erp.integration.domain.IntegrationEvent;
import com.aztu.hr_erp.integration.dto.IntegrationEventMapper;
import com.aztu.hr_erp.integration.dto.IntegrationEventResponse;
import com.aztu.hr_erp.integration.repository.IntegrationEventRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** Writes outbox rows. The worker delivers them; HR never writes to other modules' DBs. */
@Service
public class IntegrationEventService {

    private final IntegrationEventRepository repository;
    private final ObjectMapper objectMapper;

    public IntegrationEventService(IntegrationEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /** Emit an integration event with status='pending' for the outbox worker to deliver. */
    @Transactional
    public IntegrationEvent emit(TargetSystem target, EventType type, UUID employeeId, Map<String, Object> payload) {
        IntegrationEvent event = new IntegrationEvent();
        event.setTargetSystem(target.code());
        event.setEventType(type.code());
        event.setEmployeeId(employeeId);
        event.setPayload(objectMapper.writeValueAsString(payload));
        event.setStatus(IntegrationStatus.PENDING.code());
        event.setAttempts(0);
        return repository.save(event);
    }

    @Transactional(readOnly = true)
    public PageResponse<IntegrationEventResponse> list(String status, String targetSystem, int page, int size) {
        if (status != null) Codes.require(IntegrationStatus.class, status);
        if (targetSystem != null) Codes.require(TargetSystem.class, targetSystem);
        Page<IntegrationEvent> result = repository.search(status, targetSystem, PageRequest.of(page, size));
        return PageResponse.from(result, result.map(IntegrationEventMapper::toResponse).getContent());
    }
}
