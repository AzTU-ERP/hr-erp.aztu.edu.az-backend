package com.aztu.hr_erp.security;

import com.aztu.hr_erp.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Convenience accessor for the authenticated caller's external auth user_id. */
public final class CurrentUser {
    private CurrentUser() {}

    private static final class UnauthorizedException extends ApiException {
        UnauthorizedException() { super(HttpStatus.UNAUTHORIZED, "Authentication is required"); }
    }

    public static SsoUser get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SsoUser u) {
            return u;
        }
        throw new UnauthorizedException();
    }

    public static UUID id() {
        return get().userId();
    }
}
