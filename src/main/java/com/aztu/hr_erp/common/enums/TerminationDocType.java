package com.aztu.hr_erp.common.enums;
import com.aztu.hr_erp.common.CodedEnum;
public enum TerminationDocType implements CodedEnum {
    RESIGNATION("resignation"), SETTLEMENT("settlement"), OTHER("other");
    private final String code; TerminationDocType(String c){this.code=c;}
    public String code(){return code;}
}
