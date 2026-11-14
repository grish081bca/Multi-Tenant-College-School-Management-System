package com.college.erp.collegemanagementsystem.dto;

import com.college.erp.collegemanagementsystem.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class FeeStructureDTO {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private Long programId;
    private String programName;
    private Long academicYearId;
    private String academicYearName;
    private Long semesterId;
    private String semesterName;
    private String code;
    private String name;
    private String description;
    private BigDecimal amount;
    private LocalDate dueDate;
    private Status status;
}
