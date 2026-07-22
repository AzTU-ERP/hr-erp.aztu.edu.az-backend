package com.aztu.hr_erp.termination.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

/** termination_documents — resignation | settlement | other. storage_path is the VPS path only. */
@Entity
@Table(name = "termination_documents")
public class TerminationDocument {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "document_id", updatable = false, nullable = false)
    private UUID documentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "termination_id", nullable = false)
    private EmployeeTermination termination;

    @Column(name = "doc_type")
    private String docType;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "original_name")
    private String originalName;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID documentId) { this.documentId = documentId; }
    public EmployeeTermination getTermination() { return termination; }
    public void setTermination(EmployeeTermination termination) { this.termination = termination; }
    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
