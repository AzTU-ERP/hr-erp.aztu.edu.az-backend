package com.aztu.hr_erp.notification.service;

import com.aztu.hr_erp.application.dto.ApplicationEmailContext;
import com.aztu.hr_erp.application.event.ApplicationApprovedEvent;
import com.aztu.hr_erp.application.event.ApplicationRejectedEvent;
import com.aztu.hr_erp.application.service.ApplicationService;
import com.aztu.hr_erp.common.enums.TemplateType;
import java.util.HashMap;
import java.util.Map;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

/** Sends approval/rejection emails after the decision transaction commits. */
@Component
public class HrEmailEventListener {

    private final ApplicationService applicationService;
    private final EmailDispatchService emailDispatchService;

    public HrEmailEventListener(ApplicationService applicationService,
                                EmailDispatchService emailDispatchService) {
        this.applicationService = applicationService;
        this.emailDispatchService = emailDispatchService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApproved(ApplicationApprovedEvent event) {
        ApplicationEmailContext ctx = applicationService.emailContext(event.applicationId());
        Map<String, String> vars = new HashMap<>();
        vars.put("name", (ctx.name() + " " + ctx.surname()).trim());
        vars.put("vacancy", ctx.vacancyTitle());
        emailDispatchService.dispatch(TemplateType.APPROVAL, ctx.applicantId(),
                event.applicationId(), ctx.email(), vars);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRejected(ApplicationRejectedEvent event) {
        ApplicationEmailContext ctx = applicationService.emailContext(event.applicationId());
        Map<String, String> vars = new HashMap<>();
        vars.put("name", (ctx.name() + " " + ctx.surname()).trim());
        vars.put("vacancy", ctx.vacancyTitle());
        vars.put("reason", event.reason());
        emailDispatchService.dispatch(TemplateType.REJECTION, ctx.applicantId(),
                event.applicationId(), ctx.email(), vars);
    }
}
