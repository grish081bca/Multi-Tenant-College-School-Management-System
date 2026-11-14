package com.college.erp.collegemanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StudentCertificateRequest {
    @NotNull
    private Long enrollmentId;
    @NotBlank
    @Size(max = 60)
    private String certificateType;
    @NotBlank
    @Size(max = 160)
    private String title;
    @NotNull
    private LocalDate issuedOn;
    @Size(max = 1000)
    private String description;
}
