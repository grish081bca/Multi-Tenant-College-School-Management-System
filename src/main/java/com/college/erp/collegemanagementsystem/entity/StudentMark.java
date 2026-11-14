package com.college.erp.collegemanagementsystem.entity;

import com.college.erp.collegemanagementsystem.enums.MarkVerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "student_marks", uniqueConstraints = @UniqueConstraint(name = "uk_marks_tenant_exam_enrollment", columnNames = {"tenant_id", "examination_id", "enrollment_id"}), indexes = {
        @Index(name = "idx_marks_tenant_status", columnList = "tenant_id, verification_status"),
        @Index(name = "idx_marks_tenant_enrollment", columnList = "tenant_id, enrollment_id")
})
public class StudentMark extends TenantOwnedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "examination_id", nullable = false)
    private Examination examination;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private StudentEnrollment enrollment;

    @Column(name = "marks_obtained", nullable = false, precision = 7, scale = 2)
    private BigDecimal marksObtained;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private MarkVerificationStatus verificationStatus = MarkVerificationStatus.PENDING;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "verification_remarks", length = 1000)
    private String verificationRemarks;
}
