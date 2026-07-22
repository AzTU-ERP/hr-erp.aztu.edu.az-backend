package com.aztu.hr_erp.department.service;

import com.aztu.hr_erp.common.exception.ConflictException;
import com.aztu.hr_erp.common.exception.NotFoundException;
import com.aztu.hr_erp.department.domain.Department;
import com.aztu.hr_erp.department.dto.CreateDepartmentRequest;
import com.aztu.hr_erp.department.dto.DepartmentMapper;
import com.aztu.hr_erp.department.dto.DepartmentResponse;
import com.aztu.hr_erp.department.dto.UpdateDepartmentRequest;
import com.aztu.hr_erp.department.repository.DepartmentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> list(boolean activeOnly) {
        List<Department> all = activeOnly
                ? repository.findByIsActiveTrueOrderByNameAsc()
                : repository.findAllByOrderByNameAsc();
        return all.stream().map(DepartmentMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse get(Integer id) {
        return DepartmentMapper.toResponse(find(id));
    }

    /** Returns the entity for cross-feature use (vacancy/employee), enforcing existence + active where needed. */
    @Transactional(readOnly = true)
    public Department requireActive(Integer id) {
        Department d = find(id);
        if (!Boolean.TRUE.equals(d.getIsActive())) {
            throw new ConflictException("Department is not active");
        }
        return d;
    }

    @Transactional(readOnly = true)
    public Department find(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));
    }

    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest req) {
        if (repository.existsByCode(req.code())) {
            throw new ConflictException("Department code already exists: " + req.code());
        }
        Department d = new Department();
        d.setCode(req.code());
        d.setName(req.name());
        d.setIsActive(true);
        return DepartmentMapper.toResponse(repository.save(d));
    }

    @Transactional
    public DepartmentResponse update(Integer id, UpdateDepartmentRequest req) {
        Department d = find(id);
        d.setName(req.name());
        if (req.isActive() != null) {
            d.setIsActive(req.isActive());
        }
        return DepartmentMapper.toResponse(repository.save(d));
    }

    /** Inactive departments are hidden from new vacancies. */
    @Transactional
    public DepartmentResponse deactivate(Integer id) {
        Department d = find(id);
        d.setIsActive(false);
        return DepartmentMapper.toResponse(repository.save(d));
    }
}
