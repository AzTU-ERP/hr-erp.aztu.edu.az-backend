package com.aztu.hr_erp.integration.repository;

import com.aztu.hr_erp.integration.domain.IntegrationEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IntegrationEventRepository extends JpaRepository<IntegrationEvent, UUID> {

    List<IntegrationEvent> findByStatusInAndAttemptsLessThanOrderByCreatedAtAsc(
            List<String> statuses, int maxAttempts);

    @Query("""
            select e from IntegrationEvent e
            where (:status is null or e.status = :status)
              and (:targetSystem is null or e.targetSystem = :targetSystem)
            order by e.createdAt desc
            """)
    Page<IntegrationEvent> search(@Param("status") String status,
                                 @Param("targetSystem") String targetSystem,
                                 Pageable pageable);
}
