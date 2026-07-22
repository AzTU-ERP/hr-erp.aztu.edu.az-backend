package com.aztu.hr_erp.common.enums;
import com.aztu.hr_erp.common.CodedEnum;
public enum ApplicantDocType implements CodedEnum {
    CV("cv"), OTHER("other");
    private final String code; ApplicantDocType(String c){this.code=c;}
    public String code(){return code;}
}
