package com.aztu.hr_erp.applicant.dto;

import com.aztu.hr_erp.applicant.domain.Applicant;
import com.aztu.hr_erp.applicant.domain.ApplicantDocument;

public final class ApplicantMapper {
    private ApplicantMapper() {}

    public static ApplicantResponse toResponse(Applicant a) {
        return new ApplicantResponse(
                a.getApplicantId(), a.getUserId(), a.getName(), a.getSurname(),
                a.getFatherName(), a.getEmail(), a.getPhone(), a.getCreatedAt());
    }

    public static ApplicantDocumentResponse toResponse(ApplicantDocument d) {
        return new ApplicantDocumentResponse(
                d.getDocumentId(),
                d.getApplicant() != null ? d.getApplicant().getApplicantId() : null,
                d.getDocType(), d.getStoragePath(), d.getOriginalName(),
                d.getMimeType(), d.getSizeBytes(), d.getUploadedAt());
    }
}
