package com.aztu.hr_erp.application.event;

import java.util.UUID;

/** Published when an application is rejected; the notification feature emails the reason. */
public record ApplicationRejectedEvent(UUID applicationId, String reason) {}
