package com.aztu.hr_erp.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeResponse(
        UUID employeeId,
        UUID applicationId,
        UUID userId,
        UUID applicantId,
        String applicantName,
        String applicantSurname,
        String applicantEmail,
        Integer departmentId,
        String departmentName,
        String jobTitle,
        String jobType,
        BigDecimal salary,
        String status,
        LocalDateTime approvedAt,
        LocalDateTime officialAt,
        LocalDateTime createdAt) {}
