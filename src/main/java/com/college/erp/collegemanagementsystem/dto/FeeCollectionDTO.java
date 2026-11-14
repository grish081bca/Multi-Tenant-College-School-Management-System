package com.college.erp.collegemanagementsystem.dto;

import com.college.erp.collegemanagementsystem.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class FeeCollectionDTO {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private Long enrollmentId;
    private Long feeStructureId;
    private String feeCode;
    private String feeName;
    private String registrationNumber;
    private String studentName;
    private String receiptNumber;
    private BigDecimal amount;
    private BigDecimal feeAmount;
    private BigDecimal totalPaid;
    private BigDecimal balance;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private OffsetDateTime collectedAt;
    private String collectedAtDisplay;
    private String collectedBy;
}
