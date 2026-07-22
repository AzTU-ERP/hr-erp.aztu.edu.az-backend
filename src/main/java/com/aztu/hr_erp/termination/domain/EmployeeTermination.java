package com.aztu.hr_erp.termination.domain;

import com.aztu.hr_erp.common.BaseEntity;
import com.aztu.hr_erp.employee.domain.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/** employee_terminations — one active termination per employee (employee_id UNIQUE). */
@Entity
@Table(name = "employee_terminations")
public class EmployeeTermination extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "termination_id", updatable = false, nullable = false)
    private UUID terminationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "terminated_by", nullable = false)
    private UUID terminatedBy;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    public UUID getTerminationId() { return terminationId; }
    public void setTerminationId(UUID terminationId) { this.terminationId = terminationId; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public UUID getTerminatedBy() { return terminatedBy; }
    public void setTerminatedBy(UUID terminatedBy) { this.terminatedBy = terminatedBy; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
}
