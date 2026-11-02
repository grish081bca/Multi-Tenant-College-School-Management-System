package com.college.erp.collegemanagementsystem.entity;

import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.util.DateFormatUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * @author grish
 *
 */
@Entity
@Getter
@Setter
@Table(
        name = "entity_change_logs",
        indexes = {
                @Index(name = "idx_entity_change_logs_entity", columnList = "entity_name, entity_id"),
                @Index(name = "idx_entity_change_logs_created_at", columnList = "created_at")
        }
)
public class EntityChangeLog extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private EntityChangeAction action;
    @Column(name = "field_name", length = 100)
    private String fieldName;
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Transient
    @JsonIgnore
    public String getCreatedAtDisplay() {
        return DateFormatUtils.formatDateTime(getCreatedAt());
    }
}
