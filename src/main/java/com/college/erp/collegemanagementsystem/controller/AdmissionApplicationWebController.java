package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.request.AdmissionApplicationRequest;
import com.college.erp.collegemanagementsystem.enums.AdmissionStatus;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.AdmissionApplicationService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
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
@RequestMapping("/web/admissions")
@PreAuthorize("hasAuthority('PERMISSION_ADMISSION_READ')")
public class AdmissionApplicationWebController {
    private final AdmissionApplicationService service;
    private final AcademicManagementService academics;
    private final TenantService tenants;
    private final TenantAccessService tenantAccess;
    private final EntityChangeLogService changeLogs;

    public AdmissionApplicationWebController(AdmissionApplicationService service, AcademicManagementService academics,
                                             TenantService tenants, TenantAccessService tenantAccess, EntityChangeLogService changeLogs) {
        this.service = service;
        this.academics = academics;
        this.tenants = tenants;
        this.tenantAccess = tenantAccess;
        this.changeLogs = changeLogs;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, @RequestParam(required = false) AdmissionStatus status,
                       @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) Long tenantId, Model model) {
        model.addAttribute("page", service.search(q, status, page, size, tenantId));
        model.addAttribute("q", q);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", AdmissionStatus.values());
        model.addAttribute("systemAdmin", isSystemAdmin());
        model.addAttribute("canWrite", canWrite());
        model.addAttribute("selectedTenantId", tenantId);
        if (isSystemAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
        var filters = WebPagination.filters();
        filters.put("q", q);
        filters.put("status", status);
        filters.put("tenantId", isSystemAdmin() ? tenantId : null);
        WebPagination.add(model, "/web/admissions", size, filters);
        return "admissions-list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERMISSION_ADMISSION_WRITE')")
    public String add(@RequestParam(required = false) Long tenantId, Model model) {
        prepare(new AdmissionApplicationRequest(), tenantId, model);
        model.addAttribute("isEdit", false);
        return "admissions-form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_ADMISSION_WRITE')")
    public String create(@Valid @ModelAttribute("application") AdmissionApplicationRequest request, BindingResult binding,
                         @RequestParam(required = false) Long tenantId, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            flash.addFlashAttribute("error", "Please correct the highlighted admission fields.");
            flash.addFlashAttribute("admissionForm", request);
            return "redirect:/web/admissions/add" + tenantQuery(tenantId);
        }
        try {
            service.create(request, tenantId);
            flash.addFlashAttribute("success", "Admission application created.");
            return "redirect:/web/admissions" + tenantQuery(tenantId);
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
            flash.addFlashAttribute("admissionForm", request);
            return "redirect:/web/admissions/add" + tenantQuery(tenantId);
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('PERMISSION_ADMISSION_WRITE')")
    public String edit(@PathVariable Long id, @RequestParam(required = false) Long tenantId, Model model, RedirectAttributes flash) {
        try {
            var item = service.get(id, tenantId);
            AdmissionApplicationRequest form = from(item);
            prepare(form, tenantId, model);
            model.addAttribute("isEdit", true);
            model.addAttribute("id", id);
            return "admissions-form";
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
            return "redirect:/web/admissions" + tenantQuery(tenantId);
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_ADMISSION_WRITE')")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("application") AdmissionApplicationRequest request, BindingResult binding,
                         @RequestParam(required = false) Long tenantId, @RequestParam(required = false) String remarks, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            flash.addFlashAttribute("error", "Please correct the highlighted admission fields.");
            flash.addFlashAttribute("admissionForm", request);
            return "redirect:/web/admissions/" + id + "/edit" + tenantQuery(tenantId);
        }
        try {
            service.update(id, request, tenantId, remarks);
            flash.addFlashAttribute("success", "Admission application updated.");
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
        }
        return "redirect:/web/admissions" + tenantQuery(tenantId);
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @RequestParam(required = false) Long tenantId, Model model, RedirectAttributes flash) {
        try {
            var item = service.get(id, tenantId);
            model.addAttribute("application", item);
            model.addAttribute("tenantId", item.getTenantId());
            model.addAttribute("systemAdmin", isSystemAdmin());
            model.addAttribute("canWrite", canWrite());
            model.addAttribute("changeLogs", changeLogs.getRecentChanges("AdmissionApplication", id));
            return "admissions-detail";
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
            return "redirect:/web/admissions" + tenantQuery(tenantId);
        }
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PERMISSION_ADMISSION_WRITE')")
    public String status(@PathVariable Long id, @RequestParam AdmissionStatus status, @RequestParam(required = false) String remarks,
                         @RequestParam(required = false) Long tenantId, RedirectAttributes flash) {
        try {
            service.transition(id, status, tenantId, remarks);
            flash.addFlashAttribute("success", "Admission status updated.");
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
        }
        return "redirect:/web/admissions/" + id + tenantQuery(tenantId);
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('PERMISSION_ADMISSION_WRITE')")
    public String delete(@PathVariable Long id, @RequestParam(required = false) String remarks, @RequestParam(required = false) Long tenantId, RedirectAttributes flash) {
        try {
            service.delete(id, tenantId, remarks);
            flash.addFlashAttribute("success", "Admission application deleted.");
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
        }
        return "redirect:/web/admissions" + tenantQuery(tenantId);
    }

    private void prepare(AdmissionApplicationRequest form, Long requestedTenant, Model model) {
        if (model.containsAttribute("admissionForm"))
            form = (AdmissionApplicationRequest) model.asMap().get("admissionForm");
        Long tenantId = tenantAccess.resolve(requestedTenant, false);
        if (form.getApplicationDate() == null) form.setApplicationDate(java.time.LocalDate.now());
        model.addAttribute("application", form);
        model.addAttribute("tenantId", tenantId);
        model.addAttribute("systemAdmin", isSystemAdmin());
        if (isSystemAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
        model.addAttribute("programs", academics.options("programs", tenantId));
        model.addAttribute("academicYears", academics.options("academic-years", tenantId));
        model.addAttribute("semesters", academics.options("semesters", tenantId));
        model.addAttribute("sections", academics.options("sections", tenantId));
        model.addAttribute("today", java.time.LocalDate.now());
    }

    private AdmissionApplicationRequest from(com.college.erp.collegemanagementsystem.dto.AdmissionApplicationDTO d) {
        var r = new AdmissionApplicationRequest();
        r.setFirstName(d.getFirstName());
        r.setMiddleName(d.getMiddleName());
        r.setLastName(d.getLastName());
        r.setEmail(d.getEmail());
        r.setPhone(d.getPhone());
        r.setDateOfBirth(d.getDateOfBirth());
        r.setAddress(d.getAddress());
        r.setProgramId(d.getProgramId());
        r.setAcademicYearId(d.getAcademicYearId());
        r.setSemesterId(d.getSemesterId());
        r.setSectionId(d.getSectionId());
        r.setApplicationDate(d.getApplicationDate());
        r.setRemarks(d.getRemarks());
        return r;
    }

    private boolean isSystemAdmin() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getPrincipal() instanceof AuthenticatedUserPrincipal p && (p.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SYSTEM_ADMIN || p.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SUPER_ADMIN);
    }

    private boolean canWrite() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("PERMISSION_ADMISSION_WRITE"));
    }

    private String tenantQuery(Long id) {
        return isSystemAdmin() && id != null ? "?tenantId=" + id : "";
    }

    private String message(Exception e) {
        return e.getMessage() == null ? "The request could not be completed." : e.getMessage();
    }
}
