package com.aztu.hr_erp.integration.controller;

import com.aztu.hr_erp.common.ApiResponse;
import com.aztu.hr_erp.common.PageResponse;
import com.aztu.hr_erp.integration.dto.IntegrationEventResponse;
import com.aztu.hr_erp.integration.service.IntegrationEventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Visibility into the outbox for HR admins. */
@RestController
@RequestMapping("/api/integration/events")
public class IntegrationEventController {

    private final IntegrationEventService service;

    public IntegrationEventController(IntegrationEventService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<IntegrationEventResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetSystem,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(status, targetSystem, page, size));
    }
}
