package com.college.erp.collegemanagementsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "student_result_courses", uniqueConstraints = @UniqueConstraint(name = "uk_result_course", columnNames = {"result_id", "program_subject_id"}))
public class StudentResultCourse extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "result_id", nullable = false)
    private StudentResult result;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_subject_id", nullable = false)
    private ProgramSubject programSubject;

    @Column(name = "marks_obtained", nullable = false, precision = 9, scale = 2)
    private BigDecimal marksObtained;

    @Column(name = "maximum_marks", nullable = false, precision = 9, scale = 2)
    private BigDecimal maximumMarks;

    @Column(name = "percentage", nullable = false, precision = 6, scale = 2)
    private BigDecimal percentage;

    @Column(name = "credit_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal creditHours;

    @Column(name = "grade_point", nullable = false, precision = 5, scale = 4)
    private BigDecimal gradePoint;
}
