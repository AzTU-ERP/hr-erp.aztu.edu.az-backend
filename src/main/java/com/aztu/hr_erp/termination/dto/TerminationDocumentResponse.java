package com.aztu.hr_erp.termination.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TerminationDocumentResponse(
        UUID documentId,
        UUID terminationId,
        String docType,
        String storagePath,
        String originalName,
        LocalDateTime uploadedAt) {}
