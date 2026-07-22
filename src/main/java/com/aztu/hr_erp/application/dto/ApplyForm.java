package com.aztu.hr_erp.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/** Multipart apply form bound via @ModelAttribute (name/surname/.../cv). */
public class ApplyForm {

    @NotBlank private String name;
    @NotBlank private String surname;
    private String fatherName;
    @NotBlank @Email private String email;
    @NotBlank private String phone;
    @NotNull private UUID vacancyId;
    private boolean isAlumni;
    private MultipartFile cv;

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
    public UUID getVacancyId() { return vacancyId; }
    public void setVacancyId(UUID vacancyId) { this.vacancyId = vacancyId; }
    public boolean isAlumni() { return isAlumni; }
    public void setAlumni(boolean alumni) { isAlumni = alumni; }
    public MultipartFile getCv() { return cv; }
    public void setCv(MultipartFile cv) { this.cv = cv; }
}
