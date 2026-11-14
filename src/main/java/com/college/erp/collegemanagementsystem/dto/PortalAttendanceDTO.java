package com.college.erp.collegemanagementsystem.dto;

import com.college.erp.collegemanagementsystem.enums.AttendanceStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PortalAttendanceDTO {
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private String sectionName;
}
