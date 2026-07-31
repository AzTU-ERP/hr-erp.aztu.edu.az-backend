package com.aztu.hr_erp.security;

import com.aztu.hr_erp.infrastructure.sso.SsoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * SSO-based stateless security. Public karyera endpoints are open; everything under {@code /api}
 * requires an auth-erp token carrying HR access.
 *
 * <p>Authorization keys off the <em>permission</em> {@code hr.staff.manage} rather than a role name.
 * auth-erp's role codes are not platform-scoped — every platform has its own {@code admin} role and
 * they all collapse to the authority {@code ROLE_admin} — so a role check cannot tell an HR admin
 * from an LMS admin. Its permission codes <em>are</em> platform-scoped: {@code hr.staff.manage} is
 * seeded only under the HR platform and granted to the HR admin and hr_officer roles, so an LMS-only
 * admin never holds it. {@code ROLE_super_admin} is global and bypasses platform scoping by design.
 */
@Configuration
public class SecurityConfig {

    /** Global cross-platform administrator in auth-erp; reaches every platform by design. */
    private static final String SUPER_ADMIN = "ROLE_super_admin";
    /** HR-platform permission held by the HR admin and hr_officer roles, and by nobody else. */
    private static final String HR_ACCESS = "hr.staff.manage";

    private final SsoClient ssoClient;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(SsoClient ssoClient, CorsConfigurationSource corsConfigurationSource) {
        this.ssoClient = ssoClient;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/error").permitAll()
                // OpenAPI spec + Swagger UI (springdoc)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/**").hasAnyAuthority(SUPER_ADMIN, HR_ACCESS)
                .anyRequest().authenticated())
            // SSO auth first, then account-state enforcement on the resulting principal.
            .addFilterBefore(new SsoAuthenticationFilter(ssoClient), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(new AccountStateFilter(), SsoAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> write(res, HttpStatus.UNAUTHORIZED, "Authentication is required"))
                .accessDeniedHandler((req, res, e) -> write(res, HttpStatus.FORBIDDEN, "You are not authorized to perform this action")));
        return http.build();
    }

    private void write(jakarta.servlet.http.HttpServletResponse res, HttpStatus status, String message) throws java.io.IOException {
        res.setStatus(status.value());
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String safe = message.replace("\\", "\\\\").replace("\"", "\\\"");
        res.getWriter().write("{\"success\":false,\"message\":\"" + safe + "\",\"data\":null}");
    }
}
