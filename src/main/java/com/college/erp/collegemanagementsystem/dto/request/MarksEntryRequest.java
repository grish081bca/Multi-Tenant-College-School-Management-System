package com.college.erp.collegemanagementsystem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MarksEntryRequest {
    @NotNull
    private Long examinationId;

    @NotNull
    private Long enrollmentId;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 5, fraction = 2)
    private BigDecimal marksObtained;
}
