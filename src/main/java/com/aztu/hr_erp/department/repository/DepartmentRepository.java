package com.aztu.hr_erp.department.repository;

import com.aztu.hr_erp.department.domain.Department;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    boolean existsByCode(String code);
    List<Department> findByIsActiveTrueOrderByNameAsc();
    List<Department> findAllByOrderByNameAsc();
}
