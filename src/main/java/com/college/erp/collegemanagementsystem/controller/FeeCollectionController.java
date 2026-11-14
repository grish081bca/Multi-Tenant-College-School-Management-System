package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.dto.request.FeeCollectionRequest;
import com.college.erp.collegemanagementsystem.service.FeeManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fee-collections")
@PreAuthorize("hasAuthority('PERMISSION_FEE_COLLECTION_READ')")
public class FeeCollectionController {
    private final FeeManagementService fees;

    public FeeCollectionController(FeeManagementService fees) { this.fees = fees; }

    @GetMapping
    public ResponseEntity<RestResponseDTO> list(@RequestParam(required = false) String q,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Fee collections found.", fees.searchCollections(q, page, size, tenantId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponseDTO> get(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Fee collection found.", fees.getCollection(id, tenantId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_FEE_COLLECTION_WRITE')")
    public ResponseEntity<RestResponseDTO> collect(@Valid @RequestBody FeeCollectionRequest request,
                                                   @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponseDTO.success("Payment collected and receipt generated.", fees.collect(request, tenantId)));
    }
}
