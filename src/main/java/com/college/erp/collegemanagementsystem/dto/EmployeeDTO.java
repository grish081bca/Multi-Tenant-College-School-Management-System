package com.college.erp.collegemanagementsystem.dto;

import com.college.erp.collegemanagementsystem.enums.EmployeeType;
import com.college.erp.collegemanagementsystem.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeDTO {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private String employeeNumber;
    private EmployeeType employeeType;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phone;
    private String designation;
    private Long departmentId;
    private String departmentName;
    private LocalDate hireDate;
    private Status status;
}
