package com.aztu.hr_erp.security;

import java.util.Set;
import java.util.UUID;

/** The authenticated caller as described by the central SSO microservice. No passwords are ever stored. */
public record SsoUser(UUID userId, Set<String> roles) {}
