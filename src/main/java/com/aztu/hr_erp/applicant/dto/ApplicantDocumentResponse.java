package com.aztu.hr_erp.applicant.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicantDocumentResponse(
        UUID documentId,
        UUID applicantId,
        String docType,
        String storagePath,
        String originalName,
        String mimeType,
        Long sizeBytes,
        LocalDateTime uploadedAt) {}
