package com.aztu.hr_erp.notification.domain;

import com.aztu.hr_erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/** hr_email_log — every email queued/sent with delivery status. FK ids kept as plain UUIDs. */
@Entity
@Table(name = "hr_email_log")
public class HrEmailLog extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "email_id", updatable = false, nullable = false)
    private UUID emailId;

    @Column(name = "applicant_id")
    private UUID applicantId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "to_email", nullable = false)
    private String toEmail;

    @Column(name = "subject")
    private String subject;

    @Column(name = "status")
    private String status = "pending";

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public UUID getEmailId() { return emailId; }
    public void setEmailId(UUID emailId) { this.emailId = emailId; }
    public UUID getApplicantId() { return applicantId; }
    public void setApplicantId(UUID applicantId) { this.applicantId = applicantId; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }
    public String getToEmail() { return toEmail; }
    public void setToEmail(String toEmail) { this.toEmail = toEmail; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
