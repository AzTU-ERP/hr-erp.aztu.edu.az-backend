package com.aztu.hr_erp.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * Explicit multipart support for CV / document uploads. Declared directly because the
 * Spring Cloud Gateway (server-webmvc) starter on the classpath suppresses the multipart
 * auto-configuration, which would otherwise parse multipart/form-data requests.
 */
@Configuration
public class MultipartConfig {

    private static final long MAX_FILE = 15L * 1024 * 1024;   // 15 MB
    private static final long MAX_REQUEST = 20L * 1024 * 1024; // 20 MB

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        // location="" (default temp dir), no per-file/in-memory threshold limit beyond the sizes below
        return new MultipartConfigElement("", MAX_FILE, MAX_REQUEST, 0);
    }

    @Bean(name = "multipartResolver")
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
