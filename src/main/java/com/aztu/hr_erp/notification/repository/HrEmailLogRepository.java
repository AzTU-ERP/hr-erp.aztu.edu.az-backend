package com.aztu.hr_erp.notification.repository;

import com.aztu.hr_erp.notification.domain.HrEmailLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HrEmailLogRepository extends JpaRepository<HrEmailLog, UUID> {

    List<HrEmailLog> findByStatusInOrderByCreatedAtAsc(List<String> statuses);

    @Query("""
            select e from HrEmailLog e
            where (:status is null or e.status = :status)
            order by e.createdAt desc
            """)
    Page<HrEmailLog> search(@Param("status") String status, Pageable pageable);
}
