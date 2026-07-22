package com.aztu.hr_erp.common.enums;
import com.aztu.hr_erp.common.CodedEnum;
public enum IntegrationStatus implements CodedEnum {
    PENDING("pending"), SENT("sent"), FAILED("failed");
    private final String code; IntegrationStatus(String c){this.code=c;}
    public String code(){return code;}
}
