package com.aztu.hr_erp.applicant.domain;

import com.aztu.hr_erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/** applicants — exists BEFORE any account, keyed by email. user_id is filled only if they become an account holder. */
@Entity
@Table(name = "applicants")
public class Applicant extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "applicant_id", updatable = false, nullable = false)
    private UUID applicantId;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    public UUID getApplicantId() { return applicantId; }
    public void setApplicantId(UUID applicantId) { this.applicantId = applicantId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
