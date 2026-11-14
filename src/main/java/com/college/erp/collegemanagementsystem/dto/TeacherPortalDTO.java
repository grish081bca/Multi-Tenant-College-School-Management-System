package com.college.erp.collegemanagementsystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TeacherPortalDTO {
    private String teacherName;
    private String employeeNumber;
    private String designation;
    private String departmentName;
    private String email;
    private List<PortalTimetableDTO> timetable;
    private List<UserNotificationDTO> notifications;
}
