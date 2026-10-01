package com.college.erp.collegemanagementsystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "semesters", uniqueConstraints = {
        @UniqueConstraint(name = "uk_semester_tenant_code", columnNames = {"tenant_id", "code"}),
        @UniqueConstraint(name = "uk_semester_tenant_name", columnNames = {"tenant_id", "name"})
}, indexes = @Index(name = "idx_semester_tenant_name", columnList = "tenant_id, name"))
public class Semester extends TenantOwnedEntity {
    @Column(name = "sequence_number")
    private Integer sequenceNumber;
    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }
}
