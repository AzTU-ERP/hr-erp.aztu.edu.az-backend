package com.aztu.hr_erp.application.event;

import java.math.BigDecimal;
import java.util.UUID;

/** Published when an application is approved; employee + notification features react. */
public record ApplicationApprovedEvent(UUID applicationId, BigDecimal salary) {}
