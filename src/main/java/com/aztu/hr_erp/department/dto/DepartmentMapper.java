package com.aztu.hr_erp.department.dto;

import com.aztu.hr_erp.department.domain.Department;

public final class DepartmentMapper {
    private DepartmentMapper() {}

    public static DepartmentResponse toResponse(Department d) {
        return new DepartmentResponse(
                d.getDepartmentId(),
                d.getCode(),
                d.getName(),
                Boolean.TRUE.equals(d.getIsActive()),
                d.getCreatedAt());
    }
}
