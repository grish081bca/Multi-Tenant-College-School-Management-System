package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.dto.request.EmployeeRequest;
import com.college.erp.collegemanagementsystem.enums.EmployeeType;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@PreAuthorize("hasAuthority('PERMISSION_EMPLOYEE_READ')")
public class EmployeeController {
    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<RestResponseDTO> list(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) EmployeeType type, @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Employee profiles found.", service.search(q, type, page, size, tenantId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponseDTO> get(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Employee profile found.", service.get(id, tenantId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_EMPLOYEE_WRITE')")
    public ResponseEntity<RestResponseDTO> create(@Valid @RequestBody EmployeeRequest request, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponseDTO.success("Employee profile created.", service.create(request, tenantId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_EMPLOYEE_WRITE')")
    public ResponseEntity<RestResponseDTO> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request,
                                                  @RequestParam(required = false) Long tenantId, @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(RestResponseDTO.success("Employee profile updated.", service.update(id, request, tenantId, remarks)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PERMISSION_EMPLOYEE_WRITE')")
    public ResponseEntity<RestResponseDTO> status(@PathVariable Long id, @RequestParam Status status,
                                                  @RequestParam(required = false) Long tenantId, @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(RestResponseDTO.success("Employee status updated.", service.changeStatus(id, status, tenantId, remarks)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_EMPLOYEE_WRITE')")
    public ResponseEntity<RestResponseDTO> delete(@PathVariable Long id, @RequestParam(required = false) Long tenantId,
                                                  @RequestParam(required = false) String remarks) {
        service.delete(id, tenantId, remarks);
        return ResponseEntity.ok(RestResponseDTO.success("Employee profile deleted."));
    }
}
