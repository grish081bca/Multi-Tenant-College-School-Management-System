package com.college.erp.collegemanagementsystem.entity;

import com.college.erp.collegemanagementsystem.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Common ownership and lifecycle fields for college academic data. */
@MappedSuperclass
@Getter
@Setter
public abstract class TenantOwnedEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.ACTIVE;
}
