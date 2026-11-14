package com.college.erp.collegemanagementsystem.dto;

import com.college.erp.collegemanagementsystem.enums.MarkVerificationStatus;
import com.college.erp.collegemanagementsystem.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class MarksEntryDTO {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private Long examinationId;
    private String examinationCode;
    private String examinationName;
    private LocalDate examDate;
    private BigDecimal maximumMarks;
    private Long enrollmentId;
    private Long studentId;
    private String registrationNumber;
    private String studentName;
    private String sectionName;
    private String subjectName;
    private BigDecimal marksObtained;
    private MarkVerificationStatus verificationStatus;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewedAtDisplay;
    private String verificationRemarks;
    private Status status;
}
