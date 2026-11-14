package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.dto.request.ResultProcessRequest;
import com.college.erp.collegemanagementsystem.service.StudentResultsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/results")
public class ResultsController {
    private final StudentResultsService results;

    public ResultsController(StudentResultsService results) { this.results = results; }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_RESULTS_READ')")
    public ResponseEntity<RestResponseDTO> list(@RequestParam(required = false) String q,
                                               @RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer size,
                                               @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Student results found successfully.", results.search(q, page, size, tenantId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_RESULTS_READ')")
    public ResponseEntity<RestResponseDTO> get(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Student result found successfully.", results.get(id, tenantId)));
    }

    @PostMapping("/process")
    @PreAuthorize("hasAuthority('PERMISSION_RESULTS_PROCESS')")
    public ResponseEntity<RestResponseDTO> process(@Valid @RequestBody ResultProcessRequest request,
                                                   @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Student result processed successfully.",
                results.process(request.getEnrollmentId(), tenantId)));
    }
}
