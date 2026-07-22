package com.aztu.hr_erp.termination.dto;

import com.aztu.hr_erp.termination.domain.EmployeeTermination;
import com.aztu.hr_erp.termination.domain.TerminationDocument;

public final class TerminationMapper {
    private TerminationMapper() {}

    public static TerminationResponse toResponse(EmployeeTermination t) {
        return new TerminationResponse(
                t.getTerminationId(),
                t.getEmployee() != null ? t.getEmployee().getEmployeeId() : null,
                t.getReason(), t.getTerminatedBy(), t.getEffectiveDate(), t.getCreatedAt());
    }

    public static TerminationDocumentResponse toResponse(TerminationDocument d) {
        return new TerminationDocumentResponse(
                d.getDocumentId(),
                d.getTermination() != null ? d.getTermination().getTerminationId() : null,
                d.getDocType(), d.getStoragePath(), d.getOriginalName(), d.getUploadedAt());
    }
}
