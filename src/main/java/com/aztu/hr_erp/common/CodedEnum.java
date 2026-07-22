package com.aztu.hr_erp.common;

/** Enums whose persisted form is a stable lowercase string code (matches the DBML varchar values). */
public interface CodedEnum {
    String code();
}
