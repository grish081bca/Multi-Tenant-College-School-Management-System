package com.college.erp.collegemanagementsystem.entity;

import com.college.erp.collegemanagementsystem.enums.EmployeeType;
import com.college.erp.collegemanagementsystem.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "employees", uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_tenant_number", columnNames = {"tenant_id", "employee_number"}),
        @UniqueConstraint(name = "uk_employee_tenant_email", columnNames = {"tenant_id", "email"})
}, indexes = @Index(name = "idx_employee_tenant_type_status", columnList = "tenant_id, employee_type, status"))
public class Employee extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;
    @Column(name = "employee_number", nullable = false, length = 50)
    private String employeeNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, length = 20)
    private EmployeeType employeeType;
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
    @Column(nullable = false, length = 160)
    private String designation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    @Column(name = "hire_date")
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;
    @Column(nullable = false)
    private boolean deleted = false;
}
