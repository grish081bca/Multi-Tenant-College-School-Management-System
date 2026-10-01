package com.college.erp.collegemanagementsystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "departments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_department_tenant_code", columnNames = {"tenant_id", "code"}),
        @UniqueConstraint(name = "uk_department_tenant_name", columnNames = {"tenant_id", "name"})
}, indexes = @Index(name = "idx_department_tenant_name", columnList = "tenant_id, name"))
public class Department extends TenantOwnedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;
    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }
}
