package com.aztu.hr_erp.infrastructure.sso;

import com.aztu.hr_erp.security.SsoUser;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Validates SSO tokens issued by the central auth microservice.
 *
 * <p>Three strategies, in priority order:
 * <ol>
 *   <li><b>JWKS (preferred)</b> — auth-erp signs RS256 access tokens and publishes its public key at
 *       {@code /.well-known/jwks.json}. Verification is local: no per-request call to auth, so HR
 *       stays up when auth is briefly unavailable. The cost is that revocation is not immediate — a
 *       token revoked in auth keeps working here until it expires (auth's access TTL is 15 minutes).
 *   <li><b>Introspection (legacy fallback)</b> — retained for deployments still pointing at a remote
 *       validator. auth-erp exposes no such endpoint today.
 *   <li><b>Dev decoder (last resort)</b> — accepts an unsigned base64 {@code "userId:role1,role2"}
 *       string. Off by default and force-disabled under the {@code prod} profile.
 * </ol>
 *
 * <p>Authority strings are returned verbatim from the token: auth-erp already prefixes role grants
 * with {@code ROLE_} and emits permission codes bare, so callers must not re-prefix.
 */
@Component
public class SsoClient {

    private static final Logger log = LoggerFactory.getLogger(SsoClient.class);
    private static final String PROD_PROFILE = "prod";
    private static final String ACCESS_TOKEN_PURPOSE = "access";

    private final String introspectionUrl;
    private final boolean devMode;
    private final JwtDecoder jwtDecoder;
    private final RestClient restClient = RestClient.create();

    public SsoClient(@Value("${app.sso.jwks-uri:}") String jwksUri,
                     @Value("${app.sso.issuer:}") String issuer,
                     @Value("${app.sso.introspection-url:}") String introspectionUrl,
                     @Value("${app.sso.dev-mode:false}") boolean devMode,
                     Environment environment) {
        this.introspectionUrl = introspectionUrl;
        this.jwtDecoder = hasText(jwksUri) ? buildDecoder(jwksUri, issuer) : null;

        // Dev mode accepts unsigned, self-asserted tokens — it must never survive into production,
        // whatever the environment happens to say.
        boolean prod = environment.matchesProfiles(PROD_PROFILE);
        if (devMode && prod) {
            log.error("app.sso.dev-mode=true is ignored under the '{}' profile — unsigned SSO tokens "
                    + "are never accepted in production.", PROD_PROFILE);
        }
        this.devMode = devMode && !prod;

        if (this.jwtDecoder == null && !hasText(introspectionUrl) && !this.devMode) {
            log.error("No SSO validation strategy configured: set app.sso.jwks-uri (preferred) or "
                    + "app.sso.introspection-url. Every authenticated request will be rejected.");
        }
    }

    private static JwtDecoder buildDecoder(String jwksUri, String issuer) {
        // Lazy: the key set is fetched on first use (and cached/refreshed), so an unreachable auth
        // service does not block startup here.
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(hasText(issuer)
                ? JwtValidators.createDefaultWithIssuer(issuer)
                : JwtValidators.createDefault());
        log.info("SSO tokens will be verified locally against {}", jwksUri);
        return decoder;
    }

    public Optional<SsoUser> validate(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        if (jwtDecoder != null) {
            return validateJwt(token);
        }
        if (hasText(introspectionUrl)) {
            return validateRemote(token);
        }
        if (devMode) {
            return decodeDev(token);
        }
        return Optional.empty();
    }

    /** Verify signature, issuer and expiry locally against auth-erp's published JWKS. */
    private Optional<SsoUser> validateJwt(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);

            // auth-erp also mints short-lived purpose=mfa challenge tokens; those must not
            // authenticate a request here any more than they do there.
            String purpose = jwt.getClaimAsString("purpose");
            if (!ACCESS_TOKEN_PURPOSE.equals(purpose)) {
                log.debug("Rejected SSO token with purpose={}", purpose);
                return Optional.empty();
            }

            Set<String> authorities = new LinkedHashSet<>();
            List<String> claim = jwt.getClaimAsStringList("authorities");
            if (claim != null) {
                authorities.addAll(claim);
            }
            String sid = jwt.getClaimAsString("sid");

            return Optional.of(new SsoUser(
                    UUID.fromString(jwt.getSubject()),
                    authorities,
                    sid == null ? null : UUID.fromString(sid),
                    jwt.getClaimAsString("email"),
                    jwt.getClaimAsString("user_type"),
                    Boolean.TRUE.equals(jwt.getClaim("must_change")),
                    Boolean.TRUE.equals(jwt.getClaim("email_verified"))));
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("SSO token verification failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<SsoUser> validateRemote(String token) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri(introspectionUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(Map.class);
            if (body == null) return Optional.empty();
            // Fail CLOSED: a response that does not positively assert active=true is not a valid
            // token. (This previously accepted a response with the key missing entirely.)
            if (!Boolean.TRUE.equals(body.get("active"))) return Optional.empty();

            UUID userId = UUID.fromString(String.valueOf(body.getOrDefault("user_id", body.get("sub"))));

            // Per the introspection contract, "roles" are bare role codes and need the ROLE_ prefix
            // that authority strings carry; "authorities" are already fully formed.
            Object rolesRaw = body.get("roles");
            boolean bareRoles = rolesRaw != null;
            if (rolesRaw == null) {
                rolesRaw = body.get("authorities");
            }
            Set<String> authorities = new LinkedHashSet<>();
            if (rolesRaw instanceof Iterable<?> it) {
                it.forEach(r -> authorities.add(bareRoles ? "ROLE_" + r : String.valueOf(r)));
            }
            return Optional.of(new SsoUser(userId, authorities, null, null, null, false, true));
        } catch (Exception ex) {
            log.warn("SSO introspection failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private Optional<SsoUser> decodeDev(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":", 2);
            UUID userId = UUID.fromString(parts[0].trim());
            Set<String> authorities = new LinkedHashSet<>();
            if (parts.length > 1) {
                Arrays.stream(parts[1].split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        // Dev tokens carry bare role codes; mirror the ROLE_ prefix auth-erp emits.
                        .forEach(r -> authorities.add(r.startsWith("ROLE_") || r.contains(".") ? r : "ROLE_" + r));
            }
            return Optional.of(new SsoUser(userId, authorities, null, null, null, false, true));
        } catch (Exception ex) {
            log.debug("Dev SSO token decode failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
