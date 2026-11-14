package com.college.erp.collegemanagementsystem.dto.request;

import com.college.erp.collegemanagementsystem.enums.Status;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class FeeStructureRequest {
    @NotNull private Long programId;
    @NotNull private Long academicYearId;
    @NotNull private Long semesterId;
    @NotBlank @Size(max = 50) private String code;
    @NotBlank @Size(max = 160) private String name;
    @Size(max = 1000) private String description;
    @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) private BigDecimal amount;
    private LocalDate dueDate;
    @NotNull private Status status = Status.ACTIVE;
}
