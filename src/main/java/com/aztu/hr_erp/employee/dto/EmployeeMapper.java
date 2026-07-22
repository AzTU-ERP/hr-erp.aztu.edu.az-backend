package com.aztu.hr_erp.employee.dto;

import com.aztu.hr_erp.employee.domain.Employee;
import com.aztu.hr_erp.employee.domain.EmployeeDocument;
import com.aztu.hr_erp.employee.domain.EmployeeSchedule;

public final class EmployeeMapper {
    private EmployeeMapper() {}

    public static EmployeeResponse toResponse(Employee e) {
        return new EmployeeResponse(
                e.getEmployeeId(), e.getApplicationId(), e.getUserId(),
                e.getApplicant() != null ? e.getApplicant().getApplicantId() : null,
                e.getApplicant() != null ? e.getApplicant().getName() : null,
                e.getApplicant() != null ? e.getApplicant().getSurname() : null,
                e.getApplicant() != null ? e.getApplicant().getEmail() : null,
                e.getDepartment() != null ? e.getDepartment().getDepartmentId() : null,
                e.getDepartment() != null ? e.getDepartment().getName() : null,
                e.getJobTitle(), e.getJobType(), e.getSalary(), e.getStatus(),
                e.getApprovedAt(), e.getOfficialAt(), e.getCreatedAt());
    }

    public static EmployeeDocumentResponse toResponse(EmployeeDocument d) {
        return new EmployeeDocumentResponse(
                d.getDocumentId(),
                d.getEmployee() != null ? d.getEmployee().getEmployeeId() : null,
                d.getDocType(), d.getStoragePath(), d.getOriginalName(),
                d.getMimeType(), d.getUploadedBy(), d.getUploadedAt());
    }

    public static EmployeeScheduleResponse toResponse(EmployeeSchedule s) {
        return new EmployeeScheduleResponse(
                s.getScheduleId(),
                s.getEmployee() != null ? s.getEmployee().getEmployeeId() : null,
                s.getDayOfWeek(), s.getStartTime(), s.getEndTime(), s.getHours(),
                s.getEffectiveFrom(), s.getEffectiveTo());
    }
}
