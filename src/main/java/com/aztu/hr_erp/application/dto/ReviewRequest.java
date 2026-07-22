package com.aztu.hr_erp.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/** HR screening decision: screening | approved | rejected. Reason required on rejection; salary used on approval. */
public record ReviewRequest(
        @NotBlank String decision,
        String reason,
        BigDecimal salary) {}
