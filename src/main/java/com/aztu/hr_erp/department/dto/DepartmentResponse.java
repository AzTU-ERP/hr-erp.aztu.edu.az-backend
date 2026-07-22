package com.aztu.hr_erp.department.dto;

import java.time.LocalDateTime;

public record DepartmentResponse(
        Integer departmentId,
        String code,
        String name,
        boolean isActive,
        LocalDateTime createdAt) {}
