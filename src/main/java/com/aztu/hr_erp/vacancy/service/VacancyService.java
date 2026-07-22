package com.aztu.hr_erp.vacancy.service;

import com.aztu.hr_erp.common.Codes;
import com.aztu.hr_erp.common.PageResponse;
import com.aztu.hr_erp.common.enums.Category;
import com.aztu.hr_erp.common.enums.JobType;
import com.aztu.hr_erp.common.enums.VacancyStatus;
import com.aztu.hr_erp.common.exception.NotFoundException;
import com.aztu.hr_erp.department.domain.Department;
import com.aztu.hr_erp.department.service.DepartmentService;
import com.aztu.hr_erp.vacancy.domain.Vacancy;
import com.aztu.hr_erp.vacancy.dto.CreateVacancyRequest;
import com.aztu.hr_erp.vacancy.dto.UpdateVacancyRequest;
import com.aztu.hr_erp.vacancy.dto.VacancyMapper;
import com.aztu.hr_erp.vacancy.dto.VacancyResponse;
import com.aztu.hr_erp.vacancy.repository.VacancyRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VacancyService {

    private static final Logger log = LoggerFactory.getLogger(VacancyService.class);

    private final VacancyRepository repository;
    private final DepartmentService departmentService;

    public VacancyService(VacancyRepository repository, DepartmentService departmentService) {
        this.repository = repository;
        this.departmentService = departmentService;
    }

    @Transactional
    public VacancyResponse create(CreateVacancyRequest req, UUID createdBy) {
        Codes.require(JobType.class, req.jobType());
        Codes.require(Category.class, req.category());
        // Inactive departments are hidden from new vacancies.
        Department dept = departmentService.requireActive(req.departmentId());

        Vacancy v = new Vacancy();
        v.setDepartment(dept);
        v.setJobTitle(req.jobTitle());
        v.setJobType(req.jobType());
        v.setSalary(req.salary());
        v.setCategory(req.category());
        v.setDescription(req.description());
        v.setStatus(VacancyStatus.OPEN.code());
        v.setCreatedBy(createdBy);
        v.setClosesAt(req.closesAt());
        return VacancyMapper.toResponse(repository.save(v));
    }

    @Transactional(readOnly = true)
    public PageResponse<VacancyResponse> list(Integer departmentId, String category, String status,
                                              int page, int size) {
        if (category != null) Codes.require(Category.class, category);
        if (status != null) Codes.require(VacancyStatus.class, status);
        Pageable pageable = PageRequest.of(page, size);
        Page<Vacancy> result = repository.search(departmentId, category, status, pageable);
        return PageResponse.from(result, result.map(VacancyMapper::toResponse).getContent());
    }

    @Transactional(readOnly = true)
    public PageResponse<VacancyResponse> listPublicOpen(String category, int page, int size) {
        if (category != null) Codes.require(Category.class, category);
        Pageable pageable = PageRequest.of(page, size);
        Page<Vacancy> result = repository.searchPublicOpen(category, pageable);
        return PageResponse.from(result, result.map(VacancyMapper::toResponse).getContent());
    }

    @Transactional(readOnly = true)
    public VacancyResponse get(UUID id) {
        return VacancyMapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public VacancyResponse getPublic(UUID id) {
        Vacancy v = find(id);
        if (!VacancyStatus.OPEN.code().equals(v.getStatus())) {
            throw new NotFoundException("Vacancy not found");
        }
        return VacancyMapper.toResponse(v);
    }

    @Transactional(readOnly = true)
    public Vacancy find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vacancy not found"));
    }

    @Transactional
    public VacancyResponse update(UUID id, UpdateVacancyRequest req) {
        Vacancy v = find(id);
        if (req.departmentId() != null) v.setDepartment(departmentService.find(req.departmentId()));
        if (req.jobTitle() != null) v.setJobTitle(req.jobTitle());
        if (req.jobType() != null) v.setJobType(Codes.require(JobType.class, req.jobType()));
        if (req.salary() != null) v.setSalary(req.salary());
        if (req.category() != null) v.setCategory(Codes.require(Category.class, req.category()));
        if (req.description() != null) v.setDescription(req.description());
        if (req.status() != null) v.setStatus(Codes.require(VacancyStatus.class, req.status()));
        if (req.closesAt() != null) v.setClosesAt(req.closesAt());
        return VacancyMapper.toResponse(repository.save(v));
    }

    /** open -> closed -> filled transition support; closing a vacancy. */
    @Transactional
    public VacancyResponse close(UUID id) {
        Vacancy v = find(id);
        v.setStatus(VacancyStatus.CLOSED.code());
        return VacancyMapper.toResponse(repository.save(v));
    }

    /** Scheduled auto-close of vacancies past their closes_at deadline. */
    @Transactional
    public int autoCloseExpired() {
        List<Vacancy> expired = repository.findByStatusAndClosesAtBefore(
                VacancyStatus.OPEN.code(), LocalDateTime.now());
        expired.forEach(v -> v.setStatus(VacancyStatus.CLOSED.code()));
        repository.saveAll(expired);
        if (!expired.isEmpty()) {
            log.info("Auto-closed {} expired vacancies", expired.size());
        }
        return expired.size();
    }
}
