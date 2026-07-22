package com.aztu.hr_erp.notification.repository;

import com.aztu.hr_erp.notification.domain.HrTemplate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrTemplateRepository extends JpaRepository<HrTemplate, UUID> {
    List<HrTemplate> findByTypeOrderByCreatedAtDesc(String type);
    List<HrTemplate> findAllByOrderByCreatedAtDesc();
    Optional<HrTemplate> findFirstByTypeAndIsActiveTrueOrderByCreatedAtAsc(String type);
}
