package com.college.erp.collegemanagementsystem.entity;

import com.college.erp.collegemanagementsystem.enums.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "fee_collections", uniqueConstraints =
        @UniqueConstraint(name = "uk_fee_collection_receipt", columnNames = "receipt_number"),
        indexes = @Index(name = "idx_fee_collection_tenant_enrollment", columnList = "tenant_id, enrollment_id"))
public class FeeCollection extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, updatable = false)
    private StudentEnrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_structure_id", nullable = false, updatable = false)
    private FeeStructure feeStructure;

    @Column(name = "receipt_number", length = 80)
    private String receiptNumber;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30, updatable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "reference_number", length = 120, updatable = false)
    private String referenceNumber;

    @Column(name = "collected_at", nullable = false, updatable = false)
    private OffsetDateTime collectedAt;

    @Column(name = "collected_by", nullable = false, length = 150, updatable = false)
    private String collectedBy;
}
