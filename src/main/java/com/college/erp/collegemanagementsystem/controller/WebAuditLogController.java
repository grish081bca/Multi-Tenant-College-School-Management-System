package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Map;

@Controller
public class WebAuditLogController {
    private final EntityChangeLogService auditLogs;

    public WebAuditLogController(EntityChangeLogService auditLogs) {
        this.auditLogs = auditLogs;
    }

    @GetMapping("/web/audit-logs")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String entityName,
                       @RequestParam(required = false) Long entityId,
                       @RequestParam(required = false) EntityChangeAction action,
                       @RequestParam(required = false) String changedBy,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer size,
                       Model model) {
        boolean invalidRange = fromDate != null && toDate != null && fromDate.isAfter(toDate);
        boolean invalidEntityId = entityId != null && entityId < 1;
        model.addAttribute("page", invalidRange || invalidEntityId ? null : auditLogs.search(
                q, entityName, entityId, action, changedBy, fromDate, toDate, page, size));
        model.addAttribute("q", q);
        model.addAttribute("entityName", entityName);
        model.addAttribute("entityId", entityId);
        model.addAttribute("selectedAction", action);
        model.addAttribute("changedBy", changedBy);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("actions", EntityChangeAction.values());
        model.addAttribute("error", invalidRange ? "From date must be on or before to date."
                : invalidEntityId ? "Entity ID must be a positive number." : null);
        Map<String, Object> filters = WebPagination.filters();
        filters.put("q", q);
        filters.put("entityName", entityName);
        filters.put("entityId", entityId);
        filters.put("action", action);
        filters.put("changedBy", changedBy);
        filters.put("fromDate", fromDate);
        filters.put("toDate", toDate);
        WebPagination.add(model, "/web/audit-logs", size, filters);
        return "audit-logs";
    }
}
