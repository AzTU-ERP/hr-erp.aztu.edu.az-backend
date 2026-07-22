package com.aztu.hr_erp.employee.repository;

import com.aztu.hr_erp.employee.domain.EmployeeDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, UUID> {
    List<EmployeeDocument> findByEmployee_EmployeeIdOrderByUploadedAtDesc(UUID employeeId);
    boolean existsByEmployee_EmployeeIdAndDocType(UUID employeeId, String docType);
}
