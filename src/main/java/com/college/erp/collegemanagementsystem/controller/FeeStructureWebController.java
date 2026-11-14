package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.FeeStructureDTO;
import com.college.erp.collegemanagementsystem.dto.request.FeeStructureRequest;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.FeeManagementService;
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
@RequestMapping("/web/fee-structures")
@PreAuthorize("hasAuthority('PERMISSION_FEE_STRUCTURE_READ')")
public class FeeStructureWebController {
    private final FeeManagementService fees;
    private final AcademicManagementService academics;
    private final TenantService tenants;

    public FeeStructureWebController(FeeManagementService fees, AcademicManagementService academics, TenantService tenants) {
        this.fees = fees; this.academics = academics; this.tenants = tenants;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, @RequestParam(required = false) Status status,
                       @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) Long tenantId, Model model) {
        model.addAttribute("page", fees.searchStructures(q, status, page, size, tenantId));
        model.addAttribute("q", q); model.addAttribute("selectedStatus", status); model.addAttribute("statuses", Status.values());
        scope(model, tenantId);
        var filters = WebPagination.filters(); filters.put("q", q); filters.put("status", status); filters.put("tenantId", isPlatformAdmin() ? tenantId : null);
        WebPagination.add(model, "/web/fee-structures", size, filters);
        return "fee-structures-list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERMISSION_FEE_STRUCTURE_WRITE')")
    public String add(@RequestParam(required = false) Long tenantId, Model model) {
        prepare(new FeeStructureRequest(), tenantId, model); model.addAttribute("isEdit", false); return "fee-structures-form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_FEE_STRUCTURE_WRITE')")
    public String create(@Valid @ModelAttribute("form") FeeStructureRequest form, BindingResult binding,
                        @RequestParam(required = false) Long tenantId, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            flash.addFlashAttribute("error", "Complete the required fee structure fields.");
            flash.addFlashAttribute("feeStructureForm", form);
            return "redirect:/web/fee-structures/add" + tenantQuery(tenantId);
        }
        try { fees.createStructure(form, tenantId); flash.addFlashAttribute("success", "Fee structure created."); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); flash.addFlashAttribute("feeStructureForm", form); return "redirect:/web/fee-structures/add" + tenantQuery(tenantId); }
        return "redirect:/web/fee-structures" + tenantQuery(tenantId);
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('PERMISSION_FEE_STRUCTURE_WRITE')")
    public String edit(@PathVariable Long id, @RequestParam(required = false) Long tenantId, Model model, RedirectAttributes flash) {
        try {
            FeeStructureDTO value = fees.getStructure(id, tenantId); FeeStructureRequest form = from(value);
            prepare(form, tenantId == null ? value.getTenantId() : tenantId, model);
            model.addAttribute("record", value); model.addAttribute("isEdit", true); return "fee-structures-form";
        } catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); return "redirect:/web/fee-structures" + tenantQuery(tenantId); }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_FEE_STRUCTURE_WRITE')")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") FeeStructureRequest form, BindingResult binding,
                        @RequestParam(required = false) Long tenantId, @RequestParam(required = false) String remarks,
                        RedirectAttributes flash) {
        if (binding.hasErrors()) { flash.addFlashAttribute("error", "Complete the required fee structure fields."); flash.addFlashAttribute("feeStructureForm", form); return "redirect:/web/fee-structures/" + id + "/edit" + tenantQuery(tenantId); }
        try { fees.updateStructure(id, form, tenantId, remarks); flash.addFlashAttribute("success", "Fee structure updated."); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); }
        return "redirect:/web/fee-structures" + tenantQuery(tenantId);
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @RequestParam(required = false) Long tenantId, Model model, RedirectAttributes flash) {
        try { model.addAttribute("record", fees.getStructure(id, tenantId)); model.addAttribute("platformAdmin", isPlatformAdmin()); model.addAttribute("canWrite", hasAuthority("PERMISSION_FEE_STRUCTURE_WRITE")); return "fee-structures-detail"; }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); return "redirect:/web/fee-structures" + tenantQuery(tenantId); }
    }

    private void prepare(FeeStructureRequest form, Long tenantId, Model model) {
        if (model.containsAttribute("feeStructureForm")) form = (FeeStructureRequest) model.asMap().get("feeStructureForm");
        model.addAttribute("form", form); model.addAttribute("selectedTenantId", tenantId); model.addAttribute("platformAdmin", isPlatformAdmin());
        model.addAttribute("statuses", new Status[]{Status.ACTIVE, Status.INACTIVE});
        if (isPlatformAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
        if (!isPlatformAdmin() || tenantId != null) {
            model.addAttribute("programs", academics.options("programs", tenantId));
            model.addAttribute("academicYears", academics.options("academic-years", tenantId));
            model.addAttribute("semesters", academics.options("semesters", tenantId));
        } else {
            model.addAttribute("programs", java.util.List.of()); model.addAttribute("academicYears", java.util.List.of());
            model.addAttribute("semesters", java.util.List.of());
        }
    }
    private void scope(Model model, Long tenantId) { model.addAttribute("selectedTenantId", tenantId); model.addAttribute("platformAdmin", isPlatformAdmin()); model.addAttribute("canWrite", hasAuthority("PERMISSION_FEE_STRUCTURE_WRITE")); if (isPlatformAdmin()) model.addAttribute("tenants", tenants.getAllTenants()); }
    private FeeStructureRequest from(FeeStructureDTO d) { FeeStructureRequest r = new FeeStructureRequest(); r.setProgramId(d.getProgramId()); r.setAcademicYearId(d.getAcademicYearId()); r.setSemesterId(d.getSemesterId()); r.setCode(d.getCode()); r.setName(d.getName()); r.setDescription(d.getDescription()); r.setAmount(d.getAmount()); r.setDueDate(d.getDueDate()); r.setStatus(d.getStatus()); return r; }
    private boolean isPlatformAdmin() { Authentication a = SecurityContextHolder.getContext().getAuthentication(); return a != null && a.getPrincipal() instanceof AuthenticatedUserPrincipal p && (p.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SUPER_ADMIN || p.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SYSTEM_ADMIN); }
    private boolean hasAuthority(String authority) { Authentication a = SecurityContextHolder.getContext().getAuthentication(); return a != null && a.getAuthorities().stream().anyMatch(item -> authority.equals(item.getAuthority())); }
    private String tenantQuery(Long id) { return isPlatformAdmin() && id != null ? "?tenantId=" + id : ""; }
    private String message(Exception ex) { return ex.getMessage() == null ? "The request could not be completed." : ex.getMessage(); }
}
