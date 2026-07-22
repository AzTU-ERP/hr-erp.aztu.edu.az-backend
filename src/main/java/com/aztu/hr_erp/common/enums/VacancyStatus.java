package com.aztu.hr_erp.common.enums;
import com.aztu.hr_erp.common.CodedEnum;
public enum VacancyStatus implements CodedEnum {
    OPEN("open"), CLOSED("closed"), FILLED("filled");
    private final String code; VacancyStatus(String c){this.code=c;}
    public String code(){return code;}
}
