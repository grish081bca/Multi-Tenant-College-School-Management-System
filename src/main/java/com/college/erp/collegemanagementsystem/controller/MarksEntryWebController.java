package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.MarksEntryDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.request.MarksEntryRequest;
import com.college.erp.collegemanagementsystem.enums.MarkVerificationStatus;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.StudentMarksService;
import com.college.erp.collegemanagementsystem.service.TenantService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/web/marks")
public class MarksEntryWebController {
    private final StudentMarksService marks;
    private final EntityChangeLogService changeLogs;
    private final TenantService tenants;

    public MarksEntryWebController(StudentMarksService marks, EntityChangeLogService changeLogs, TenantService tenants) {
        this.marks = marks;
        this.changeLogs = changeLogs;
        this.tenants = tenants;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_READ')")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String verificationStatus,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       @RequestParam(required = false) Long tenantId, Model model) {
        MarkVerificationStatus filter = parseStatus(verificationStatus);
        int pageSize = PagablePage.normalizeSize(size);
        model.addAttribute("page", marks.search(q, filter, page, pageSize, tenantId));
        model.addAttribute("q", q);
        model.addAttribute("selectedVerificationStatus", filter);
        model.addAttribute("verificationStatuses", MarkVerificationStatus.values());
        populateScope(model, tenantId);
        Map<String, Object> retained = new LinkedHashMap<>();
        retained.put("q", q); retained.put("verificationStatus", filter); retained.put("tenantId", tenantId);
        com.college.erp.collegemanagementsystem.controller.WebPagination.add(model, "/web/marks", pageSize, retained);
        return "marks-list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_WRITE')")
    public String addForm(@RequestParam(required = false) Long tenantId, Model model) {
        populateForm(model, tenantId);
        if (!model.containsAttribute("form")) model.addAttribute("form", new MarksEntryRequest());
        model.addAttribute("isEdit", false);
        return "marks-form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_WRITE')")
    public String create(@ModelAttribute("form") MarksEntryRequest form, BindingResult binding,
                         @RequestParam(required = false) Long tenantId, Model model, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            flash.addFlashAttribute("error", "Examination, student enrollment, and a valid marks value are required.");
            flash.addFlashAttribute("form", form);
            return redirectAdd(tenantId);
        }
        try {
            marks.create(form, tenantId);
            flash.addFlashAttribute("success", "Marks entered and submitted for verification.");
            return redirectList(tenantId);
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
            flash.addFlashAttribute("form", form);
            return redirectAdd(tenantId);
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_WRITE')")
    public String editForm(@PathVariable Long id, @RequestParam(required = false) Long tenantId,
                           Model model, RedirectAttributes flash) {
        try {
            MarksEntryDTO record = marks.get(id, tenantId);
            populateForm(model, record.getTenantId());
            MarksEntryRequest form = new MarksEntryRequest();
            form.setExaminationId(record.getExaminationId());
            form.setEnrollmentId(record.getEnrollmentId());
            form.setMarksObtained(record.getMarksObtained());
            model.addAttribute("form", form);
            model.addAttribute("record", record);
            model.addAttribute("isEdit", true);
            return "marks-form";
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
            return redirectList(tenantId);
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_WRITE')")
    public String update(@PathVariable Long id, @ModelAttribute("form") MarksEntryRequest form,
                         BindingResult binding, @RequestParam(required = false) Long tenantId,
                         @RequestParam(required = false) String remarks, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            flash.addFlashAttribute("error", "Examination, student enrollment, and a valid marks value are required.");
            flash.addFlashAttribute("form", form);
            return redirectEdit(id, tenantId);
        }
        try {
            marks.update(id, form, tenantId, remarks);
            flash.addFlashAttribute("success", "Marks updated and submitted for verification.");
            return redirectList(tenantId);
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
            flash.addFlashAttribute("form", form);
            return redirectEdit(id, tenantId);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_MARKS_READ')")
    public String detail(@PathVariable Long id, @RequestParam(required = false) Long tenantId,
                         Model model, RedirectAttributes flash) {
        try {
            MarksEntryDTO record = marks.get(id, tenantId);
            model.addAttribute("record", record);
            model.addAttribute("platformAdmin", isPlatformAdmin());
            model.addAttribute("canWriteMarks", hasAuthority("PERMISSION_MARKS_WRITE"));
            model.addAttribute("changeLogs", changeLogs.getRecentChanges("StudentMark", id));
            return "marks-detail";
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
            return redirectList(tenantId);
        }
    }

    private void populateForm(Model model, Long tenantId) {
        model.addAttribute("platformAdmin", isPlatformAdmin());
        model.addAttribute("selectedTenantId", tenantId);
        model.addAttribute("enrollments", marks.enrollmentOptions(tenantId));
        model.addAttribute("examinations", marks.examinationOptions(tenantId));
        if (isPlatformAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
    }

    private void populateScope(Model model, Long tenantId) {
        model.addAttribute("platformAdmin", isPlatformAdmin());
        model.addAttribute("selectedTenantId", tenantId);
        model.addAttribute("canWriteMarks", hasAuthority("PERMISSION_MARKS_WRITE"));
        model.addAttribute("canVerifyMarks", hasAuthority("PERMISSION_MARKS_VERIFY"));
        if (isPlatformAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
    }

    private MarkVerificationStatus parseStatus(String value) {
        try { return value == null || value.isBlank() ? null : MarkVerificationStatus.valueOf(value); }
        catch (IllegalArgumentException ex) { return null; }
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
        return isPlatformAdmin() && tenantId != null ? "redirect:/web/marks?tenantId=" + tenantId : "redirect:/web/marks";
    }

    private String redirectAdd(Long tenantId) {
        return isPlatformAdmin() && tenantId != null ? "redirect:/web/marks/add?tenantId=" + tenantId : "redirect:/web/marks/add";
    }

    private String redirectEdit(Long id, Long tenantId) {
        return "redirect:/web/marks/" + id + "/edit" + (isPlatformAdmin() && tenantId != null ? "?tenantId=" + tenantId : "");
    }

    private String message(Exception ex) {
        return ex.getMessage() == null ? "The request could not be completed." : ex.getMessage();
    }
}
