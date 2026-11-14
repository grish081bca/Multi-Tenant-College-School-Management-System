package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.enums.MarkVerificationStatus;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.StudentMarksService;
import com.college.erp.collegemanagementsystem.service.TenantService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/marks-verification")
public class MarksVerificationWebController {
    private final StudentMarksService marks;
    private final TenantService tenants;

    public MarksVerificationWebController(StudentMarksService marks, TenantService tenants) {
        this.marks = marks;
        this.tenants = tenants;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_READ')")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       @RequestParam(required = false) Long tenantId, Model model) {
        int pageSize = PagablePage.normalizeSize(size);
        model.addAttribute("page", marks.search(q, MarkVerificationStatus.PENDING, page, pageSize, tenantId));
        model.addAttribute("q", q);
        model.addAttribute("platformAdmin", isPlatformAdmin());
        model.addAttribute("selectedTenantId", tenantId);
        model.addAttribute("canVerifyMarks", hasAuthority("PERMISSION_MARKS_VERIFY"));
        model.addAttribute("pageSize", pageSize);
        if (isPlatformAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
        WebPagination.add(model, "/web/marks-verification", pageSize, java.util.Map.of("q", q == null ? "" : q, "tenantId", tenantId == null ? "" : tenantId));
        return "marks-verification-list";
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_VERIFY')")
    public String verify(@PathVariable Long id, @RequestParam(required = false) Long tenantId,
                         @RequestParam(required = false) String remarks, RedirectAttributes flash) {
        try {
            marks.verify(id, tenantId, remarks);
            flash.addFlashAttribute("success", "Marks verified successfully.");
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
        }
        return redirectList(tenantId);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_VERIFY')")
    public String reject(@PathVariable Long id, @RequestParam(required = false) Long tenantId,
                         @RequestParam String remarks, RedirectAttributes flash) {
        try {
            marks.reject(id, tenantId, remarks);
            flash.addFlashAttribute("success", "Marks rejected successfully.");
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
        }
        return redirectList(tenantId);
    }

    private boolean isPlatformAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof AuthenticatedUserPrincipal principal
                && (principal.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SUPER_ADMIN
                || principal.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SYSTEM_ADMIN);
    }

    private boolean hasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(item -> authority.equals(item.getAuthority()));
    }

    private String redirectList(Long tenantId) {
        return isPlatformAdmin() && tenantId != null ? "redirect:/web/marks-verification?tenantId=" + tenantId : "redirect:/web/marks-verification";
    }

    private String message(Exception ex) {
        return ex.getMessage() == null ? "The request could not be completed." : ex.getMessage();
    }
}
