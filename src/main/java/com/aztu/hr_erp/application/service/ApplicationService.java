package com.aztu.hr_erp.application.service;

import com.aztu.hr_erp.applicant.domain.Applicant;
import com.aztu.hr_erp.applicant.domain.ApplicantDocument;
import com.aztu.hr_erp.applicant.service.ApplicantService;
import com.aztu.hr_erp.application.domain.Application;
import com.aztu.hr_erp.application.domain.ApplicationReview;
import com.aztu.hr_erp.application.dto.ApplicationMapper;
import com.aztu.hr_erp.application.dto.ApplicationResponse;
import com.aztu.hr_erp.application.dto.ApplicationReviewResponse;
import com.aztu.hr_erp.application.dto.ApplyResponse;
import com.aztu.hr_erp.application.dto.ReviewRequest;
import com.aztu.hr_erp.application.event.ApplicationApprovedEvent;
import com.aztu.hr_erp.application.event.ApplicationRejectedEvent;
import com.aztu.hr_erp.application.repository.ApplicationRepository;
import com.aztu.hr_erp.application.repository.ApplicationReviewRepository;
import com.aztu.hr_erp.common.Codes;
import com.aztu.hr_erp.common.PageResponse;
import com.aztu.hr_erp.common.enums.ApplicationStatus;
import com.aztu.hr_erp.common.enums.Category;
import com.aztu.hr_erp.common.enums.ReviewDecision;
import com.aztu.hr_erp.common.enums.VacancyStatus;
import com.aztu.hr_erp.common.exception.BadRequestException;
import com.aztu.hr_erp.common.exception.ConflictException;
import com.aztu.hr_erp.common.exception.NotFoundException;
import com.aztu.hr_erp.infrastructure.storage.FileStorageService;
import com.aztu.hr_erp.infrastructure.storage.StoredFile;
import com.aztu.hr_erp.vacancy.domain.Vacancy;
import com.aztu.hr_erp.vacancy.service.VacancyService;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationReviewRepository reviewRepository;
    private final ApplicantService applicantService;
    private final VacancyService vacancyService;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher events;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ApplicationReviewRepository reviewRepository,
                              ApplicantService applicantService,
                              VacancyService vacancyService,
                              FileStorageService fileStorageService,
                              ApplicationEventPublisher events) {
        this.applicationRepository = applicationRepository;
        this.reviewRepository = reviewRepository;
        this.applicantService = applicantService;
        this.vacancyService = vacancyService;
        this.fileStorageService = fileStorageService;
        this.events = events;
    }

    /** Public apply flow: validate → upsert applicant → store CV → create application. */
    @Transactional
    public ApplyResponse apply(String name, String surname, String fatherName, String email,
                               String phone, UUID vacancyId, boolean isAlumni, MultipartFile cv) {
        Vacancy vacancy = vacancyService.find(vacancyId);
        if (!VacancyStatus.OPEN.code().equals(vacancy.getStatus())) {
            throw new BadRequestException("This vacancy is not open for applications");
        }
        // Category drives who may apply: alumni vacancies are restricted to alumni.
        if (Category.ALUMNI.code().equals(vacancy.getCategory()) && !isAlumni) {
            throw new BadRequestException("This vacancy is open to alumni only");
        }

        Applicant applicant = applicantService.upsertByEmail(name, surname, fatherName, email, phone);

        if (applicationRepository.existsByApplicant_ApplicantIdAndVacancy_VacancyId(
                applicant.getApplicantId(), vacancyId)) {
            throw new ConflictException("You have already applied to this vacancy");
        }

        StoredFile stored = fileStorageService.storeCv(cv, "cv/" + applicant.getApplicantId());
        ApplicantDocument cvDoc = applicantService.addCv(applicant, stored);

        Application application = new Application();
        application.setApplicant(applicant);
        application.setVacancy(vacancy);
        application.setCvDocument(cvDoc);
        application.setSource("karyera");
        application.setStatus(ApplicationStatus.SUBMITTED.code());
        application = applicationRepository.save(application);

        return new ApplyResponse(application.getApplicationId(), applicant.getApplicantId(),
                application.getStatus());
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> list(String status, UUID vacancyId, int page, int size) {
        if (status != null) Codes.require(ApplicationStatus.class, status);
        Page<Application> result = applicationRepository.search(status, vacancyId, PageRequest.of(page, size));
        return PageResponse.from(result, result.map(ApplicationMapper::toResponse).getContent());
    }

    @Transactional(readOnly = true)
    public ApplicationResponse get(UUID id) {
        return ApplicationMapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public Application find(UUID id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));
    }

    /** Loads the applicant/vacancy data the notification feature needs to render an email. */
    @Transactional(readOnly = true)
    public com.aztu.hr_erp.application.dto.ApplicationEmailContext emailContext(UUID id) {
        Application a = find(id);
        return new com.aztu.hr_erp.application.dto.ApplicationEmailContext(
                a.getApplicant().getApplicantId(),
                a.getApplicant().getEmail(),
                a.getApplicant().getName(),
                a.getApplicant().getSurname(),
                a.getVacancy().getJobTitle());
    }

    @Transactional(readOnly = true)
    public List<ApplicationReviewResponse> reviews(UUID applicationId) {
        find(applicationId);
        return reviewRepository.findByApplication_ApplicationIdOrderByReviewedAtAsc(applicationId)
                .stream().map(ApplicationMapper::toResponse).toList();
    }

    /** Records a review row (audit trail) for every decision and transitions the application. */
    @Transactional
    public ApplicationResponse decide(UUID id, ReviewRequest req, UUID reviewedBy) {
        Codes.require(ReviewDecision.class, req.decision());
        Application application = find(id);

        ApplicationReview review = new ApplicationReview();
        review.setApplication(application);
        review.setReviewedBy(reviewedBy);
        review.setDecision(req.decision());
        review.setReason(req.reason());
        reviewRepository.save(review);

        ReviewDecision decision = Codes.from(ReviewDecision.class, req.decision());
        switch (decision) {
            case SCREENING -> application.setStatus(ApplicationStatus.SCREENING.code());
            case APPROVED -> {
                application.setStatus(ApplicationStatus.APPROVED.code());
                applicationRepository.save(application);
                events.publishEvent(new ApplicationApprovedEvent(application.getApplicationId(), req.salary()));
                return ApplicationMapper.toResponse(application);
            }
            case REJECTED -> {
                if (req.reason() == null || req.reason().isBlank()) {
                    throw new BadRequestException("A reason is required for rejection");
                }
                application.setStatus(ApplicationStatus.REJECTED.code());
                applicationRepository.save(application);
                events.publishEvent(new ApplicationRejectedEvent(application.getApplicationId(), req.reason()));
                return ApplicationMapper.toResponse(application);
            }
        }
        applicationRepository.save(application);
        return ApplicationMapper.toResponse(application);
    }

    @Transactional
    public ApplicationResponse withdraw(UUID id) {
        Application application = find(id);
        application.setStatus(ApplicationStatus.WITHDRAWN.code());
        return ApplicationMapper.toResponse(applicationRepository.save(application));
    }
}
