package com.aztu.hr_erp.common.enums;
import com.aztu.hr_erp.common.CodedEnum;
public enum ApplicationStatus implements CodedEnum {
    SUBMITTED("submitted"), SCREENING("screening"), APPROVED("approved"),
    REJECTED("rejected"), WITHDRAWN("withdrawn");
    private final String code; ApplicationStatus(String c){this.code=c;}
    public String code(){return code;}
}
