package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.service.FeeManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/receipts")
@PreAuthorize("hasAuthority('PERMISSION_FEE_COLLECTION_READ')")
public class ReceiptController {
    private final FeeManagementService fees;

    public ReceiptController(FeeManagementService fees) { this.fees = fees; }

    @GetMapping("/{receiptNumber}")
    public ResponseEntity<RestResponseDTO> get(@PathVariable String receiptNumber,
                                               @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Payment receipt found.", fees.getReceipt(receiptNumber, tenantId)));
    }
}
