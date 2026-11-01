package com.college.erp.collegemanagementsystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdmissionApplicationRequest {
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
    @Past
    private LocalDate dateOfBirth;
    @Size(max = 500)
    private String address;
    @NotNull
    @Positive
    private Long programId;
    @NotNull
    @Positive
    private Long academicYearId;
    @NotNull
    @Positive
    private Long semesterId;
    @NotNull
    @Positive
    private Long sectionId;
    @NotNull
    @PastOrPresent
    private LocalDate applicationDate;
    @Size(max = 1000)
    private String remarks;
}
