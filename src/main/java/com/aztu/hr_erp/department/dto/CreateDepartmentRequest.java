package com.aztu.hr_erp.department.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDepartmentRequest(
        @NotBlank String code,
        @NotBlank String name) {}
