package com.aztu.hr_erp.common.enums;
import com.aztu.hr_erp.common.CodedEnum;
public enum TemplateType implements CodedEnum {
    APPROVAL("approval"), REJECTION("rejection"),
    ONBOARDING_STEP("onboarding_step"), TERMINATION("termination");
    private final String code; TemplateType(String c){this.code=c;}
    public String code(){return code;}
}
