package com.aztu.hr_erp.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmailLogResponse(
        UUID emailId,
        UUID applicantId,
        UUID applicationId,
        UUID templateId,
        String toEmail,
        String subject,
        String status,
        LocalDateTime sentAt,
        LocalDateTime createdAt) {}
