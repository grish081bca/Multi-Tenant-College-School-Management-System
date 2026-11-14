package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.PortalService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/teacher-portal")
@PreAuthorize("hasAuthority('PERMISSION_TEACHER_PORTAL')")
public class TeacherPortalWebController {
    private final PortalService portals;
    public TeacherPortalWebController(PortalService portals) { this.portals = portals; }
    @GetMapping
    public String dashboard(Model model) { model.addAttribute("portal", portals.teacherPortal()); return "teacher-portal"; }
}
