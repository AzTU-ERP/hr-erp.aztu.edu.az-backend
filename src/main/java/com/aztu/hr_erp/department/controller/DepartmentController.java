package com.aztu.hr_erp.department.controller;

import com.aztu.hr_erp.common.ApiResponse;
import com.aztu.hr_erp.department.dto.CreateDepartmentRequest;
import com.aztu.hr_erp.department.dto.DepartmentResponse;
import com.aztu.hr_erp.department.dto.UpdateDepartmentRequest;
import com.aztu.hr_erp.department.service.DepartmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<DepartmentResponse>> list(
            @RequestParam(name = "activeOnly", defaultValue = "false") boolean activeOnly) {
        return ApiResponse.ok(service.list(activeOnly));
    }

    @GetMapping("/{id}")
    public ApiResponse<DepartmentResponse> get(@PathVariable Integer id) {
        return ApiResponse.ok(service.get(id));
    }

    @PostMapping
    public ApiResponse<DepartmentResponse> create(@Valid @RequestBody CreateDepartmentRequest req) {
        return ApiResponse.ok(service.create(req), "Department created");
    }

    @PutMapping("/{id}")
    public ApiResponse<DepartmentResponse> update(@PathVariable Integer id,
                                                  @Valid @RequestBody UpdateDepartmentRequest req) {
        return ApiResponse.ok(service.update(id, req), "Department updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<DepartmentResponse> deactivate(@PathVariable Integer id) {
        return ApiResponse.ok(service.deactivate(id), "Department deactivated");
    }
}
