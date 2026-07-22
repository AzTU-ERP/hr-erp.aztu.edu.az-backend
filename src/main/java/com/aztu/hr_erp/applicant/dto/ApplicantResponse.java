package com.aztu.hr_erp.applicant.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicantResponse(
        UUID applicantId,
        UUID userId,
        String name,
        String surname,
        String fatherName,
        String email,
        String phone,
        LocalDateTime createdAt) {}
