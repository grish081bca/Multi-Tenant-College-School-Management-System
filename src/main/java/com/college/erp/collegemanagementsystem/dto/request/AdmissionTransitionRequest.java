package com.college.erp.collegemanagementsystem.dto.request;

import com.college.erp.collegemanagementsystem.enums.AdmissionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdmissionTransitionRequest {
    @NotNull
    private AdmissionStatus status;
    @Size(max = 1000)
    private String remarks;
}
