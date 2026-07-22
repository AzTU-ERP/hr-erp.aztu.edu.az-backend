package com.aztu.hr_erp.vacancy.domain;

import com.aztu.hr_erp.common.BaseEntity;
import com.aztu.hr_erp.department.domain.Department;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

/** vacancies — a position advertised on the karyera subdomain. */
@Entity
@Table(name = "vacancies")
public class Vacancy extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "vacancy_id", updatable = false, nullable = false)
    private UUID vacancyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(name = "job_type", nullable = false)
    private String jobType;

    @Column(name = "salary")
    private BigDecimal salary;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status = "open";

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "opened_at", updatable = false)
    private LocalDateTime openedAt;

    @Column(name = "closes_at")
    private LocalDateTime closesAt;

    public UUID getVacancyId() { return vacancyId; }
    public void setVacancyId(UUID vacancyId) { this.vacancyId = vacancyId; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }
    public LocalDateTime getClosesAt() { return closesAt; }
    public void setClosesAt(LocalDateTime closesAt) { this.closesAt = closesAt; }
}
