package com.aztu.hr_erp.notification.controller;

import com.aztu.hr_erp.common.ApiResponse;
import com.aztu.hr_erp.common.Codes;
import com.aztu.hr_erp.common.PageResponse;
import com.aztu.hr_erp.common.enums.EmailStatus;
import com.aztu.hr_erp.notification.domain.HrEmailLog;
import com.aztu.hr_erp.notification.dto.EmailLogResponse;
import com.aztu.hr_erp.notification.dto.NotificationMapper;
import com.aztu.hr_erp.notification.repository.HrEmailLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** hr_email_log delivery status view. */
@RestController
@RequestMapping("/api/emails")
public class EmailLogController {

    private final HrEmailLogRepository repository;

    public EmailLogController(HrEmailLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ApiResponse<PageResponse<EmailLogResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (status != null) Codes.require(EmailStatus.class, status);
        Page<HrEmailLog> result = repository.search(status, PageRequest.of(page, size));
        return ApiResponse.ok(PageResponse.from(result,
                result.map(NotificationMapper::toResponse).getContent()));
    }
}
