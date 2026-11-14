package com.college.erp.collegemanagementsystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class FeeBalanceDTO {
    private Long feeStructureId;
    private String feeCode;
    private String feeName;
    private BigDecimal amount;
    private BigDecimal paid;
    private BigDecimal balance;
    private LocalDate dueDate;
}
