package com.aztu.hr_erp.application.domain;

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

/** application_reviews — full audit trail: one row per screening/approve/reject decision. */
@Entity
@Table(name = "application_reviews")
public class ApplicationReview {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "review_id", updatable = false, nullable = false)
    private UUID reviewId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "reviewed_by", nullable = false)
    private UUID reviewedBy;

    @Column(name = "decision", nullable = false)
    private String decision;

    @Column(name = "reason")
    private String reason;

    @CreationTimestamp
    @Column(name = "reviewed_at", updatable = false)
    private LocalDateTime reviewedAt;

    public UUID getReviewId() { return reviewId; }
    public void setReviewId(UUID reviewId) { this.reviewId = reviewId; }
    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }
    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
