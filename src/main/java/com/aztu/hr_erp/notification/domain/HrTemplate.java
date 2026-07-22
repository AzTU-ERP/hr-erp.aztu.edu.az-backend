package com.aztu.hr_erp.notification.domain;

import com.aztu.hr_erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/** hr_templates — admin-authored email templates with {{placeholders}}. */
@Entity
@Table(name = "hr_templates")
public class HrTemplate extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "template_id", updatable = false, nullable = false)
    private UUID templateId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
