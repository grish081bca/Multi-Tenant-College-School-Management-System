package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.service.AcademicModuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** Shared response semantics for separately mapped academic REST controllers. */
public abstract class AbstractAcademicRestController {
    private final AcademicModuleService service;
    protected AbstractAcademicRestController(AcademicModuleService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<RestResponseDTO> create(@RequestBody AcademicRecordDTO request, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponseDTO.success("Record created successfully.", service.save(null, request, null, tenantId)));
    }
    @PutMapping("/{id}")
    public ResponseEntity<RestResponseDTO> update(@PathVariable Long id, @RequestBody AcademicRecordDTO request, @RequestParam(required = false) String remarks, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Record updated successfully.", service.save(id, request, remarks, tenantId)));
    }
    @GetMapping
    public ResponseEntity<RestResponseDTO> list(@RequestParam Map<String, String> parameters) {
        Map<String, String> filters = new LinkedHashMap<>(parameters);
        String q = filters.remove("q"), statusText = filters.remove("status");
        Integer page = parse(filters.remove("page"), 1), size = parse(filters.remove("size"), 10);
        Long tenantId = parseLong(filters.remove("tenantId"));
        Status status = null;
        try { if (statusText != null && !statusText.isBlank()) status = Status.valueOf(statusText); } catch (IllegalArgumentException ignored) { }
        return ResponseEntity.ok(RestResponseDTO.success("Records found successfully.", service.search(q, filters, status, page, size, tenantId)));
    }
    @GetMapping("/{id}")
    public ResponseEntity<RestResponseDTO> get(@PathVariable Long id, @RequestParam(required = false) Long tenantId) { return ResponseEntity.ok(RestResponseDTO.success("Record found successfully.", service.get(id, tenantId))); }
    @PatchMapping("/{id}/status")
    public ResponseEntity<RestResponseDTO> status(@PathVariable Long id, @RequestParam Status status, @RequestParam(required = false) String remarks, @RequestParam(required = false) Long tenantId) {
        service.changeStatus(id, status, remarks, tenantId); return ResponseEntity.ok(RestResponseDTO.success("Record status updated successfully."));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponseDTO> delete(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        service.delete(id, tenantId); return ResponseEntity.ok(RestResponseDTO.success("Record deleted successfully."));
    }
    private Integer parse(String value, int fallback) { try { return value == null ? fallback : Integer.valueOf(value); } catch (NumberFormatException ex) { return fallback; } }
    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Long.valueOf(value); } catch (NumberFormatException ex) { throw new ValidationException("Tenant filter must be a valid ID."); }
    }
}
