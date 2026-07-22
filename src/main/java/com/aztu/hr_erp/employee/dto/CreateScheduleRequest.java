package com.aztu.hr_erp.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateScheduleRequest(
        String dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal hours,
        LocalDate effectiveFrom,
        LocalDate effectiveTo) {}
