package com.college.erp.collegemanagementsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity @Getter @Setter
@Table(name = "examinations", uniqueConstraints = @UniqueConstraint(name = "uk_examination_tenant_code", columnNames = {"tenant_id", "code"}), indexes = {
        @Index(name = "idx_examination_tenant_date", columnList = "tenant_id, exam_date"),
        @Index(name = "idx_examination_tenant_section", columnList = "tenant_id, section_id")
})
public class Examination extends TenantOwnedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "section_id", nullable = false)
    private Section section;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "program_subject_id", nullable = false)
    private ProgramSubject programSubject;
    @Column(name = "exam_date", nullable = false) private LocalDate examDate;
    @Column(name = "start_time", nullable = false) private LocalTime startTime;
    @Column(name = "end_time", nullable = false) private LocalTime endTime;
    @Column(name = "maximum_marks", nullable = false, precision = 7, scale = 2) private BigDecimal maximumMarks;
}
