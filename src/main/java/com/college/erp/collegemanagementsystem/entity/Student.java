package com.college.erp.collegemanagementsystem.entity;

import com.college.erp.collegemanagementsystem.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Getter @Setter
@Table(name = "students", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_tenant_registration", columnNames = {"tenant_id", "registration_number"}),
        @UniqueConstraint(name = "uk_student_tenant_email", columnNames = {"tenant_id", "email"})
}, indexes = @Index(name = "idx_student_tenant_name", columnList = "tenant_id, last_name, first_name"))
public class Student extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "tenant_id", nullable = false, updatable = false) private Tenant tenant;
    @Column(name = "registration_number", nullable = false, length = 50) private String registrationNumber;
    @Column(name = "first_name", nullable = false, length = 100) private String firstName;
    @Column(name = "middle_name", length = 100) private String middleName;
    @Column(name = "last_name", nullable = false, length = 100) private String lastName;
    @Column(name = "email", length = 200) private String email;
    @Column(name = "phone", length = 30) private String phone;
    @Column(name = "date_of_birth") private LocalDate dateOfBirth;
    @Column(name = "address", length = 500) private String address;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 20) private Status status = Status.ACTIVE;
}
