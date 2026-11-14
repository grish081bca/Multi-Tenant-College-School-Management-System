package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.CollegeNoticeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/notifications")
@PreAuthorize("hasAnyAuthority('PERMISSION_STUDENT_PORTAL', 'PERMISSION_TEACHER_PORTAL')")
public class NotificationWebController {
    private final CollegeNoticeService notices;
    public NotificationWebController(CollegeNoticeService notices) { this.notices = notices; }

    @GetMapping
    public String list(Model model) { model.addAttribute("notifications", notices.notifications()); return "notifications-list"; }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable Long id, RedirectAttributes flash) {
        try { notices.markRead(id); flash.addFlashAttribute("success", "Notification marked as read."); }
        catch (Exception ex) { flash.addFlashAttribute("error", ex.getMessage() == null ? "The request could not be completed." : ex.getMessage()); }
        return "redirect:/web/notifications";
    }
}
