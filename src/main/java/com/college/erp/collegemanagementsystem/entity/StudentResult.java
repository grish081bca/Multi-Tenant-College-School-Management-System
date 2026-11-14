package com.college.erp.collegemanagementsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "student_results", uniqueConstraints = @UniqueConstraint(name = "uk_result_tenant_enrollment", columnNames = {"tenant_id", "enrollment_id"}), indexes = @Index(name = "idx_result_tenant_gpa", columnList = "tenant_id, gpa"))
public class StudentResult extends TenantOwnedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private StudentEnrollment enrollment;

    @Column(name = "total_marks_obtained", nullable = false, precision = 9, scale = 2)
    private BigDecimal totalMarksObtained;

    @Column(name = "total_maximum_marks", nullable = false, precision = 9, scale = 2)
    private BigDecimal totalMaximumMarks;

    @Column(name = "percentage", nullable = false, precision = 6, scale = 2)
    private BigDecimal percentage;

    @Column(name = "gpa", nullable = false, precision = 5, scale = 2)
    private BigDecimal gpa;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "processed_by", nullable = false, length = 100)
    private String processedBy;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentResultCourse> courseResults = new ArrayList<>();

    public void addCourseResult(StudentResultCourse courseResult) {
        courseResults.add(courseResult);
        courseResult.setResult(this);
    }
}
