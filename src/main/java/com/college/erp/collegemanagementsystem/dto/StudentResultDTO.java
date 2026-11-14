package com.college.erp.collegemanagementsystem.dto;

import com.college.erp.collegemanagementsystem.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class StudentResultDTO {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private Long enrollmentId;
    private String registrationNumber;
    private String studentName;
    private String programName;
    private String academicYearName;
    private String semesterName;
    private BigDecimal totalMarksObtained;
    private BigDecimal totalMaximumMarks;
    private BigDecimal percentage;
    private BigDecimal gpa;
    private LocalDateTime processedAt;
    private String processedAtDisplay;
    private String processedBy;
    private Status status;
    private List<StudentResultCourseDTO> courses;
}
