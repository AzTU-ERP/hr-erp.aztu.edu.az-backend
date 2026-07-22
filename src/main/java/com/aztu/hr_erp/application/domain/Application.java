package com.aztu.hr_erp.application.domain;

import com.aztu.hr_erp.applicant.domain.Applicant;
import com.aztu.hr_erp.applicant.domain.ApplicantDocument;
import com.aztu.hr_erp.vacancy.domain.Vacancy;
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

/** applications — one row per application; unique (applicant, vacancy) prevents double-apply. */
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "application_id", updatable = false, nullable = false)
    private UUID applicationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private Vacancy vacancy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_document_id")
    private ApplicantDocument cvDocument;

    @Column(name = "source")
    private String source = "karyera";

    @Column(name = "status")
    private String status = "submitted";

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public Applicant getApplicant() { return applicant; }
    public void setApplicant(Applicant applicant) { this.applicant = applicant; }
    public Vacancy getVacancy() { return vacancy; }
    public void setVacancy(Vacancy vacancy) { this.vacancy = vacancy; }
    public ApplicantDocument getCvDocument() { return cvDocument; }
    public void setCvDocument(ApplicantDocument cvDocument) { this.cvDocument = cvDocument; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
