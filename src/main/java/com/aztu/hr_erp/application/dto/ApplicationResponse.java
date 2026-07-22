package com.aztu.hr_erp.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationResponse(
        UUID applicationId,
        UUID applicantId,
        String applicantName,
        String applicantSurname,
        String applicantEmail,
        String applicantPhone,
        UUID vacancyId,
        String vacancyTitle,
        String vacancyCategory,
        UUID cvDocumentId,
        String cvOriginalName,
        String source,
        String status,
        LocalDateTime submittedAt) {}
