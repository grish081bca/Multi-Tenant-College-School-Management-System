package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.RestResponseDTO;
import com.college.erp.collegemanagementsystem.service.CollegeNoticeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("hasAnyAuthority('PERMISSION_STUDENT_PORTAL', 'PERMISSION_TEACHER_PORTAL')")
public class NotificationController {
    private final CollegeNoticeService notices;

    public NotificationController(CollegeNoticeService notices) { this.notices = notices; }

    @GetMapping
    public ResponseEntity<RestResponseDTO> list() {
        return ResponseEntity.ok(RestResponseDTO.success("Notifications found.", notices.notifications()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<RestResponseDTO> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(RestResponseDTO.success("Notification marked as read.", notices.markRead(id)));
    }
}
