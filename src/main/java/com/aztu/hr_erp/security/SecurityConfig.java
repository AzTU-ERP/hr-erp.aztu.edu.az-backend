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

/** SSO-based stateless security. Public karyera endpoints are open; HR admin endpoints require hr_admin. */
@Configuration
public class SecurityConfig {

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
                .requestMatchers("/api/**").hasRole("hr_admin")
                .anyRequest().permitAll())
            .addFilterBefore(new SsoAuthenticationFilter(ssoClient), UsernamePasswordAuthenticationFilter.class)
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
