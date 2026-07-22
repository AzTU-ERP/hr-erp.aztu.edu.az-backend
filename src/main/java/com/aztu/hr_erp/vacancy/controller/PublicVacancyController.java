package com.aztu.hr_erp.vacancy.controller;

import com.aztu.hr_erp.common.ApiResponse;
import com.aztu.hr_erp.common.PageResponse;
import com.aztu.hr_erp.vacancy.dto.VacancyResponse;
import com.aztu.hr_erp.vacancy.service.VacancyService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public karyera surface: only open vacancies, respecting category visibility. Unauthenticated. */
@RestController
@RequestMapping("/api/public/vacancies")
public class PublicVacancyController {

    private final VacancyService service;

    public PublicVacancyController(VacancyService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<VacancyResponse>> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.listPublicOpen(category, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<VacancyResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.getPublic(id));
    }
}
