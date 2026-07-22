package com.aztu.hr_erp.common.enums;
import com.aztu.hr_erp.common.CodedEnum;
public enum EmployeeDocType implements CodedEnum {
    CONTRACT("contract"), APPROVAL_DOC("approval_doc"), ID_DOC("id_doc"), OTHER("other");
    private final String code; EmployeeDocType(String c){this.code=c;}
    public String code(){return code;}
}
