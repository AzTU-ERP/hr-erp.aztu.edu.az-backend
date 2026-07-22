package com.aztu.hr_erp.employee.repository;

import com.aztu.hr_erp.employee.domain.Employee;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    boolean existsByApplicationId(UUID applicationId);
    Optional<Employee> findByApplicationId(UUID applicationId);

    @Query("""
            select e from Employee e
            where (:status is null or e.status = :status)
              and (:departmentId is null or e.department.departmentId = :departmentId)
            order by e.createdAt desc
            """)
    Page<Employee> search(@Param("status") String status,
                         @Param("departmentId") Integer departmentId,
                         Pageable pageable);
}
