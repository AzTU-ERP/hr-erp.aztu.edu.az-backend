package com.aztu.hr_erp.common.enums;
import com.aztu.hr_erp.common.CodedEnum;
public enum TargetSystem implements CodedEnum {
    FINANCE("finance"), TURNSTILE("turnstile"), AUTH("auth");
    private final String code; TargetSystem(String c){this.code=c;}
    public String code(){return code;}
}
