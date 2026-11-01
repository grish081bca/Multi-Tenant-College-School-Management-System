package com.college.erp.collegemanagementsystem.entity;

import com.college.erp.collegemanagementsystem.enums.AdmissionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "admission_applications", uniqueConstraints = @UniqueConstraint(name = "uk_admission_tenant_application_number", columnNames = {"tenant_id", "application_number"}), indexes = @Index(name = "idx_admission_tenant_status", columnList = "tenant_id, status"))
public class AdmissionApplication extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;
    @Column(name = "application_number", nullable = false, length = 50)
    private String applicationNumber;
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "middle_name", length = 100)
    private String middleName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(length = 200)
    private String email;
    @Column(length = 30)
    private String phone;
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Column(length = 500)
    private String address;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;
    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdmissionStatus status = AdmissionStatus.DRAFT;
    @Column(length = 1000)
    private String remarks;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admitted_student_id")
    private Student admittedStudent;
    @Column(nullable = false)
    private boolean deleted = false;
    @Version
    private Long version;
}
