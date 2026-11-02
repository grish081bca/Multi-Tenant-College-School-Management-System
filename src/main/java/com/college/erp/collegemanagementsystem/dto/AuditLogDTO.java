package com.college.erp.collegemanagementsystem.dto;

import com.college.erp.collegemanagementsystem.entity.EntityChangeLog;
import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.util.DateFormatUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class AuditLogDTO {
    private Long id;
    private String entityName;
    private Long entityId;
    private EntityChangeAction action;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String remarks;
    private String createdBy;
    private OffsetDateTime createdAt;

    @JsonIgnore
    public String getCreatedAtDisplay() {
        return DateFormatUtils.formatDateTime(createdAt);
    }

    public static AuditLogDTO from(EntityChangeLog entity) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(entity.getId());
        dto.setEntityName(entity.getEntityName());
        dto.setEntityId(entity.getEntityId());
        dto.setAction(entity.getAction());
        dto.setFieldName(entity.getFieldName());
        dto.setOldValue(entity.getOldValue());
        dto.setNewValue(entity.getNewValue());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
