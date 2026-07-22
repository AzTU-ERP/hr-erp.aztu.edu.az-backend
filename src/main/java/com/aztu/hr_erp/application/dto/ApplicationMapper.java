package com.aztu.hr_erp.application.dto;

import com.aztu.hr_erp.application.domain.Application;
import com.aztu.hr_erp.application.domain.ApplicationReview;

public final class ApplicationMapper {
    private ApplicationMapper() {}

    public static ApplicationResponse toResponse(Application a) {
        return new ApplicationResponse(
                a.getApplicationId(),
                a.getApplicant() != null ? a.getApplicant().getApplicantId() : null,
                a.getApplicant() != null ? a.getApplicant().getName() : null,
                a.getApplicant() != null ? a.getApplicant().getSurname() : null,
                a.getApplicant() != null ? a.getApplicant().getEmail() : null,
                a.getApplicant() != null ? a.getApplicant().getPhone() : null,
                a.getVacancy() != null ? a.getVacancy().getVacancyId() : null,
                a.getVacancy() != null ? a.getVacancy().getJobTitle() : null,
                a.getVacancy() != null ? a.getVacancy().getCategory() : null,
                a.getCvDocument() != null ? a.getCvDocument().getDocumentId() : null,
                a.getCvDocument() != null ? a.getCvDocument().getOriginalName() : null,
                a.getSource(),
                a.getStatus(),
                a.getSubmittedAt());
    }

    public static ApplicationReviewResponse toResponse(ApplicationReview r) {
        return new ApplicationReviewResponse(
                r.getReviewId(),
                r.getApplication() != null ? r.getApplication().getApplicationId() : null,
                r.getReviewedBy(),
                r.getDecision(),
                r.getReason(),
                r.getReviewedAt());
    }
}
