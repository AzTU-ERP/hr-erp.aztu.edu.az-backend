package com.aztu.hr_erp.security;

import java.util.Set;
import java.util.UUID;

/**
 * The authenticated caller as described by the central SSO microservice. No passwords are ever
 * stored.
 *
 * <p>{@code authorities} holds the token's authority strings <em>verbatim</em> — auth-erp already
 * emits its role grants {@code ROLE_}-prefixed (e.g. {@code ROLE_super_admin}) alongside bare
 * permission codes (e.g. {@code hr.staff.manage}), so nothing downstream may add a prefix of its
 * own. {@code sessionId} is auth's {@code sid} claim, kept so a future revocation signal can match
 * a session without re-parsing the token.
 */
public record SsoUser(UUID userId,
                      Set<String> authorities,
                      UUID sessionId,
                      String email,
                      String userType,
                      boolean mustChange,
                      boolean emailVerified) {
}
