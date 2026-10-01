package com.college.erp.collegemanagementsystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "faculties", uniqueConstraints = {
        @UniqueConstraint(name = "uk_faculty_tenant_code", columnNames = {"tenant_id", "code"}),
        @UniqueConstraint(name = "uk_faculty_tenant_name", columnNames = {"tenant_id", "name"})
}, indexes = @Index(name = "idx_faculty_tenant_name", columnList = "tenant_id, name"))
public class Faculty extends TenantOwnedEntity { }
