package com.aztu.hr_erp.employee.service;

import com.aztu.hr_erp.applicant.domain.Applicant;
import com.aztu.hr_erp.application.domain.Application;
import com.aztu.hr_erp.application.service.ApplicationService;
import com.aztu.hr_erp.common.Codes;
import com.aztu.hr_erp.common.PageResponse;
import com.aztu.hr_erp.common.enums.EmployeeDocType;
import com.aztu.hr_erp.common.enums.EmployeeStatus;
import com.aztu.hr_erp.common.enums.EventType;
import com.aztu.hr_erp.common.enums.TargetSystem;
import com.aztu.hr_erp.common.exception.ConflictException;
import com.aztu.hr_erp.common.exception.NotFoundException;
import com.aztu.hr_erp.employee.domain.Employee;
import com.aztu.hr_erp.employee.domain.EmployeeDocument;
import com.aztu.hr_erp.employee.domain.EmployeeSchedule;
import com.aztu.hr_erp.employee.dto.CreateScheduleRequest;
import com.aztu.hr_erp.employee.dto.EmployeeDocumentResponse;
import com.aztu.hr_erp.employee.dto.EmployeeMapper;
import com.aztu.hr_erp.employee.dto.EmployeeResponse;
import com.aztu.hr_erp.employee.dto.EmployeeScheduleResponse;
import com.aztu.hr_erp.employee.repository.EmployeeDocumentRepository;
import com.aztu.hr_erp.employee.repository.EmployeeRepository;
import com.aztu.hr_erp.employee.repository.EmployeeScheduleRepository;
import com.aztu.hr_erp.infrastructure.storage.FileStorageService;
import com.aztu.hr_erp.infrastructure.storage.StoredFile;
import com.aztu.hr_erp.integration.service.IntegrationEventService;
import com.aztu.hr_erp.vacancy.domain.Vacancy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Onboarding lifecycle: approval -> employee -> documents/contracts -> active. Emits Finance/Turnstile events. */
@Service
public class EmployeeService {

    /** The minimal document set required before an employee becomes official. */
    private static final List<String> REQUIRED_OFFICIAL_DOCS =
            List.of(EmployeeDocType.CONTRACT.code(), EmployeeDocType.APPROVAL_DOC.code());

    private final EmployeeRepository employeeRepository;
    private final EmployeeDocumentRepository documentRepository;
    private final EmployeeScheduleRepository scheduleRepository;
    private final ApplicationService applicationService;
    private final FileStorageService fileStorageService;
    private final IntegrationEventService integrationEventService;

    public EmployeeService(EmployeeRepository employeeRepository,
                           EmployeeDocumentRepository documentRepository,
                           EmployeeScheduleRepository scheduleRepository,
                           ApplicationService applicationService,
                           FileStorageService fileStorageService,
                           IntegrationEventService integrationEventService) {
        this.employeeRepository = employeeRepository;
        this.documentRepository = documentRepository;
        this.scheduleRepository = scheduleRepository;
        this.applicationService = applicationService;
        this.fileStorageService = fileStorageService;
        this.integrationEventService = integrationEventService;
    }

