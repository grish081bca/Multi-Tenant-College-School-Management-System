package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.dto.request.MarksReviewRequest;
import com.college.erp.collegemanagementsystem.enums.MarkVerificationStatus;
import com.college.erp.collegemanagementsystem.service.StudentMarksService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/marks-verification")
public class MarksVerificationController {
    private final StudentMarksService marks;

    public MarksVerificationController(StudentMarksService marks) { this.marks = marks; }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_READ')")
    public ResponseEntity<RestResponseDTO> pending(@RequestParam(required = false) String q,
                                                  @RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer size,
                                                  @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Pending marks found successfully.",
                marks.search(q, MarkVerificationStatus.PENDING, page, size, tenantId)));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_VERIFY')")
    public ResponseEntity<RestResponseDTO> verify(@PathVariable Long id,
                                                  @RequestParam(required = false) Long tenantId,
                                                  @Valid @RequestBody(required = false) MarksReviewRequest request) {
        return ResponseEntity.ok(RestResponseDTO.success("Marks verified successfully.", marks.verify(id, tenantId, request == null ? null : request.getRemarks())));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_VERIFY')")
    public ResponseEntity<RestResponseDTO> reject(@PathVariable Long id,
                                                 @RequestParam(required = false) Long tenantId,
                                                 @Valid @RequestBody MarksReviewRequest request) {
        return ResponseEntity.ok(RestResponseDTO.success("Marks rejected successfully.", marks.reject(id, tenantId, request == null ? null : request.getRemarks())));
    }
}
