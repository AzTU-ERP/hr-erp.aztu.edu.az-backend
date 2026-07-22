package com.aztu.hr_erp.application.dto;

import java.util.UUID;

/** Returned to the public karyera surface after a successful application. */
public record ApplyResponse(UUID applicationId, UUID applicantId, String status) {}
