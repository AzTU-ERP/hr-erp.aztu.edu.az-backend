package com.aztu.hr_erp.termination.repository;

import com.aztu.hr_erp.termination.domain.EmployeeTermination;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeTerminationRepository extends JpaRepository<EmployeeTermination, UUID> {
    boolean existsByEmployee_EmployeeId(UUID employeeId);
    Optional<EmployeeTermination> findByEmployee_EmployeeId(UUID employeeId);
}
