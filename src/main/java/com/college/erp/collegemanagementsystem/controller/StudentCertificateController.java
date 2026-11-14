package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.dto.request.CertificateRevocationRequest;
import com.college.erp.collegemanagementsystem.dto.request.StudentCertificateRequest;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.service.StudentCertificateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/certificates")
@PreAuthorize("hasAuthority('PERMISSION_CERTIFICATE_READ')")
public class StudentCertificateController {
    private final StudentCertificateService certificates;

    public StudentCertificateController(StudentCertificateService certificates) { this.certificates = certificates; }

    @GetMapping
    public ResponseEntity<RestResponseDTO> list(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) Status status,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Student certificates found.", certificates.search(q, status, page, size, tenantId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponseDTO> get(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Student certificate found.", certificates.get(id, tenantId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CERTIFICATE_WRITE')")
    public ResponseEntity<RestResponseDTO> issue(@Valid @RequestBody StudentCertificateRequest request,
                                                 @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponseDTO.success("Student certificate issued.", certificates.issue(request, tenantId)));
    }

    @PatchMapping("/{id}/revoke")
    @PreAuthorize("hasAuthority('PERMISSION_CERTIFICATE_WRITE')")
    public ResponseEntity<RestResponseDTO> revoke(@PathVariable Long id,
                                                 @Valid @RequestBody CertificateRevocationRequest request,
                                                 @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Student certificate revoked.", certificates.revoke(id, request.getReason(), tenantId)));
    }
}
