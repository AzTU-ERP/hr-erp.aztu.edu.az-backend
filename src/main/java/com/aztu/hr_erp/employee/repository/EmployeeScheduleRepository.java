package com.aztu.hr_erp.employee.repository;

import com.aztu.hr_erp.employee.domain.EmployeeSchedule;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, UUID> {
    List<EmployeeSchedule> findByEmployee_EmployeeIdOrderByEffectiveFromAsc(UUID employeeId);
}
