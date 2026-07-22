package com.aztu.hr_erp.vacancy.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically auto-closes vacancies whose closes_at deadline has passed. */
@Component
public class VacancyScheduler {

    private final VacancyService vacancyService;

    public VacancyScheduler(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    @Scheduled(fixedDelayString = "${app.vacancy.auto-close-interval-ms:300000}")
    public void autoClose() {
        vacancyService.autoCloseExpired();
    }
}
