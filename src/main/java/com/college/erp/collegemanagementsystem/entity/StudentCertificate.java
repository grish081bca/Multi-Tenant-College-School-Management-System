package com.college.erp.collegemanagementsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "student_certificates", uniqueConstraints = {
        @UniqueConstraint(name = "uk_certificate_tenant_code", columnNames = {"tenant_id", "code"}),
        @UniqueConstraint(name = "uk_certificate_tenant_enrollment_type", columnNames = {"tenant_id", "enrollment_id", "certificate_type"})
})
public class StudentCertificate extends TenantOwnedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private StudentEnrollment enrollment;

    @Column(name = "certificate_type", nullable = false, length = 60)
    private String certificateType;

    @Column(name = "issued_on", nullable = false)
    private LocalDate issuedOn;

    @Column(name = "revocation_reason", length = 500)
    private String revocationReason;

}
