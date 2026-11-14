package com.college.erp.collegemanagementsystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StudentResultCourseDTO {
    private Long programSubjectId;
    private String subjectCode;
    private String subjectName;
    private BigDecimal marksObtained;
    private BigDecimal maximumMarks;
    private BigDecimal percentage;
    private BigDecimal creditHours;
    private BigDecimal gradePoint;
}
