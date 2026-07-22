package com.aztu.hr_erp.termination.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TerminationResponse(
        UUID terminationId,
        UUID employeeId,
        String reason,
        UUID terminatedBy,
        LocalDate effectiveDate,
        LocalDateTime createdAt) {}
