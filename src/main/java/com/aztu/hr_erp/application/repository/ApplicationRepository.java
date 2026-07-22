package com.aztu.hr_erp.application.repository;

import com.aztu.hr_erp.application.domain.Application;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByApplicant_ApplicantIdAndVacancy_VacancyId(UUID applicantId, UUID vacancyId);

    @Query("""
            select a from Application a
            where (:status is null or a.status = :status)
              and (:vacancyId is null or a.vacancy.vacancyId = :vacancyId)
            order by a.submittedAt desc
            """)
    Page<Application> search(@Param("status") String status,
                            @Param("vacancyId") UUID vacancyId,
                            Pageable pageable);
}
