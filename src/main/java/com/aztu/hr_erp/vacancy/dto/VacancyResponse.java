package com.aztu.hr_erp.vacancy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record VacancyResponse(
        UUID vacancyId,
        Integer departmentId,
        String departmentName,
        String jobTitle,
        String jobType,
        BigDecimal salary,
        String category,
        String description,
        String status,
        UUID createdBy,
        LocalDateTime openedAt,
        LocalDateTime closesAt,
        LocalDateTime createdAt) {}
