package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.service.PortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teacher-portal")
@PreAuthorize("hasAuthority('PERMISSION_TEACHER_PORTAL')")
public class TeacherPortalController {
    private final PortalService portals;

    public TeacherPortalController(PortalService portals) { this.portals = portals; }

    @GetMapping
    public ResponseEntity<RestResponseDTO> dashboard() {
        return ResponseEntity.ok(RestResponseDTO.success("Teacher portal data found.", portals.teacherPortal()));
    }
}
