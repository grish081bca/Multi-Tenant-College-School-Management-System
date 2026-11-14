package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.dto.request.FeeStructureRequest;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.service.FeeManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fee-structures")
@PreAuthorize("hasAuthority('PERMISSION_FEE_STRUCTURE_READ')")
public class FeeStructureController {
    private final FeeManagementService fees;

    public FeeStructureController(FeeManagementService fees) { this.fees = fees; }

    @GetMapping
    public ResponseEntity<RestResponseDTO> list(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) Status status,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Fee structures found.", fees.searchStructures(q, status, page, size, tenantId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponseDTO> get(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Fee structure found.", fees.getStructure(id, tenantId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_FEE_STRUCTURE_WRITE')")
    public ResponseEntity<RestResponseDTO> create(@Valid @RequestBody FeeStructureRequest request,
                                                  @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponseDTO.success("Fee structure created.", fees.createStructure(request, tenantId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_FEE_STRUCTURE_WRITE')")
    public ResponseEntity<RestResponseDTO> update(@PathVariable Long id, @Valid @RequestBody FeeStructureRequest request,
                                                  @RequestParam(required = false) Long tenantId,
                                                  @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(RestResponseDTO.success("Fee structure updated.", fees.updateStructure(id, request, tenantId, remarks)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PERMISSION_FEE_STRUCTURE_WRITE')")
    public ResponseEntity<RestResponseDTO> status(@PathVariable Long id, @RequestParam Status status,
                                                 @RequestParam(required = false) Long tenantId,
                                                 @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(RestResponseDTO.success("Fee structure status updated.", fees.changeStructureStatus(id, status, tenantId, remarks)));
    }
}
