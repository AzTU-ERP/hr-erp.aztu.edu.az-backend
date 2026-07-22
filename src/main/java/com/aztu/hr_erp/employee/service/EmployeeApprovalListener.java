package com.aztu.hr_erp.employee.service;

import com.aztu.hr_erp.application.event.ApplicationApprovedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reacts to an approved application synchronously (within the approval transaction) so the
 * employee record and its outbox events are created atomically with the approval.
 */
@Component
public class EmployeeApprovalListener {

    private final EmployeeService employeeService;

    public EmployeeApprovalListener(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @EventListener
    public void onApproved(ApplicationApprovedEvent event) {
        employeeService.createFromApproval(event.applicationId(), event.salary());
    }
}
