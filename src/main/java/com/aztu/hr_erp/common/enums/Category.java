package com.aztu.hr_erp.common.enums;
import com.aztu.hr_erp.common.CodedEnum;
public enum Category implements CodedEnum {
    ALUMNI("alumni"), AZTU("aztu");
    private final String code; Category(String c){this.code=c;}
    public String code(){return code;}
}
