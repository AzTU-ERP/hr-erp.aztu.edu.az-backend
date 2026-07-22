package com.aztu.hr_erp.integration.service;

import com.aztu.hr_erp.common.enums.IntegrationStatus;
import com.aztu.hr_erp.common.enums.TargetSystem;
import com.aztu.hr_erp.integration.domain.IntegrationEvent;
import com.aztu.hr_erp.integration.repository.IntegrationEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

/**
 * Polls integration_events (pending/failed) and delivers the JSON payload to Finance or Turnstile,
 * tracking attempts, last_error and sent_at. Honors a max-attempts ceiling (backoff via attempt count).
 */
@Component
public class IntegrationOutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(IntegrationOutboxWorker.class);

    private final IntegrationEventRepository repository;
    private final String financeBaseUrl;
    private final String turnstileBaseUrl;
    private final int maxAttempts;
    private final RestClient restClient = RestClient.create();

    public IntegrationOutboxWorker(IntegrationEventRepository repository,
                                   @Value("${app.integration.finance.base-url:http://localhost:9101}") String financeBaseUrl,
                                   @Value("${app.integration.turnstile.base-url:http://localhost:9102}") String turnstileBaseUrl,
                                   @Value("${app.integration.max-attempts:5}") int maxAttempts) {
        this.repository = repository;
        this.financeBaseUrl = financeBaseUrl;
        this.turnstileBaseUrl = turnstileBaseUrl;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${app.integration.poll-interval-ms:60000}")
    @Transactional
    public void deliverPending() {
        List<IntegrationEvent> due = repository.findByStatusInAndAttemptsLessThanOrderByCreatedAtAsc(
                List.of(IntegrationStatus.PENDING.code(), IntegrationStatus.FAILED.code()), maxAttempts);
        for (IntegrationEvent event : due) {
            deliver(event);
        }
    }

    private void deliver(IntegrationEvent event) {
        event.setAttempts(event.getAttempts() == null ? 1 : event.getAttempts() + 1);
        try {
            String baseUrl = TargetSystem.FINANCE.code().equals(event.getTargetSystem())
                    ? financeBaseUrl : turnstileBaseUrl;
            restClient.post()
                    .uri(baseUrl + "/api/hr-events")
                    .header("X-Event-Type", event.getEventType())
                    .header("X-Target-System", event.getTargetSystem())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(event.getPayload())
                    .retrieve()
                    .toBodilessEntity();
            event.setStatus(IntegrationStatus.SENT.code());
            event.setSentAt(LocalDateTime.now());
            event.setLastError(null);
            log.info("Delivered {} event {} to {}", event.getEventType(), event.getEventId(), event.getTargetSystem());
        } catch (Exception ex) {
            event.setStatus(IntegrationStatus.FAILED.code());
            event.setLastError(truncate(ex.getMessage()));
            log.warn("Delivery of event {} failed (attempt {}): {}",
                    event.getEventId(), event.getAttempts(), ex.getMessage());
        }
        repository.save(event);
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > 1000 ? s.substring(0, 1000) : s;
    }
}
