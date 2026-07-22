package com.aztu.hr_erp.application.controller;

import com.aztu.hr_erp.applicant.domain.ApplicantDocument;
import com.aztu.hr_erp.application.domain.Application;
import com.aztu.hr_erp.application.dto.ApplicationResponse;
import com.aztu.hr_erp.application.dto.ApplicationReviewResponse;
import com.aztu.hr_erp.application.dto.ReviewRequest;
import com.aztu.hr_erp.application.service.ApplicationService;
import com.aztu.hr_erp.common.ApiResponse;
import com.aztu.hr_erp.common.PageResponse;
import com.aztu.hr_erp.common.exception.NotFoundException;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HR admin CV screening (requires hr_admin). Lists ALL applications across all statuses. */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService service;
    private final FileStorageService fileStorageService;

    public ApplicationController(ApplicationService service, FileStorageService fileStorageService) {
        this.service = service;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ApplicationResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID vacancyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(status, vacancyId, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ApplicationResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @GetMapping("/{id}/reviews")
    public ApiResponse<List<ApplicationReviewResponse>> reviews(@PathVariable UUID id) {
        return ApiResponse.ok(service.reviews(id));
    }

    @PostMapping("/{id}/reviews")
    public ApiResponse<ApplicationResponse> decide(@PathVariable UUID id,
                                                   @Valid @RequestBody ReviewRequest req) {
        return ApiResponse.ok(service.decide(id, req, CurrentUser.id()), "Decision recorded");
    }

    @PostMapping("/{id}/withdraw")
    public ApiResponse<ApplicationResponse> withdraw(@PathVariable UUID id) {
        return ApiResponse.ok(service.withdraw(id), "Application withdrawn");
    }

    /** Download / preview the submitted CV. */
    @GetMapping("/{id}/cv")
    public ResponseEntity<Resource> downloadCv(@PathVariable UUID id) {
        Application application = service.find(id);
        ApplicantDocument cv = application.getCvDocument();
        if (cv == null) {
            throw new NotFoundException("No CV attached to this application");
        }
        Resource resource = new FileSystemResource(fileStorageService.resolve(cv.getStoragePath()));
        if (!resource.exists()) {
            throw new NotFoundException("CV file is missing on the server");
        }
        String filename = cv.getOriginalName() != null ? cv.getOriginalName() : "cv";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(cv.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
