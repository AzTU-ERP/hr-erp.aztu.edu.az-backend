package com.aztu.hr_erp.common.enums;
import com.aztu.hr_erp.common.CodedEnum;
public enum JobType implements CodedEnum {
    FULL_TIME("full_time"), PART_TIME("part_time"), HOURLY("hourly");
    private final String code; JobType(String c){this.code=c;}
    public String code(){return code;}
}
