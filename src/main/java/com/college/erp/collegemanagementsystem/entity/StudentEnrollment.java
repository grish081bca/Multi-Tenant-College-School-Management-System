package com.college.erp.collegemanagementsystem.entity;

import com.college.erp.collegemanagementsystem.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
@Table(name = "student_enrollments", uniqueConstraints = @UniqueConstraint(name = "uk_enrollment_tenant_student_program_year_semester", columnNames = {"tenant_id", "student_id", "program_id", "academic_year_id", "semester_id"}), indexes = @Index(name = "idx_enrollment_tenant_student", columnList = "tenant_id, student_id"))
public class StudentEnrollment extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "tenant_id", nullable = false, updatable = false) private Tenant tenant;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "student_id", nullable = false) private Student student;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "program_id", nullable = false) private Program program;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "academic_year_id", nullable = false) private AcademicYear academicYear;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "semester_id", nullable = false) private Semester semester;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "section_id", nullable = false) private Section section;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 20) private Status status = Status.ACTIVE;
}
