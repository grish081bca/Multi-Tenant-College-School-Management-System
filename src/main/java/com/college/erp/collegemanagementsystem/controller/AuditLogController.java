package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {
    private final EntityChangeLogService auditLogs;

    public AuditLogController(EntityChangeLogService auditLogs) {
        this.auditLogs = auditLogs;
    }

    @GetMapping
    public ResponseEntity<RestResponseDTO> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) EntityChangeAction action,
            @RequestParam(required = false) String changedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            return ResponseEntity.badRequest().body(RestResponseDTO.badRequest("From date must be on or before to date."));
        }
        if (entityId != null && entityId < 1) {
            return ResponseEntity.badRequest().body(RestResponseDTO.badRequest("Entity ID must be a positive number."));
        }
        return ResponseEntity.ok(RestResponseDTO.success("Audit logs found successfully.",
                auditLogs.search(q, entityName, entityId, action, changedBy, fromDate, toDate, page, size)));
    }
}
