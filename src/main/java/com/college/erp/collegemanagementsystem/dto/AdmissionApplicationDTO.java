package com.college.erp.collegemanagementsystem.dto;

import com.college.erp.collegemanagementsystem.enums.AdmissionStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdmissionApplicationDTO {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private String applicationNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private Long programId;
    private String programName;
    private Long academicYearId;
    private String academicYearName;
    private Long semesterId;
    private String semesterName;
    private Long sectionId;
    private String sectionName;
    private LocalDate applicationDate;
    private AdmissionStatus status;
    private String remarks;
    private Long admittedStudentId;
    private String admittedRegistrationNumber;
}
