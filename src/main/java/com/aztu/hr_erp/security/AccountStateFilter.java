package com.aztu.hr_erp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Enforces account state carried in the SSO token, mirroring auth-erp's own {@code AccountStateFilter}.
 * A caller who must rotate their password — or a non-student whose email is unverified — is blocked
 * with a 403 rather than being let into HR.
 *
 * <p>auth-erp blocks these accounts at its own edge, but its token stays otherwise valid, so without
 * this filter such a user walked straight into HR. Remediation lives in auth (HR has no password or
 * email endpoints of its own), so there is no allowlist here — only the public karyera surface is
 * exempt, since that is reachable without a token at all.
 *
 * <p>Registered after {@link SsoAuthenticationFilter} so the principal is already populated.
 */
public class AccountStateFilter extends OncePerRequestFilter {

    /** auth-erp does not require students to verify their email before using a platform. */
    private static final String STUDENT_USER_TYPE = "student";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();

        if (!(principal instanceof SsoUser user) || !isEnforced(request)) {
            chain.doFilter(request, response);
            return;
        }

        if (user.mustChange()) {
            write(response, "You must change your password in the central auth service before using HR");
            return;
        }

        boolean requiresVerification = user.userType() != null && !STUDENT_USER_TYPE.equals(user.userType());
        if (requiresVerification && !user.emailVerified()) {
            write(response, "Verify your email address in the central auth service before using HR");
            return;
        }

        chain.doFilter(request, response);
    }

    /** Enforce exactly where SecurityConfig requires authorization — never on the public surface. */
    private static boolean isEnforced(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        return uri.startsWith("/api/") && !uri.startsWith("/api/public");
    }

    private void write(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpStatus.FORBIDDEN.value());
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String safe = message.replace("\\", "\\\\").replace("\"", "\\\"");
        res.getWriter().write("{\"success\":false,\"message\":\"" + safe + "\",\"data\":null}");
    }
}
