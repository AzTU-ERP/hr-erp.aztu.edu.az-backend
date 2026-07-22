package com.aztu.hr_erp.common.enums;
import com.aztu.hr_erp.common.CodedEnum;
public enum ReviewDecision implements CodedEnum {
    SCREENING("screening"), APPROVED("approved"), REJECTED("rejected");
    private final String code; ReviewDecision(String c){this.code=c;}
    public String code(){return code;}
}
