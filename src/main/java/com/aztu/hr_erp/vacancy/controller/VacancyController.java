package com.aztu.hr_erp.vacancy.controller;

import com.aztu.hr_erp.common.ApiResponse;
import com.aztu.hr_erp.common.PageResponse;
import com.aztu.hr_erp.security.CurrentUser;
import com.aztu.hr_erp.vacancy.dto.CreateVacancyRequest;
import com.aztu.hr_erp.vacancy.dto.UpdateVacancyRequest;
import com.aztu.hr_erp.vacancy.dto.VacancyResponse;
import com.aztu.hr_erp.vacancy.service.VacancyService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HR admin vacancy management (requires hr_admin). */
@RestController
@RequestMapping("/api/vacancies")
public class VacancyController {

    private final VacancyService service;

    public VacancyController(VacancyService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<VacancyResponse>> list(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(departmentId, category, status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<VacancyResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @PostMapping
    public ApiResponse<VacancyResponse> create(@Valid @RequestBody CreateVacancyRequest req) {
        return ApiResponse.ok(service.create(req, CurrentUser.id()), "Vacancy created");
    }

    @PutMapping("/{id}")
    public ApiResponse<VacancyResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateVacancyRequest req) {
        return ApiResponse.ok(service.update(id, req), "Vacancy updated");
    }

    @PostMapping("/{id}/close")
    public ApiResponse<VacancyResponse> close(@PathVariable UUID id) {
        return ApiResponse.ok(service.close(id), "Vacancy closed");
    }
}
