package com.aztu.hr_erp.vacancy.repository;

import com.aztu.hr_erp.vacancy.domain.Vacancy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VacancyRepository extends JpaRepository<Vacancy, UUID> {

    @Query("""
            select v from Vacancy v
            where (:departmentId is null or v.department.departmentId = :departmentId)
              and (:category is null or v.category = :category)
              and (:status is null or v.status = :status)
            order by v.openedAt desc
            """)
    Page<Vacancy> search(@Param("departmentId") Integer departmentId,
                         @Param("category") String category,
                         @Param("status") String status,
                         Pageable pageable);

    @Query("""
            select v from Vacancy v
            where v.status = 'open'
              and (:category is null or v.category = :category)
            order by v.openedAt desc
            """)
    Page<Vacancy> searchPublicOpen(@Param("category") String category, Pageable pageable);

    List<Vacancy> findByStatusAndClosesAtBefore(String status, LocalDateTime time);
}
