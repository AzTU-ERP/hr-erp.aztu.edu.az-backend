package com.aztu.hr_erp.department.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateDepartmentRequest(
        @NotBlank String name,
        Boolean isActive) {}
