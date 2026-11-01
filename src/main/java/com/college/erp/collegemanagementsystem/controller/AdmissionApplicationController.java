package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.dto.request.AdmissionApplicationRequest;
import com.college.erp.collegemanagementsystem.dto.request.AdmissionTransitionRequest;
import com.college.erp.collegemanagementsystem.enums.AdmissionStatus;
import com.college.erp.collegemanagementsystem.service.AdmissionApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admissions")
@PreAuthorize("hasAuthority('PERMISSION_ADMISSION_READ')")
public class AdmissionApplicationController {
    private final AdmissionApplicationService service;

    public AdmissionApplicationController(AdmissionApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<RestResponseDTO> list(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) AdmissionStatus status, @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Admission applications found.", service.search(q, status, page, size, tenantId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponseDTO> get(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Admission application found.", service.get(id, tenantId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_ADMISSION_WRITE')")
    public ResponseEntity<RestResponseDTO> create(@Valid @RequestBody AdmissionApplicationRequest request, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponseDTO.success("Admission application created.", service.create(request, tenantId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_ADMISSION_WRITE')")
    public ResponseEntity<RestResponseDTO> update(@PathVariable Long id, @Valid @RequestBody AdmissionApplicationRequest request,
                                                  @RequestParam(required = false) Long tenantId, @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(RestResponseDTO.success("Admission application updated.", service.update(id, request, tenantId, remarks)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PERMISSION_ADMISSION_WRITE')")
    public ResponseEntity<RestResponseDTO> transition(@PathVariable Long id, @Valid @RequestBody AdmissionTransitionRequest request,
                                                      @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Admission status updated.", service.transition(id, request.getStatus(), tenantId, request.getRemarks())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_ADMISSION_WRITE')")
    public ResponseEntity<RestResponseDTO> delete(@PathVariable Long id, @RequestParam(required = false) Long tenantId,
                                                  @RequestParam(required = false) String remarks) {
        service.delete(id, tenantId, remarks);
        return ResponseEntity.ok(RestResponseDTO.success("Admission application deleted."));
    }
}
