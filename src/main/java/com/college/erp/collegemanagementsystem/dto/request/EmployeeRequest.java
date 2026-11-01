package com.college.erp.collegemanagementsystem.dto.request;

import com.college.erp.collegemanagementsystem.enums.EmployeeType;
import com.college.erp.collegemanagementsystem.enums.Status;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeRequest {
    @NotBlank
    @Size(max = 50)
    private String employeeNumber;
    @NotNull
    private EmployeeType employeeType;
    @NotBlank
    @Size(max = 100)
    private String firstName;
    @Size(max = 100)
    private String middleName;
    @NotBlank
    @Size(max = 100)
    private String lastName;
    @Email
    @Size(max = 200)
    private String email;
    @Size(max = 30)
    private String phone;
    @NotBlank
    @Size(max = 160)
    private String designation;
    @Positive
    private Long departmentId;
    @PastOrPresent
    private LocalDate hireDate;
    @NotNull
    private Status status;
}
