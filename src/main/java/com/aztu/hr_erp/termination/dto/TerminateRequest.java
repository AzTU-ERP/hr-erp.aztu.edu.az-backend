package com.aztu.hr_erp.termination.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TerminateRequest(
        @NotBlank String reason,
        @NotNull LocalDate effectiveDate) {}
