package com.aztu.hr_erp.notification.service;

import com.aztu.hr_erp.common.Codes;
import com.aztu.hr_erp.common.enums.TemplateType;
import com.aztu.hr_erp.common.exception.NotFoundException;
import com.aztu.hr_erp.notification.domain.HrTemplate;
import com.aztu.hr_erp.notification.dto.CreateTemplateRequest;
import com.aztu.hr_erp.notification.dto.NotificationMapper;
import com.aztu.hr_erp.notification.dto.TemplateResponse;
import com.aztu.hr_erp.notification.dto.UpdateTemplateRequest;
import com.aztu.hr_erp.notification.repository.HrTemplateRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TemplateService {

    private final HrTemplateRepository repository;

    public TemplateService(HrTemplateRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> list(String type) {
        List<HrTemplate> all = type != null
                ? repository.findByTypeOrderByCreatedAtDesc(Codes.require(TemplateType.class, type))
                : repository.findAllByOrderByCreatedAtDesc();
        return all.stream().map(NotificationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TemplateResponse get(UUID id) {
        return NotificationMapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public HrTemplate find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Template not found"));
    }

    @Transactional
    public TemplateResponse create(CreateTemplateRequest req, UUID createdBy) {
        Codes.require(TemplateType.class, req.type());
        HrTemplate t = new HrTemplate();
        t.setType(req.type());
        t.setName(req.name());
        t.setSubject(req.subject());
        t.setBody(req.body());
        t.setCreatedBy(createdBy);
        t.setIsActive(true);
        return NotificationMapper.toResponse(repository.save(t));
    }

    @Transactional
    public TemplateResponse update(UUID id, UpdateTemplateRequest req) {
        HrTemplate t = find(id);
        if (req.name() != null) t.setName(req.name());
        if (req.subject() != null) t.setSubject(req.subject());
        if (req.body() != null) t.setBody(req.body());
        if (req.isActive() != null) t.setIsActive(req.isActive());
        return NotificationMapper.toResponse(repository.save(t));
    }

    @Transactional
    public TemplateResponse deactivate(UUID id) {
        HrTemplate t = find(id);
        t.setIsActive(false);
        return NotificationMapper.toResponse(repository.save(t));
    }
}
