package com.college.erp.collegemanagementsystem.dto;

import com.college.erp.collegemanagementsystem.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StudentCertificateDTO {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private Long enrollmentId;
    private String registrationNumber;
    private String studentName;
    private String certificateNumber;
    private String certificateType;
    private String title;
    private LocalDate issuedOn;
    private Status status;
    private String revocationReason;
}
