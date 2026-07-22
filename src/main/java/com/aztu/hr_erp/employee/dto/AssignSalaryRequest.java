package com.aztu.hr_erp.employee.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AssignSalaryRequest(@NotNull BigDecimal salary) {}
