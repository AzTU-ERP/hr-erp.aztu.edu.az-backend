package com.aztu.hr_erp.applicant.domain;

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

/** applicant_documents — uploaded CVs (and other docs). storage_path is the VPS path only. */
@Entity
@Table(name = "applicant_documents")
public class ApplicantDocument {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "document_id", updatable = false, nullable = false)
    private UUID documentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @Column(name = "doc_type")
    private String docType = "cv";

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID documentId) { this.documentId = documentId; }
    public Applicant getApplicant() { return applicant; }
    public void setApplicant(Applicant applicant) { this.applicant = applicant; }
    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
