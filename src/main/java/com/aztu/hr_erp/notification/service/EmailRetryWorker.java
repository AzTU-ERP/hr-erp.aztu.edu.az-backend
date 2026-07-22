package com.aztu.hr_erp.notification.service;

import com.aztu.hr_erp.common.enums.EmailStatus;
import com.aztu.hr_erp.notification.domain.HrEmailLog;
import com.aztu.hr_erp.notification.repository.HrEmailLogRepository;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically retries hr_email_log rows still pending or failed. */
@Component
public class EmailRetryWorker {

    private final HrEmailLogRepository emailLogRepository;
    private final EmailDispatchService emailDispatchService;

    public EmailRetryWorker(HrEmailLogRepository emailLogRepository,
                            EmailDispatchService emailDispatchService) {
        this.emailLogRepository = emailLogRepository;
        this.emailDispatchService = emailDispatchService;
    }

    @Scheduled(fixedDelayString = "${app.email.retry-interval-ms:120000}")
    public void retryUnsent() {
        List<HrEmailLog> unsent = emailLogRepository.findByStatusInOrderByCreatedAtAsc(
                List.of(EmailStatus.PENDING.code(), EmailStatus.FAILED.code()));
        for (HrEmailLog row : unsent) {
            emailDispatchService.resend(row);
        }
    }
}
