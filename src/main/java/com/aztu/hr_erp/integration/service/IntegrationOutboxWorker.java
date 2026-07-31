package com.aztu.hr_erp.integration.service;

import com.aztu.hr_erp.common.enums.IntegrationStatus;
import com.aztu.hr_erp.common.enums.TargetSystem;
import com.aztu.hr_erp.employee.domain.Employee;
import com.aztu.hr_erp.employee.repository.EmployeeRepository;
import com.aztu.hr_erp.infrastructure.auth.AuthProvisioningClient;
import com.aztu.hr_erp.integration.domain.IntegrationEvent;
import com.aztu.hr_erp.integration.repository.IntegrationEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

/**
 * Polls integration_events (pending/failed) and delivers each one, tracking attempts, last_error and
 * sent_at. Honors a max-attempts ceiling (backoff via attempt count).
 *
 * <p>Finance and Turnstile take a fire-and-forget POST of the JSON payload. Auth is different: it
 * answers with the {@code user_id} it created, which has to be written back to the employee — so the
 * outbox is what gives that cross-service call its retries and failure record.
 */
@Component
public class IntegrationOutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(IntegrationOutboxWorker.class);

    private final IntegrationEventRepository repository;
    private final EmployeeRepository employeeRepository;
    private final AuthProvisioningClient authProvisioningClient;
    private final ObjectMapper objectMapper;
    private final String financeBaseUrl;
    private final String turnstileBaseUrl;
    private final int maxAttempts;
    private final RestClient restClient = RestClient.create();

    public IntegrationOutboxWorker(IntegrationEventRepository repository,
                                   EmployeeRepository employeeRepository,
                                   AuthProvisioningClient authProvisioningClient,
                                   ObjectMapper objectMapper,
                                   @Value("${app.integration.finance.base-url:http://localhost:9101}") String financeBaseUrl,
                                   @Value("${app.integration.turnstile.base-url:http://localhost:9102}") String turnstileBaseUrl,
                                   @Value("${app.integration.max-attempts:5}") int maxAttempts) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.authProvisioningClient = authProvisioningClient;
        this.objectMapper = objectMapper;
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
            if (TargetSystem.AUTH.code().equals(event.getTargetSystem())) {
                provisionInAuth(event);
            } else {
                postToPeer(event);
            }
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

    /** Fire-and-forget delivery to a peer platform. */
    private void postToPeer(IntegrationEvent event) {
        // Explicit per-target routing: a ternary would have silently sent anything that is not
        // Finance — including auth events — to Turnstile.
        String baseUrl;
        if (TargetSystem.FINANCE.code().equals(event.getTargetSystem())) {
            baseUrl = financeBaseUrl;
        } else if (TargetSystem.TURNSTILE.code().equals(event.getTargetSystem())) {
            baseUrl = turnstileBaseUrl;
        } else {
            throw new IllegalStateException("No route configured for target system " + event.getTargetSystem());
        }
        restClient.post()
                .uri(baseUrl + "/api/hr-events")
                .header("X-Event-Type", event.getEventType())
                .header("X-Target-System", event.getTargetSystem())
                .contentType(MediaType.APPLICATION_JSON)
                .body(event.getPayload())
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Create the employee's account in auth and store the returned user_id. Idempotent: an employee
     * that already carries a user_id is treated as delivered, so a retry after a partial failure
     * never provisions a second account.
     */
    private void provisionInAuth(IntegrationEvent event) {
        Employee employee = employeeRepository.findById(event.getEmployeeId())
                .orElseThrow(() -> new IllegalStateException(
                        "Employee " + event.getEmployeeId() + " no longer exists"));
        if (employee.getUserId() != null) {
            log.debug("Employee {} already has auth user {}; nothing to provision",
                    employee.getEmployeeId(), employee.getUserId());
            return;
        }

        Map<String, Object> payload = objectMapper.readValue(event.getPayload(), Map.class);
        UUID userId = authProvisioningClient.provisionStaff(
                str(payload.get("email")),
                str(payload.get("name")),
                str(payload.get("surname")),
                str(payload.get("fatherName")),
                str(payload.get("phone")),
                str(payload.get("jobTitle")),
                str(payload.get("jobType")));

        employee.setUserId(userId);
        employeeRepository.save(employee);
        log.info("Linked employee {} to auth user {}", employee.getEmployeeId(), userId);
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > 1000 ? s.substring(0, 1000) : s;
    }
}
