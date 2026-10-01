package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AcademicController {
    private static final String MODULES = "departments|faculties|programs|academic-years|semesters|sections|subjects|program-subjects|students|student-enrollments";
    private final AcademicManagementService service;
    public AcademicController(AcademicManagementService service) { this.service = service; }

    @PostMapping("/{module:" + MODULES + "}")
    public ResponseEntity<RestResponseDTO> create(@PathVariable String module, @RequestBody AcademicRecordDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponseDTO.success("Record created successfully.", service.save(module, null, request, null)));
    }
    @PutMapping("/{module:" + MODULES + "}/{id}")
    public ResponseEntity<RestResponseDTO> update(@PathVariable String module, @PathVariable Long id, @RequestBody AcademicRecordDTO request,
                                                   @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(RestResponseDTO.success("Record updated successfully.", service.save(module, id, request, remarks)));
    }
    @GetMapping("/{module:" + MODULES + "}")
    public ResponseEntity<RestResponseDTO> list(@PathVariable String module, @RequestParam(required = false) String q,
                                                 @RequestParam(required = false) Status status,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(RestResponseDTO.success("Records found successfully.", service.search(module, q, status, page, size)));
    }
    @GetMapping("/{module:" + MODULES + "}/{id}")
    public ResponseEntity<RestResponseDTO> get(@PathVariable String module, @PathVariable Long id) {
        return ResponseEntity.ok(RestResponseDTO.success("Record found successfully.", service.get(module, id)));
    }
    @PatchMapping("/{module:" + MODULES + "}/{id}/status")
    public ResponseEntity<RestResponseDTO> status(@PathVariable String module, @PathVariable Long id, @RequestParam Status status,
                                                   @RequestParam(required = false) String remarks) {
        service.changeStatus(module, id, status, remarks);
        return ResponseEntity.ok(RestResponseDTO.success("Record status updated successfully."));
    }
    @DeleteMapping("/{module:" + MODULES + "}/{id}")
    public ResponseEntity<RestResponseDTO> delete(@PathVariable String module, @PathVariable Long id) {
        service.delete(module, id);
        return ResponseEntity.ok(RestResponseDTO.success("Record deleted successfully."));
    }
}
