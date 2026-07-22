package com.aztu.hr_erp.common.enums;
import com.aztu.hr_erp.common.CodedEnum;
public enum EventType implements CodedEnum {
    EMPLOYEE_APPROVED("employee_approved"), SALARY_ASSIGNED("salary_assigned"),
    SCHEDULE_UPDATED("schedule_updated"), STAFF_CREATED("staff_created"),
    EMPLOYEE_TERMINATED("employee_terminated");
    private final String code; EventType(String c){this.code=c;}
    public String code(){return code;}
}
