package com.aztu.hr_erp.infrastructure.auth;

import com.aztu.hr_erp.common.exception.ApiException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Creates accounts in the central auth microservice on HR's behalf.
 *
 * <p>HR authenticates as itself through auth's client-credentials grant
 * ({@code POST /api/auth/token/client}) using {@code AUTH_CLIENT_ID}/{@code AUTH_CLIENT_SECRET},
 * rather than borrowing a human admin's token. The resulting access token is cached until shortly
 * before it expires.
 *
 * <p>Note that auth-erp serialises SNAKE_CASE while hr-erp uses camelCase, so every field here is
 * built as an explicit map with auth's wire names. Do not let these leak into HR's own DTOs.
 */
@Component
public class AuthProvisioningClient {

    private static final Logger log = LoggerFactory.getLogger(AuthProvisioningClient.class);

    /** Refresh a little early so a token cannot expire mid-flight. */
    private static final Duration EXPIRY_MARGIN = Duration.ofSeconds(60);

    private final String baseUrl;
    private final String clientId;
    private final String clientSecret;
    private final RestClient restClient = RestClient.create();

    private volatile String cachedToken;
    private volatile Instant cachedTokenExpiresAt = Instant.EPOCH;

    public AuthProvisioningClient(
            @Value("${app.integration.auth.base-url:http://localhost:8080}") String baseUrl,
            @Value("${app.integration.auth.client-id:}") String clientId,
            @Value("${app.integration.auth.client-secret:}") String clientSecret) {
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public boolean isConfigured() {
        return hasText(clientId) && hasText(clientSecret);
    }

    /**
     * Create a staff account in auth and return its {@code user_id}.
     *
     * <p>No roles are requested. A new employee needs an identity — so HR's audit columns line up
     * with {@code auth.users.user_id} — but not HR platform access; granting that stays a deliberate
     * admin action. auth also caps what a non-admin caller may grant, so asking for more here would
     * be rejected anyway.
     */
    public UUID provisionStaff(String email, String name, String surname, String fatherName,
                               String phone, String duty, String workType) {
        if (!isConfigured()) {
            throw new AuthProvisioningException(
                    "AUTH_CLIENT_ID / AUTH_CLIENT_SECRET are not configured; cannot provision in auth");
        }

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("email", email);
        request.put("name", name);
        request.put("surname", surname);
        request.put("father_name", fatherName);
        request.put("phone", phone);
        request.put("duty", duty);
        request.put("work_type", workType);

        Map<String, Object> data = postForData("/api/profiles/staff", request, accessToken());
        Object userId = data.get("user_id");
        if (userId == null) {
            throw new AuthProvisioningException("auth returned no user_id for " + email);
        }
        UUID provisioned = UUID.fromString(String.valueOf(userId));
        log.info("Provisioned auth account {} for {}", provisioned, email);
        return provisioned;
    }

    /** Cached client-credentials token, re-fetched shortly before expiry. */
    private String accessToken() {
        String token = cachedToken;
        if (token != null && Instant.now().isBefore(cachedTokenExpiresAt)) {
            return token;
        }
        synchronized (this) {
            if (cachedToken != null && Instant.now().isBefore(cachedTokenExpiresAt)) {
                return cachedToken;
            }
            Map<String, Object> credentials = new LinkedHashMap<>();
            credentials.put("client_id", clientId);
            credentials.put("client_secret", clientSecret);

            Map<String, Object> data = postForData("/api/auth/token/client", credentials, null);
            String issued = String.valueOf(data.get("access_token"));
            long expiresIn = data.get("expires_in") instanceof Number n ? n.longValue() : 0L;

            cachedToken = issued;
            cachedTokenExpiresAt = Instant.now().plusSeconds(Math.max(0, expiresIn)).minus(EXPIRY_MARGIN);
            log.debug("Obtained a client-credentials token from auth, valid for {}s", expiresIn);
            return issued;
        }
    }

    /** POST and unwrap auth's {@code {success, data, error, timestamp}} envelope. */
    @SuppressWarnings("unchecked")
    private Map<String, Object> postForData(String path, Map<String, Object> body, String bearerToken) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri(baseUrl + path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> {
                        if (bearerToken != null) {
                            h.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
                        }
                    })
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !Boolean.TRUE.equals(response.get("success"))) {
                throw new AuthProvisioningException("auth rejected " + path + ": " + describeError(response));
            }
            Object data = response.get("data");
            if (!(data instanceof Map<?, ?> map)) {
                throw new AuthProvisioningException("auth returned no data for " + path);
            }
            return (Map<String, Object>) map;
        } catch (AuthProvisioningException e) {
            throw e;
        } catch (Exception e) {
            // A stale cached token would otherwise survive an auth-side restart or secret rotation.
            invalidateToken();
            throw new AuthProvisioningException("Call to auth " + path + " failed: " + e.getMessage(), e);
        }
    }

    private void invalidateToken() {
        cachedToken = null;
        cachedTokenExpiresAt = Instant.EPOCH;
    }

    private static String describeError(Map<String, Object> response) {
        if (response == null) {
            return "empty response";
        }
        Object error = response.get("error");
        return error != null ? String.valueOf(error) : String.valueOf(response.get("message"));
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    /** Delivery failure — the outbox records it and retries up to app.integration.max-attempts. */
    public static class AuthProvisioningException extends ApiException {
        public AuthProvisioningException(String message) {
            super(HttpStatus.BAD_GATEWAY, message);
        }

        public AuthProvisioningException(String message, Throwable cause) {
            super(HttpStatus.BAD_GATEWAY, message);
            initCause(cause);
        }
    }
}
