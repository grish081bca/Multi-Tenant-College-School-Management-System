package com.college.erp.collegemanagementsystem.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "subjects", uniqueConstraints = {
        @UniqueConstraint(name = "uk_subject_tenant_code", columnNames = {"tenant_id", "code"}),
        @UniqueConstraint(name = "uk_subject_tenant_name", columnNames = {"tenant_id", "name"})
}, indexes = @Index(name = "idx_subject_tenant_name", columnList = "tenant_id, name"))
public class Subject extends TenantOwnedEntity {
    @Column(name = "credit_hours", precision = 5, scale = 2)
    private BigDecimal creditHours;
    public BigDecimal getCreditHours() { return creditHours; }
    public void setCreditHours(BigDecimal creditHours) { this.creditHours = creditHours; }
}
