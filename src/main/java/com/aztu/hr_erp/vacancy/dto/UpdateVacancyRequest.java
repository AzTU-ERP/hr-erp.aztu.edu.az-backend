package com.aztu.hr_erp.vacancy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateVacancyRequest(
        Integer departmentId,
        String jobTitle,
        String jobType,
        BigDecimal salary,
        String category,
        String description,
        String status,
        LocalDateTime closesAt) {}
