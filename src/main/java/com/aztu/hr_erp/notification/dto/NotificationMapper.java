package com.aztu.hr_erp.notification.dto;

import com.aztu.hr_erp.notification.domain.HrEmailLog;
import com.aztu.hr_erp.notification.domain.HrTemplate;

public final class NotificationMapper {
    private NotificationMapper() {}

    public static TemplateResponse toResponse(HrTemplate t) {
        return new TemplateResponse(t.getTemplateId(), t.getType(), t.getName(), t.getSubject(),
                t.getBody(), t.getCreatedBy(), Boolean.TRUE.equals(t.getIsActive()), t.getCreatedAt());
    }

    public static EmailLogResponse toResponse(HrEmailLog e) {
        return new EmailLogResponse(e.getEmailId(), e.getApplicantId(), e.getApplicationId(),
                e.getTemplateId(), e.getToEmail(), e.getSubject(), e.getStatus(), e.getSentAt(), e.getCreatedAt());
    }
}
