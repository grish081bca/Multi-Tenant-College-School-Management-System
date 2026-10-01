package com.college.erp.collegemanagementsystem.dto;

import com.college.erp.collegemanagementsystem.enums.Status;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class AcademicRecordDTO {
    private Long id;
    private String module;
    private String code;
    private String name;
    private String description;
    private Status status;
    private Integer sequenceNumber;
    private BigDecimal creditHours;
    private Long facultyId;
    private String facultyName;
    private Long departmentId;
    private String departmentName;
    private Long programId;
    private String programName;
    private Long academicYearId;
    private String academicYearName;
    private Long semesterId;
    private String semesterName;
    private Long subjectId;
    private String subjectName;
    private Long sectionId;
    private String sectionName;
    private Long studentId;
    private String studentName;
    private String registrationNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
}
