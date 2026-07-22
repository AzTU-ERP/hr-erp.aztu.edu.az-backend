package com.aztu.hr_erp.integration.domain;

import com.aztu.hr_erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

/** integration_events — outbox row for Finance/Turnstile. payload is JSONB; never write other modules' DBs directly. */
@Entity
@Table(name = "integration_events")
public class IntegrationEvent extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "event_id", updatable = false, nullable = false)
    private UUID eventId;

    @Column(name = "target_system", nullable = false)
    private String targetSystem;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "employee_id")
    private UUID employeeId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "status")
    private String status = "pending";

    @Column(name = "attempts")
    private Integer attempts = 0;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public UUID getEmployeeId() { return employeeId; }
    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
