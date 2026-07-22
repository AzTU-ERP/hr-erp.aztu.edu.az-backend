package com.aztu.hr_erp.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record EmployeeScheduleResponse(
        UUID scheduleId,
        UUID employeeId,
        String dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal hours,
        LocalDate effectiveFrom,
        LocalDate effectiveTo) {}
