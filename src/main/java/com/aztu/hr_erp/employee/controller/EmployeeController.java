package com.aztu.hr_erp.employee.controller;

import com.aztu.hr_erp.common.ApiResponse;
import com.aztu.hr_erp.common.PageResponse;
import com.aztu.hr_erp.common.exception.NotFoundException;
import com.aztu.hr_erp.employee.domain.EmployeeDocument;
import com.aztu.hr_erp.employee.dto.AssignSalaryRequest;
import com.aztu.hr_erp.employee.dto.CreateScheduleRequest;
import com.aztu.hr_erp.employee.dto.EmployeeDocumentResponse;
import com.aztu.hr_erp.employee.dto.EmployeeResponse;
import com.aztu.hr_erp.employee.dto.EmployeeScheduleResponse;
import com.aztu.hr_erp.employee.dto.ProvisionUserRequest;
import com.aztu.hr_erp.employee.service.EmployeeService;
import com.aztu.hr_erp.infrastructure.storage.FileStorageService;
import com.aztu.hr_erp.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;
    private final FileStorageService fileStorageService;

    public EmployeeController(EmployeeService service, FileStorageService fileStorageService) {
        this.service = service;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ApiResponse<PageResponse<EmployeeResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(status, departmentId, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<EmployeeResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @PatchMapping("/{id}/salary")
    public ApiResponse<EmployeeResponse> assignSalary(@PathVariable UUID id,
                                                      @Valid @RequestBody AssignSalaryRequest req) {
        return ApiResponse.ok(service.assignSalary(id, req.salary()), "Salary assigned");
    }

    @PatchMapping("/{id}/user")
    public ApiResponse<EmployeeResponse> provisionUser(@PathVariable UUID id,
                                                       @Valid @RequestBody ProvisionUserRequest req) {
        return ApiResponse.ok(service.provisionUser(id, req.userId()), "User provisioned");
    }

    // ---- onboarding documents ----

    @GetMapping("/{id}/documents")
    public ApiResponse<List<EmployeeDocumentResponse>> documents(@PathVariable UUID id) {
        return ApiResponse.ok(service.documents(id));
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<EmployeeDocumentResponse> uploadDocument(
            @PathVariable UUID id,
            @RequestParam String docType,
            @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(service.uploadDocument(id, docType, CurrentUser.id(), file), "Document uploaded");
    }

    @GetMapping("/{id}/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable UUID id, @PathVariable UUID documentId) {
        EmployeeDocument doc = service.findDocument(documentId);
        Resource resource = new FileSystemResource(fileStorageService.resolve(doc.getStoragePath()));
        if (!resource.exists()) {
            throw new NotFoundException("Document file is missing on the server");
        }
        String filename = doc.getOriginalName() != null ? doc.getOriginalName() : "document";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    // ---- schedules ----

    @GetMapping("/{id}/schedules")
    public ApiResponse<List<EmployeeScheduleResponse>> schedules(@PathVariable UUID id) {
        return ApiResponse.ok(service.schedules(id));
    }

    @PostMapping("/{id}/schedules")
    public ApiResponse<EmployeeScheduleResponse> addSchedule(@PathVariable UUID id,
                                                             @RequestBody CreateScheduleRequest req) {
        return ApiResponse.ok(service.addSchedule(id, req), "Schedule added");
    }
}
