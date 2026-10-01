package com.college.erp.collegemanagementsystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_years", uniqueConstraints = {
        @UniqueConstraint(name = "uk_academic_year_tenant_code", columnNames = {"tenant_id", "code"}),
        @UniqueConstraint(name = "uk_academic_year_tenant_name", columnNames = {"tenant_id", "name"})
}, indexes = @Index(name = "idx_academic_year_tenant_name", columnList = "tenant_id, name"))
public class AcademicYear extends TenantOwnedEntity { }
