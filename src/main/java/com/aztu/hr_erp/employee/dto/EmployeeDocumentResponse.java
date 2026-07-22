package com.aztu.hr_erp.employee.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeDocumentResponse(
        UUID documentId,
        UUID employeeId,
        String docType,
        String storagePath,
        String originalName,
        String mimeType,
        UUID uploadedBy,
        LocalDateTime uploadedAt) {}
