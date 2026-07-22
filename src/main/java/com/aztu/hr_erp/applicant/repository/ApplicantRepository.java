package com.aztu.hr_erp.applicant.repository;

import com.aztu.hr_erp.applicant.domain.Applicant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantRepository extends JpaRepository<Applicant, UUID> {
    Optional<Applicant> findByEmail(String email);
    boolean existsByEmail(String email);
}
