package com.aztu.hr_erp.notification.controller;

import com.aztu.hr_erp.common.ApiResponse;
import com.aztu.hr_erp.notification.dto.CreateTemplateRequest;
import com.aztu.hr_erp.notification.dto.TemplateResponse;
import com.aztu.hr_erp.notification.dto.UpdateTemplateRequest;
import com.aztu.hr_erp.notification.service.TemplateService;
import com.aztu.hr_erp.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService service;

    public TemplateController(TemplateService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<TemplateResponse>> list(@RequestParam(required = false) String type) {
        return ApiResponse.ok(service.list(type));
    }

    @GetMapping("/{id}")
    public ApiResponse<TemplateResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @PostMapping
    public ApiResponse<TemplateResponse> create(@Valid @RequestBody CreateTemplateRequest req) {
        return ApiResponse.ok(service.create(req, CurrentUser.id()), "Template created");
    }

    @PutMapping("/{id}")
    public ApiResponse<TemplateResponse> update(@PathVariable UUID id,
                                                @RequestBody UpdateTemplateRequest req) {
        return ApiResponse.ok(service.update(id, req), "Template updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<TemplateResponse> deactivate(@PathVariable UUID id) {
        return ApiResponse.ok(service.deactivate(id), "Template deactivated");
    }
}
