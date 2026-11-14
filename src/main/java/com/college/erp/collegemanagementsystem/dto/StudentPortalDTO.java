package com.college.erp.collegemanagementsystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StudentPortalDTO {
    private String studentName;
    private String registrationNumber;
    private String email;
    private String programName;
    private String academicYearName;
    private String semesterName;
    private String sectionName;
    private List<StudentResultDTO> results;
    private List<PortalAttendanceDTO> attendance;
    private List<StudentCertificateDTO> certificates;
    private List<FeeBalanceDTO> fees;
    private List<FeeCollectionDTO> receipts;
    private List<UserNotificationDTO> notifications;
}
