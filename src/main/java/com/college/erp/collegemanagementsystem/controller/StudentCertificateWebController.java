package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.request.StudentCertificateRequest;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.StudentCertificateService;
import com.college.erp.collegemanagementsystem.service.TenantAccessService;
import com.college.erp.collegemanagementsystem.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/certificates")
@PreAuthorize("hasAuthority('PERMISSION_CERTIFICATE_READ')")
public class StudentCertificateWebController {
    private final StudentCertificateService certificates;
    private final AcademicManagementService academics;
    private final TenantAccessService tenantAccess;
    private final TenantService tenants;
    private final EntityChangeLogService changeLogs;

    public StudentCertificateWebController(StudentCertificateService certificates, AcademicManagementService academics,
                                           TenantAccessService tenantAccess, TenantService tenants, EntityChangeLogService changeLogs) {
        this.certificates = certificates;
        this.academics = academics;
        this.tenantAccess = tenantAccess;
        this.tenants = tenants;
        this.changeLogs = changeLogs;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, @RequestParam(required = false) Status status,
                       @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) Long tenantId, Model model) {
        model.addAttribute("page", certificates.search(q, status, page, size, tenantId));
        model.addAttribute("q", q);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", Status.values());
        populateScope(model, tenantId);
        var filters = WebPagination.filters();
        filters.put("q", q); filters.put("status", status); filters.put("tenantId", isPlatformAdmin() ? tenantId : null);
        WebPagination.add(model, "/web/certificates", size, filters);
        return "certificates-list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERMISSION_CERTIFICATE_WRITE')")
    public String add(@RequestParam(required = false) Long tenantId, Model model) {
        prepare(new StudentCertificateRequest(), tenantId, model);
        model.addAttribute("isEdit", false);
        return "certificates-form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CERTIFICATE_WRITE')")
    public String issue(@Valid @ModelAttribute("form") StudentCertificateRequest form, BindingResult binding,
                        @RequestParam(required = false) Long tenantId, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            flash.addFlashAttribute("error", "Complete the required certificate fields.");
            flash.addFlashAttribute("certificateForm", form);
            return "redirect:/web/certificates/add" + tenantQuery(tenantId);
        }
        try {
            certificates.issue(form, tenantId);
            flash.addFlashAttribute("success", "Student certificate issued.");
            return "redirect:/web/certificates" + tenantQuery(tenantId);
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
            flash.addFlashAttribute("certificateForm", form);
            return "redirect:/web/certificates/add" + tenantQuery(tenantId);
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @RequestParam(required = false) Long tenantId,
                         Model model, RedirectAttributes flash) {
        try {
            var item = certificates.get(id, tenantId);
            model.addAttribute("certificate", item);
            model.addAttribute("canWrite", hasAuthority("PERMISSION_CERTIFICATE_WRITE"));
            model.addAttribute("platformAdmin", isPlatformAdmin());
            model.addAttribute("changeLogs", changeLogs.getRecentChanges("StudentCertificate", id));
            return "certificates-detail";
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
            return "redirect:/web/certificates" + tenantQuery(tenantId);
        }
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasAuthority('PERMISSION_CERTIFICATE_WRITE')")
    public String revoke(@PathVariable Long id, @RequestParam String reason,
                         @RequestParam(required = false) Long tenantId, RedirectAttributes flash) {
        try {
            certificates.revoke(id, reason, tenantId);
            flash.addFlashAttribute("success", "Student certificate revoked.");
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
        }
        return "redirect:/web/certificates/" + id + tenantQuery(tenantId);
    }

    private void prepare(StudentCertificateRequest form, Long tenantId, Model model) {
        if (model.containsAttribute("certificateForm")) form = (StudentCertificateRequest) model.asMap().get("certificateForm");
        Long scope = tenantAccess.resolve(tenantId, false);
        if (form.getIssuedOn() == null) form.setIssuedOn(java.time.LocalDate.now());
        model.addAttribute("form", form);
        model.addAttribute("selectedTenantId", scope);
        model.addAttribute("platformAdmin", isPlatformAdmin());
        if (isPlatformAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
        model.addAttribute("enrollments", isPlatformAdmin() && scope == null ? java.util.List.of()
                : academics.options("student-enrollments", scope).stream().filter(option -> option.getStatus() == Status.ACTIVE).toList());
    }

    private void populateScope(Model model, Long tenantId) {
        model.addAttribute("selectedTenantId", tenantId);
        model.addAttribute("platformAdmin", isPlatformAdmin());
        model.addAttribute("canWrite", hasAuthority("PERMISSION_CERTIFICATE_WRITE"));
        if (isPlatformAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
    }

    private boolean isPlatformAdmin() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getPrincipal() instanceof AuthenticatedUserPrincipal p
                && (p.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SUPER_ADMIN
                || p.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SYSTEM_ADMIN);
    }
    private boolean hasAuthority(String authority) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(item -> authority.equals(item.getAuthority()));
    }
    private String tenantQuery(Long tenantId) { return isPlatformAdmin() && tenantId != null ? "?tenantId=" + tenantId : ""; }
    private String message(Exception ex) { return ex.getMessage() == null ? "The request could not be completed." : ex.getMessage(); }
}
