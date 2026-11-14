package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.FeeCollectionDTO;
import com.college.erp.collegemanagementsystem.dto.request.FeeCollectionRequest;
import com.college.erp.collegemanagementsystem.enums.PaymentMethod;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.FeeManagementService;
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
@RequestMapping("/web/fee-collections")
@PreAuthorize("hasAuthority('PERMISSION_FEE_COLLECTION_READ')")
public class FeeCollectionWebController {
    private final FeeManagementService fees;
    private final AcademicManagementService academics;
    private final TenantService tenants;
    private final TenantAccessService tenantAccess;

    public FeeCollectionWebController(FeeManagementService fees, AcademicManagementService academics,
                                      TenantService tenants, TenantAccessService tenantAccess) {
        this.fees = fees; this.academics = academics; this.tenants = tenants; this.tenantAccess = tenantAccess;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) Long tenantId, Model model) {
        model.addAttribute("page", fees.searchCollections(q, page, size, tenantId)); model.addAttribute("q", q);
        scope(model, tenantId); var filters = WebPagination.filters(); filters.put("q", q); filters.put("tenantId", isPlatformAdmin() ? tenantId : null);
        WebPagination.add(model, "/web/fee-collections", size, filters); return "fee-collections-list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERMISSION_FEE_COLLECTION_WRITE')")
    public String add(@RequestParam(required = false) Long tenantId, Model model) {
        prepare(new FeeCollectionRequest(), tenantId, model); return "fee-collections-form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_FEE_COLLECTION_WRITE')")
    public String collect(@Valid @ModelAttribute("form") FeeCollectionRequest form, BindingResult binding,
                          @RequestParam(required = false) Long tenantId, RedirectAttributes flash) {
        if (binding.hasErrors()) { flash.addFlashAttribute("error", "Complete the required payment fields."); flash.addFlashAttribute("collectionForm", form); return "redirect:/web/fee-collections/add" + tenantQuery(tenantId); }
        try {
            FeeCollectionDTO saved = fees.collect(form, tenantId);
            flash.addFlashAttribute("success", "Payment collected. Receipt " + saved.getReceiptNumber() + " is ready.");
            return "redirect:/web/fee-collections/" + saved.getId() + "/receipt" + tenantQuery(tenantId);
        } catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); flash.addFlashAttribute("collectionForm", form); return "redirect:/web/fee-collections/add" + tenantQuery(tenantId); }
    }

    @GetMapping("/{id}/receipt")
    public String receipt(@PathVariable Long id, @RequestParam(required = false) Long tenantId, Model model, RedirectAttributes flash) {
        try { model.addAttribute("receipt", fees.getCollection(id, tenantId)); return "fee-receipt"; }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); return "redirect:/web/fee-collections" + tenantQuery(tenantId); }
    }

    private void prepare(FeeCollectionRequest form, Long requestedTenant, Model model) {
        if (model.containsAttribute("collectionForm")) form = (FeeCollectionRequest) model.asMap().get("collectionForm");
        Long tenantId = tenantAccess.resolve(requestedTenant, false);
        model.addAttribute("form", form); model.addAttribute("selectedTenantId", tenantId);
        model.addAttribute("platformAdmin", isPlatformAdmin()); model.addAttribute("paymentMethods", PaymentMethod.values());
        if (isPlatformAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
        if (!isPlatformAdmin() || tenantId != null) {
            model.addAttribute("enrollments", academics.options("student-enrollments", tenantId).stream().filter(o -> o.getStatus() == Status.ACTIVE).toList());
            model.addAttribute("feeStructures", fees.searchStructures(null, Status.ACTIVE, 1, 100, tenantId).getObjects());
        } else {
            model.addAttribute("enrollments", java.util.List.of()); model.addAttribute("feeStructures", java.util.List.of());
        }
    }
    private void scope(Model model, Long tenantId) { model.addAttribute("selectedTenantId", tenantId); model.addAttribute("platformAdmin", isPlatformAdmin()); model.addAttribute("canWrite", hasAuthority("PERMISSION_FEE_COLLECTION_WRITE")); if (isPlatformAdmin()) model.addAttribute("tenants", tenants.getAllTenants()); }
    private boolean isPlatformAdmin() { Authentication a = SecurityContextHolder.getContext().getAuthentication(); return a != null && a.getPrincipal() instanceof AuthenticatedUserPrincipal p && (p.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SUPER_ADMIN || p.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SYSTEM_ADMIN); }
    private boolean hasAuthority(String authority) { Authentication a = SecurityContextHolder.getContext().getAuthentication(); return a != null && a.getAuthorities().stream().anyMatch(item -> authority.equals(item.getAuthority())); }
    private String tenantQuery(Long id) { return isPlatformAdmin() && id != null ? "?tenantId=" + id : ""; }
    private String message(Exception ex) { return ex.getMessage() == null ? "The request could not be completed." : ex.getMessage(); }
}
