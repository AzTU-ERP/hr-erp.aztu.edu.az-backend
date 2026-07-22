package com.aztu.hr_erp.termination.service;

import com.aztu.hr_erp.applicant.domain.Applicant;
import com.aztu.hr_erp.common.Codes;
import com.aztu.hr_erp.common.enums.EventType;
import com.aztu.hr_erp.common.enums.TargetSystem;
import com.aztu.hr_erp.common.enums.TemplateType;
import com.aztu.hr_erp.common.enums.TerminationDocType;
import com.aztu.hr_erp.common.exception.ConflictException;
import com.aztu.hr_erp.common.exception.NotFoundException;
import com.aztu.hr_erp.employee.domain.Employee;
import com.aztu.hr_erp.employee.service.EmployeeService;
import com.aztu.hr_erp.infrastructure.storage.FileStorageService;
import com.aztu.hr_erp.infrastructure.storage.StoredFile;
import com.aztu.hr_erp.integration.service.IntegrationEventService;
import com.aztu.hr_erp.notification.service.EmailDispatchService;
import com.aztu.hr_erp.termination.domain.EmployeeTermination;
import com.aztu.hr_erp.termination.domain.TerminationDocument;
import com.aztu.hr_erp.termination.dto.TerminationDocumentResponse;
import com.aztu.hr_erp.termination.dto.TerminationMapper;
import com.aztu.hr_erp.termination.dto.TerminationResponse;
import com.aztu.hr_erp.termination.repository.EmployeeTerminationRepository;
import com.aztu.hr_erp.termination.repository.TerminationDocumentRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Employee termination: record it, flip status, emit Finance + Turnstile events, email the employee. */
@Service
public class TerminationService {

    private final EmployeeTerminationRepository terminationRepository;
    private final TerminationDocumentRepository documentRepository;
    private final EmployeeService employeeService;
    private final IntegrationEventService integrationEventService;
    private final EmailDispatchService emailDispatchService;
    private final FileStorageService fileStorageService;

    public TerminationService(EmployeeTerminationRepository terminationRepository,
                              TerminationDocumentRepository documentRepository,
                              EmployeeService employeeService,
                              IntegrationEventService integrationEventService,
                              EmailDispatchService emailDispatchService,
                              FileStorageService fileStorageService) {
        this.terminationRepository = terminationRepository;
        this.documentRepository = documentRepository;
        this.employeeService = employeeService;
        this.integrationEventService = integrationEventService;
        this.emailDispatchService = emailDispatchService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public TerminationResponse terminate(UUID employeeId, String reason, LocalDate effectiveDate, UUID terminatedBy) {
        Employee employee = employeeService.find(employeeId);
        if (terminationRepository.existsByEmployee_EmployeeId(employeeId)) {
            throw new ConflictException("This employee is already terminated");
        }

        EmployeeTermination termination = new EmployeeTermination();
        termination.setEmployee(employee);
        termination.setReason(reason);
        termination.setTerminatedBy(terminatedBy);
        termination.setEffectiveDate(effectiveDate);
        termination = terminationRepository.save(termination);

        // Flip employee status to terminated.
        employeeService.markTerminated(employeeId);

        // Emit employee_terminated to BOTH Finance and Turnstile.
        Map<String, Object> payload = terminationPayload(employee, reason, effectiveDate);
        integrationEventService.emit(TargetSystem.FINANCE, EventType.EMPLOYEE_TERMINATED, employeeId, payload);
        integrationEventService.emit(TargetSystem.TURNSTILE, EventType.EMPLOYEE_TERMINATED, employeeId, payload);

        // Notify the employee.
        Applicant applicant = employee.getApplicant();
        if (applicant != null && applicant.getEmail() != null) {
            Map<String, String> vars = new HashMap<>();
            vars.put("name", (applicant.getName() + " " + applicant.getSurname()).trim());
            vars.put("vacancy", employee.getJobTitle());
            vars.put("reason", reason);
            emailDispatchService.dispatch(TemplateType.TERMINATION, applicant.getApplicantId(),
                    null, applicant.getEmail(), vars);
        }

        return TerminationMapper.toResponse(termination);
    }

    @Transactional(readOnly = true)
    public TerminationResponse getByEmployee(UUID employeeId) {
        EmployeeTermination t = terminationRepository.findByEmployee_EmployeeId(employeeId)
                .orElseThrow(() -> new NotFoundException("No termination record for this employee"));
        return TerminationMapper.toResponse(t);
    }

    @Transactional(readOnly = true)
    public EmployeeTermination find(UUID terminationId) {
        return terminationRepository.findById(terminationId)
                .orElseThrow(() -> new NotFoundException("Termination not found"));
    }

    @Transactional
    public TerminationDocumentResponse addDocument(UUID terminationId, String docType, MultipartFile file) {
        Codes.require(TerminationDocType.class, docType);
        EmployeeTermination termination = find(terminationId);
        StoredFile stored = fileStorageService.storeDocument(file, "termination/" + terminationId);
        TerminationDocument doc = new TerminationDocument();
        doc.setTermination(termination);
        doc.setDocType(docType);
        doc.setStoragePath(stored.storagePath());
        doc.setOriginalName(stored.originalName());
        return TerminationMapper.toResponse(documentRepository.save(doc));
    }

    @Transactional(readOnly = true)
    public List<TerminationDocumentResponse> documents(UUID terminationId) {
        find(terminationId);
        return documentRepository.findByTermination_TerminationIdOrderByUploadedAtDesc(terminationId)
                .stream().map(TerminationMapper::toResponse).toList();
    }

    private Map<String, Object> terminationPayload(Employee e, String reason, LocalDate effectiveDate) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("employeeId", e.getEmployeeId().toString());
        m.put("name", e.getApplicant() != null ? e.getApplicant().getName() : null);
        m.put("surname", e.getApplicant() != null ? e.getApplicant().getSurname() : null);
        m.put("jobType", e.getJobType());
        m.put("effectiveDate", effectiveDate != null ? effectiveDate.toString() : null);
        m.put("reason", reason);
        return m;
    }
}
