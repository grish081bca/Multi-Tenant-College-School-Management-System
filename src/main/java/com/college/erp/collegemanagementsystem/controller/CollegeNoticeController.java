package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.dto.request.CollegeNoticeRequest;
import com.college.erp.collegemanagementsystem.enums.NoticeStatus;
import com.college.erp.collegemanagementsystem.service.CollegeNoticeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notices")
@PreAuthorize("hasAuthority('PERMISSION_NOTICE_READ')")
public class CollegeNoticeController {
    private final CollegeNoticeService notices;

    public CollegeNoticeController(CollegeNoticeService notices) { this.notices = notices; }

    @GetMapping
    public ResponseEntity<RestResponseDTO> list(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) NoticeStatus status,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Notices found.", notices.search(q, status, page, size, tenantId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponseDTO> get(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Notice found.", notices.get(id, tenantId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_NOTICE_WRITE')")
    public ResponseEntity<RestResponseDTO> create(@Valid @RequestBody CollegeNoticeRequest request,
                                                  @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponseDTO.success("Notice draft created.", notices.create(request, tenantId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_NOTICE_WRITE')")
    public ResponseEntity<RestResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CollegeNoticeRequest request,
                                                  @RequestParam(required = false) Long tenantId,
                                                  @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(RestResponseDTO.success("Notice draft updated.", notices.update(id, request, tenantId, remarks)));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('PERMISSION_NOTICE_WRITE')")
    public ResponseEntity<RestResponseDTO> publish(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Notice published.", notices.publish(id, tenantId)));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('PERMISSION_NOTICE_WRITE')")
    public ResponseEntity<RestResponseDTO> archive(@PathVariable Long id, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(RestResponseDTO.success("Notice archived.", notices.archive(id, tenantId)));
    }
}
