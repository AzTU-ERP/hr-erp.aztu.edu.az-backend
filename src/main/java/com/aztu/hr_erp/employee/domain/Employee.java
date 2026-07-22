package com.aztu.hr_erp.employee.domain;

import com.aztu.hr_erp.applicant.domain.Applicant;
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
import org.hibernate.annotations.UuidGenerator;

/** employees — created from an approved application (application_id UNIQUE = one per approval). */
@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "employee_id", updatable = false, nullable = false)
    private UUID employeeId;

    @Column(name = "application_id", unique = true)
    private UUID applicationId;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(name = "job_type", nullable = false)
    private String jobType;

    @Column(name = "salary")
    private BigDecimal salary;

    @Column(name = "status")
    private String status = "onboarding";

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "official_at")
    private LocalDateTime officialAt;

    public UUID getEmployeeId() { return employeeId; }
    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Applicant getApplicant() { return applicant; }
    public void setApplicant(Applicant applicant) { this.applicant = applicant; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getOfficialAt() { return officialAt; }
    public void setOfficialAt(LocalDateTime officialAt) { this.officialAt = officialAt; }
}
