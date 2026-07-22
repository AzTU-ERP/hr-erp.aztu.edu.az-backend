package com.aztu.hr_erp.application.controller;

import com.aztu.hr_erp.application.dto.ApplyForm;
import com.aztu.hr_erp.application.dto.ApplyResponse;
import com.aztu.hr_erp.application.service.ApplicationService;
import com.aztu.hr_erp.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public karyera apply surface — unauthenticated, multipart/form-data with a CV upload. */
@RestController
@RequestMapping("/api/public/applications")
public class PublicApplicationController {

    private final ApplicationService service;

    public PublicApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ApplyResponse> apply(@Valid @ModelAttribute ApplyForm form) {
        return ApiResponse.ok(
                service.apply(form.getName(), form.getSurname(), form.getFatherName(),
                        form.getEmail(), form.getPhone(), form.getVacancyId(), form.isAlumni(), form.getCv()),
                "Application submitted");
    }
}
