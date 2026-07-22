package com.aztu.hr_erp.applicant.repository;

import com.aztu.hr_erp.applicant.domain.ApplicantDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantDocumentRepository extends JpaRepository<ApplicantDocument, UUID> {
    List<ApplicantDocument> findByApplicant_ApplicantIdOrderByUploadedAtDesc(UUID applicantId);
}
