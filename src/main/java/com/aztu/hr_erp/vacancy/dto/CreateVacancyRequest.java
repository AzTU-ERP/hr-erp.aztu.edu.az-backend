package com.aztu.hr_erp.vacancy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateVacancyRequest(
        @NotNull Integer departmentId,
        @NotBlank String jobTitle,
        @NotBlank String jobType,
        BigDecimal salary,
        @NotBlank String category,
        String description,
        LocalDateTime closesAt) {}