    /** On approval: create the employee, assign salary, and emit Finance + Turnstile events. */
    @Transactional
    public Employee createFromApproval(UUID applicationId, BigDecimal salary) {
        if (employeeRepository.existsByApplicationId(applicationId)) {
            return employeeRepository.findByApplicationId(applicationId).orElseThrow();
        }
        Application application = applicationService.find(applicationId);
        Applicant applicant = application.getApplicant();
        Vacancy vacancy = application.getVacancy();

        Employee employee = new Employee();
        employee.setApplicationId(applicationId);
        employee.setApplicant(applicant);
        employee.setDepartment(vacancy.getDepartment());
        employee.setJobTitle(vacancy.getJobTitle());
        employee.setJobType(vacancy.getJobType());
        employee.setSalary(salary);
        employee.setStatus(EmployeeStatus.ONBOARDING.code());
        employee.setApprovedAt(LocalDateTime.now());
        employee = employeeRepository.save(employee);

        // Finance: employee_approved (personal details, job_type, salary, contract ref)
        integrationEventService.emit(TargetSystem.FINANCE, EventType.EMPLOYEE_APPROVED,
                employee.getEmployeeId(), approvalPayload(employee, applicant, null));
        // Finance: salary_assigned
        if (salary != null) {
            integrationEventService.emit(TargetSystem.FINANCE, EventType.SALARY_ASSIGNED,
                    employee.getEmployeeId(), salaryPayload(employee, applicant));
        }
        // Turnstile: staff_created (personal details, job_type)
        integrationEventService.emit(TargetSystem.TURNSTILE, EventType.STAFF_CREATED,
                employee.getEmployeeId(), staffPayload(employee, applicant));
        return employee;
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> list(String status, Integer departmentId, int page, int size) {
        if (status != null) Codes.require(EmployeeStatus.class, status);
        Page<Employee> result = employeeRepository.search(status, departmentId, PageRequest.of(page, size));
        return PageResponse.from(result, result.map(EmployeeMapper::toResponse).getContent());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(UUID id) {
        return EmployeeMapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public Employee find(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    /** Assign / update salary and notify Finance (salary_assigned). */
    @Transactional
    public EmployeeResponse assignSalary(UUID id, BigDecimal salary) {
        Employee employee = find(id);
        employee.setSalary(salary);
        employeeRepository.save(employee);
        integrationEventService.emit(TargetSystem.FINANCE, EventType.SALARY_ASSIGNED,
                employee.getEmployeeId(), salaryPayload(employee, employee.getApplicant()));
        return EmployeeMapper.toResponse(employee);
    }

    /** user_id is provisioned once the account is created in the auth system. */
    @Transactional
    public EmployeeResponse provisionUser(UUID id, UUID userId) {
        Employee employee = find(id);
        employee.setUserId(userId);
        return EmployeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeDocumentResponse uploadDocument(UUID id, String docType, UUID uploadedBy, MultipartFile file) {
        Codes.require(EmployeeDocType.class, docType);
        Employee employee = find(id);
        StoredFile stored = fileStorageService.storeDocument(file, "employee/" + id);
        EmployeeDocument doc = new EmployeeDocument();
        doc.setEmployee(employee);
        doc.setDocType(docType);
        doc.setStoragePath(stored.storagePath());
        doc.setOriginalName(stored.originalName());
        doc.setMimeType(stored.mimeType());
        doc.setUploadedBy(uploadedBy);
        doc = documentRepository.save(doc);
        maybeMakeOfficial(employee);
        return EmployeeMapper.toResponse(doc);
    }

    /** Employee is not official until the required document set is complete. */
    private void maybeMakeOfficial(Employee employee) {
        if (!EmployeeStatus.ONBOARDING.code().equals(employee.getStatus())) {
            return;
        }
        boolean complete = REQUIRED_OFFICIAL_DOCS.stream()
                .allMatch(t -> documentRepository.existsByEmployee_EmployeeIdAndDocType(employee.getEmployeeId(), t));
        if (complete) {
            employee.setStatus(EmployeeStatus.ACTIVE.code());
            employee.setOfficialAt(LocalDateTime.now());
            employeeRepository.save(employee);
        }
    }

    @Transactional(readOnly = true)
    public List<EmployeeDocumentResponse> documents(UUID id) {
        find(id);
        return documentRepository.findByEmployee_EmployeeIdOrderByUploadedAtDesc(id)
                .stream().map(EmployeeMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeDocument findDocument(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
    }

    /** Record a non-casual working schedule; forward the hours to Finance (schedule_updated). */
    @Transactional
    public EmployeeScheduleResponse addSchedule(UUID id, CreateScheduleRequest req) {
        Employee employee = find(id);
        EmployeeSchedule schedule = new EmployeeSchedule();
        schedule.setEmployee(employee);
        schedule.setDayOfWeek(req.dayOfWeek());
        schedule.setStartTime(req.startTime());
        schedule.setEndTime(req.endTime());
        schedule.setHours(req.hours());
        schedule.setEffectiveFrom(req.effectiveFrom());
        schedule.setEffectiveTo(req.effectiveTo());
        schedule = scheduleRepository.save(schedule);
        integrationEventService.emit(TargetSystem.FINANCE, EventType.SCHEDULE_UPDATED,
                employee.getEmployeeId(), schedulePayload(employee, schedule));
        return EmployeeMapper.toResponse(schedule);
    }

    @Transactional(readOnly = true)
    public List<EmployeeScheduleResponse> schedules(UUID id) {
        find(id);
        return scheduleRepository.findByEmployee_EmployeeIdOrderByEffectiveFromAsc(id)
                .stream().map(EmployeeMapper::toResponse).toList();
    }

    @Transactional
    public void markTerminated(UUID id) {
        Employee employee = find(id);
        if (EmployeeStatus.TERMINATED.code().equals(employee.getStatus())) {
            throw new ConflictException("This employee is already terminated");
        }
        employee.setStatus(EmployeeStatus.TERMINATED.code());
        employeeRepository.save(employee);
    }

    // ---- payload builders ----

    private Map<String, Object> personalDetails(Employee e, Applicant a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("employeeId", e.getEmployeeId().toString());
        m.put("applicantId", a != null ? a.getApplicantId().toString() : null);
        m.put("name", a != null ? a.getName() : null);
        m.put("surname", a != null ? a.getSurname() : null);
        m.put("fatherName", a != null ? a.getFatherName() : null);
        m.put("email", a != null ? a.getEmail() : null);
        m.put("phone", a != null ? a.getPhone() : null);
        m.put("departmentId", e.getDepartment() != null ? e.getDepartment().getDepartmentId() : null);
        m.put("jobTitle", e.getJobTitle());
        m.put("jobType", e.getJobType());
        return m;
    }

    private Map<String, Object> approvalPayload(Employee e, Applicant a, String contractRef) {
        Map<String, Object> m = personalDetails(e, a);
        m.put("salary", e.getSalary());
        m.put("contractRef", contractRef);
        return m;
    }

    private Map<String, Object> salaryPayload(Employee e, Applicant a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("employeeId", e.getEmployeeId().toString());
        m.put("name", a != null ? a.getName() : null);
        m.put("surname", a != null ? a.getSurname() : null);
        m.put("salary", e.getSalary());
        m.put("jobType", e.getJobType());
        return m;
    }

    private Map<String, Object> staffPayload(Employee e, Applicant a) {
        Map<String, Object> m = personalDetails(e, a);
        return m;
    }

    private Map<String, Object> schedulePayload(Employee e, EmployeeSchedule s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("employeeId", e.getEmployeeId().toString());
        m.put("scheduleId", s.getScheduleId().toString());
        m.put("dayOfWeek", s.getDayOfWeek());
        m.put("startTime", s.getStartTime() != null ? s.getStartTime().toString() : null);
        m.put("endTime", s.getEndTime() != null ? s.getEndTime().toString() : null);
        m.put("hours", s.getHours());
        m.put("effectiveFrom", s.getEffectiveFrom() != null ? s.getEffectiveFrom().toString() : null);
        m.put("effectiveTo", s.getEffectiveTo() != null ? s.getEffectiveTo().toString() : null);
        return m;
    }
}
