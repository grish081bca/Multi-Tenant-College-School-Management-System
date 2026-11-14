package com.college.erp.collegemanagementsystem.dto.request;

import com.college.erp.collegemanagementsystem.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FeeCollectionRequest {
    @NotNull private Long enrollmentId;
    @NotNull private Long feeStructureId;
    @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) private BigDecimal amount;
    @NotNull private PaymentMethod paymentMethod;
    @Size(max = 120) private String referenceNumber;
}
