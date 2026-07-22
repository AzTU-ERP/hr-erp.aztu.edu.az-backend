package com.aztu.hr_erp.applicant.service;

import com.aztu.hr_erp.applicant.domain.Applicant;
import com.aztu.hr_erp.applicant.domain.ApplicantDocument;
import com.aztu.hr_erp.applicant.repository.ApplicantDocumentRepository;
import com.aztu.hr_erp.applicant.repository.ApplicantRepository;
import com.aztu.hr_erp.common.enums.ApplicantDocType;
import com.aztu.hr_erp.common.exception.NotFoundException;
import com.aztu.hr_erp.infrastructure.storage.StoredFile;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicantService {

    private final ApplicantRepository applicantRepository;
    private final ApplicantDocumentRepository documentRepository;

    public ApplicantService(ApplicantRepository applicantRepository,
                            ApplicantDocumentRepository documentRepository) {
        this.applicantRepository = applicantRepository;
        this.documentRepository = documentRepository;
    }

    /** Repeat applications reuse the same applicant — upsert by email. */
    @Transactional
    public Applicant upsertByEmail(String name, String surname, String fatherName,
                                   String email, String phone) {
        Applicant applicant = applicantRepository.findByEmail(email).orElseGet(Applicant::new);
        applicant.setName(name);
        applicant.setSurname(surname);
        applicant.setFatherName(fatherName);
        applicant.setEmail(email);
        applicant.setPhone(phone);
        return applicantRepository.save(applicant);
    }

    @Transactional
    public ApplicantDocument addCv(Applicant applicant, StoredFile stored) {
        ApplicantDocument doc = new ApplicantDocument();
        doc.setApplicant(applicant);
        doc.setDocType(ApplicantDocType.CV.code());
        doc.setStoragePath(stored.storagePath());
        doc.setOriginalName(stored.originalName());
        doc.setMimeType(stored.mimeType());
        doc.setSizeBytes(stored.sizeBytes());
        return documentRepository.save(doc);
    }

    @Transactional(readOnly = true)
    public Applicant find(UUID id) {
        return applicantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Applicant not found"));
    }

    @Transactional(readOnly = true)
    public ApplicantDocument findDocument(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));
    }
}
