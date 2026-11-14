package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.dto.request.MarksEntryRequest;
import com.college.erp.collegemanagementsystem.service.StudentMarksService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/marks")
public class MarksEntryController {
    private final StudentMarksService marks;

    public MarksEntryController(StudentMarksService marks) { this.marks = marks; }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_READ')")
    public ResponseEntity<RestResponseDTO> list(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) com.college.erp.collegemanagementsystem.enums.MarkVerificationStatus verificationStatus,
                                                @RequestParam(required = false) Integer page,
                                                @RequestParam(required = false) Integer size,
                                                @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Marks entries found successfully.",
                marks.search(q, verificationStatus, page, size, tenantId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_READ')")
    public ResponseEntity<RestResponseDTO> get(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Marks entry found successfully.", marks.get(id, tenantId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_WRITE')")
    public ResponseEntity<RestResponseDTO> create(@Valid @RequestBody MarksEntryRequest request,
                                                  @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponseDTO.success("Marks entered and submitted for verification.", marks.create(request, tenantId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_WRITE')")
    public ResponseEntity<RestResponseDTO> update(@PathVariable Long id, @Valid @RequestBody MarksEntryRequest request,
                                                  @RequestParam(required = false) Long tenantId,
                                                  @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(RestResponseDTO.success("Marks entry updated successfully.", marks.update(id, request, tenantId, remarks)));
    }
}
