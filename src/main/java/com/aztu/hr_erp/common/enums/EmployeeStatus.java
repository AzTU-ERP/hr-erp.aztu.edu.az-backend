package com.aztu.hr_erp.common.enums;
import com.aztu.hr_erp.common.CodedEnum;
public enum EmployeeStatus implements CodedEnum {
    ONBOARDING("onboarding"), ACTIVE("active"), TERMINATED("terminated");
    private final String code; EmployeeStatus(String c){this.code=c;}
    public String code(){return code;}
}
