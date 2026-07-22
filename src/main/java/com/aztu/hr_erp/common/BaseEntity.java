package com.aztu.hr_erp.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

/** Shared audit base: every table carries a created_at timestamp (DB default now()). */
@MappedSuperclass
public abstract class BaseEntity {

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
