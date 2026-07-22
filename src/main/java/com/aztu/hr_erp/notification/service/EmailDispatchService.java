package com.aztu.hr_erp.notification.service;

import com.aztu.hr_erp.application.dto.ApplicationEmailContext;
import com.aztu.hr_erp.application.service.ApplicationService;
import com.aztu.hr_erp.common.enums.EmailStatus;
import com.aztu.hr_erp.common.enums.TemplateType;
import com.aztu.hr_erp.infrastructure.email.EmailSender;
import com.aztu.hr_erp.notification.domain.HrEmailLog;
import com.aztu.hr_erp.notification.domain.HrTemplate;
import com.aztu.hr_erp.notification.repository.HrEmailLogRepository;
import com.aztu.hr_erp.notification.repository.HrTemplateRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Renders an admin-authored template, writes an hr_email_log row (pending), sends via SMTP,
 * then marks it sent/failed. Rendered bodies are cached so the retry worker can resend exactly.
 */
@Service
public class EmailDispatchService {

    private static final Logger log = LoggerFactory.getLogger(EmailDispatchService.class);

    private final HrTemplateRepository templateRepository;
    private final HrEmailLogRepository emailLogRepository;
    private final EmailSender emailSender;
    private final ApplicationService applicationService;

    /** emailId -> [subject, body] so transient SMTP failures can be retried with the exact content. */
    private final Map<UUID, String[]> renderedCache = new ConcurrentHashMap<>();

    public EmailDispatchService(HrTemplateRepository templateRepository,
                                HrEmailLogRepository emailLogRepository,
                                EmailSender emailSender,
                                ApplicationService applicationService) {
        this.templateRepository = templateRepository;
        this.emailLogRepository = emailLogRepository;
        this.emailSender = emailSender;
        this.applicationService = applicationService;
    }

    // Email logging/sending is independent of the caller's transaction (and runs from AFTER_COMMIT
    // listeners), so it always commits in its own transaction.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public HrEmailLog dispatch(TemplateType type, UUID applicantId, UUID applicationId,
                               String toEmail, Map<String, String> vars) {
        HrTemplate tpl = templateRepository
                .findFirstByTypeAndIsActiveTrueOrderByCreatedAtAsc(type.code())
                .orElse(null);
        String subject = tpl != null
                ? PlaceholderRenderer.render(tpl.getSubject(), vars)
                : PlaceholderRenderer.render(defaultSubject(type), vars);
        String body = tpl != null
                ? PlaceholderRenderer.render(tpl.getBody(), vars)
                : PlaceholderRenderer.render(defaultBody(type), vars);

        HrEmailLog row = new HrEmailLog();
        row.setApplicantId(applicantId);
        row.setApplicationId(applicationId);
        row.setTemplateId(tpl != null ? tpl.getTemplateId() : null);
        row.setToEmail(toEmail);
        row.setSubject(subject);
        row.setStatus(EmailStatus.PENDING.code());
        row = emailLogRepository.save(row);

        renderedCache.put(row.getEmailId(), new String[]{subject, body});
        attemptSend(row, subject, body);
        return row;
    }

    /** Retry path: resend using the cached body, or reconstruct from the template + application. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resend(HrEmailLog row) {
        String[] cached = renderedCache.get(row.getEmailId());
        String subject;
        String body;
        if (cached != null) {
            subject = cached[0];
            body = cached[1];
        } else {
            Map<String, String> vars = reconstructVars(row);
            HrTemplate tpl = row.getTemplateId() != null
                    ? templateRepository.findById(row.getTemplateId()).orElse(null)
                    : null;
            subject = row.getSubject() != null ? row.getSubject() : "AzTU HR";
            body = tpl != null ? PlaceholderRenderer.render(tpl.getBody(), vars) : subject;
        }
        attemptSend(row, subject, body);
    }

    private Map<String, String> reconstructVars(HrEmailLog row) {
        Map<String, String> vars = new HashMap<>();
        if (row.getApplicationId() != null) {
            try {
                ApplicationEmailContext ctx = applicationService.emailContext(row.getApplicationId());
                vars.put("name", (ctx.name() + " " + ctx.surname()).trim());
                vars.put("vacancy", ctx.vacancyTitle());
            } catch (Exception ignored) {
                // application may no longer be resolvable; resend with what we have
            }
        }
        return vars;
    }

    private void attemptSend(HrEmailLog row, String subject, String body) {
        try {
            emailSender.send(row.getToEmail(), subject, body);
            row.setStatus(EmailStatus.SENT.code());
            row.setSentAt(LocalDateTime.now());
            renderedCache.remove(row.getEmailId());
        } catch (Exception ex) {
            row.setStatus(EmailStatus.FAILED.code());
            log.warn("Email {} to {} failed: {}", row.getEmailId(), row.getToEmail(), ex.getMessage());
        }
        emailLogRepository.save(row);
    }

    private String defaultSubject(TemplateType type) {
        return switch (type) {
            case APPROVAL -> "Your application for {{vacancy}} has been approved";
            case REJECTION -> "Update on your application for {{vacancy}}";
            case ONBOARDING_STEP -> "Onboarding step for {{vacancy}}";
            case TERMINATION -> "Notice of termination";
        };
    }

    private String defaultBody(TemplateType type) {
        return switch (type) {
            case APPROVAL -> "Dear {{name}},\n\nYour application for {{vacancy}} has been approved.\n\nAzTU HR";
            case REJECTION -> "Dear {{name}},\n\nYour application for {{vacancy}} was not successful.\nReason: {{reason}}\n\nAzTU HR";
            case ONBOARDING_STEP -> "Dear {{name}},\n\nThis is an onboarding update for {{vacancy}}.\n\nAzTU HR";
            case TERMINATION -> "Dear {{name}},\n\nYour employment has been terminated.\nReason: {{reason}}\n\nAzTU HR";
        };
    }
}
