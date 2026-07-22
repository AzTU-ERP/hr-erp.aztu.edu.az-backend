package com.aztu.hr_erp.application.dto;

import java.util.UUID;

/** Minimal context the notification feature needs to build an email for an application. */
public record ApplicationEmailContext(
        UUID applicantId,
        String email,
        String name,
        String surname,
        String vacancyTitle) {}
