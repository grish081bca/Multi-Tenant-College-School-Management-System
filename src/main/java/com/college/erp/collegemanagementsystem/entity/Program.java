package com.college.erp.collegemanagementsystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "programs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_program_tenant_code", columnNames = {"tenant_id", "code"}),
        @UniqueConstraint(name = "uk_program_tenant_name", columnNames = {"tenant_id", "name"})
}, indexes = @Index(name = "idx_program_tenant_name", columnList = "tenant_id, name"))
public class Program extends TenantOwnedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
}
