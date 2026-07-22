package com.aztu.hr_erp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables the scheduled outbox/email workers and async email dispatch. */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncSchedulingConfig {
}
