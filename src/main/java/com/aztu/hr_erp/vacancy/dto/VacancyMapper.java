package com.aztu.hr_erp.vacancy.dto;

import com.aztu.hr_erp.vacancy.domain.Vacancy;

public final class VacancyMapper {
    private VacancyMapper() {}

    public static VacancyResponse toResponse(Vacancy v) {
        return new VacancyResponse(
                v.getVacancyId(),
                v.getDepartment() != null ? v.getDepartment().getDepartmentId() : null,
                v.getDepartment() != null ? v.getDepartment().getName() : null,
                v.getJobTitle(),
                v.getJobType(),
                v.getSalary(),
                v.getCategory(),
                v.getDescription(),
                v.getStatus(),
                v.getCreatedBy(),
                v.getOpenedAt(),
                v.getClosesAt(),
                v.getCreatedAt());
    }
}
