package com.aztu.hr_erp.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationReviewResponse(
        UUID reviewId,
        UUID applicationId,
        UUID reviewedBy,
        String decision,
        String reason,
        LocalDateTime reviewedAt) {}
