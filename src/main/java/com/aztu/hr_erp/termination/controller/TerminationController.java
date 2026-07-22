package com.aztu.hr_erp.termination.controller;

import com.aztu.hr_erp.common.ApiResponse;
import com.aztu.hr_erp.security.CurrentUser;
import com.aztu.hr_erp.termination.dto.TerminateRequest;
import com.aztu.hr_erp.termination.dto.TerminationDocumentResponse;
import com.aztu.hr_erp.termination.dto.TerminationResponse;
import com.aztu.hr_erp.termination.service.TerminationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class TerminationController {

    private final TerminationService service;

    public TerminationController(TerminationService service) {
        this.service = service;
    }

    @PostMapping("/employees/{id}/terminate")
    public ApiResponse<TerminationResponse> terminate(@PathVariable UUID id,
                                                      @Valid @RequestBody TerminateRequest req) {
        return ApiResponse.ok(
                service.terminate(id, req.reason(), req.effectiveDate(), CurrentUser.id()),
                "Employee terminated");
    }

    @GetMapping("/employees/{id}/termination")
    public ApiResponse<TerminationResponse> getByEmployee(@PathVariable UUID id) {
        return ApiResponse.ok(service.getByEmployee(id));
    }

    @GetMapping("/terminations/{terminationId}/documents")
    public ApiResponse<List<TerminationDocumentResponse>> documents(@PathVariable UUID terminationId) {
        return ApiResponse.ok(service.documents(terminationId));
    }

    @PostMapping(value = "/terminations/{terminationId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<TerminationDocumentResponse> addDocument(
            @PathVariable UUID terminationId,
            @RequestParam String docType,
            @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(service.addDocument(terminationId, docType, file), "Document uploaded");
    }
}
