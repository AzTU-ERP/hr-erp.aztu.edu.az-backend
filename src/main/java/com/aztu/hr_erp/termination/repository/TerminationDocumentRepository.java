package com.aztu.hr_erp.termination.repository;

import com.aztu.hr_erp.termination.domain.TerminationDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminationDocumentRepository extends JpaRepository<TerminationDocument, UUID> {
    List<TerminationDocument> findByTermination_TerminationIdOrderByUploadedAtDesc(UUID terminationId);
}
