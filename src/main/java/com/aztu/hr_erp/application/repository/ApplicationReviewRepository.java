package com.aztu.hr_erp.application.repository;

import com.aztu.hr_erp.application.domain.ApplicationReview;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationReviewRepository extends JpaRepository<ApplicationReview, UUID> {
    List<ApplicationReview> findByApplication_ApplicationIdOrderByReviewedAtAsc(UUID applicationId);
}
