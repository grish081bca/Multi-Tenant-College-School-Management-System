package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.request.CollegeNoticeRequest;
import com.college.erp.collegemanagementsystem.enums.NoticeAudience;
import com.college.erp.collegemanagementsystem.enums.NoticeStatus;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.CollegeNoticeService;
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
@RequestMapping("/web/notices")
@PreAuthorize("hasAuthority('PERMISSION_NOTICE_READ')")
public class CollegeNoticeWebController {
    private final CollegeNoticeService notices;
    private final TenantService tenants;

    public CollegeNoticeWebController(CollegeNoticeService notices, TenantService tenants) { this.notices = notices; this.tenants = tenants; }

    @GetMapping
    public String list(@RequestParam(required = false) String q, @RequestParam(required = false) NoticeStatus status,
                       @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) Long tenantId, Model model) {
        model.addAttribute("page", notices.search(q, status, page, size, tenantId)); model.addAttribute("q", q);
        model.addAttribute("selectedStatus", status); model.addAttribute("statuses", NoticeStatus.values());
        model.addAttribute("selectedTenantId", tenantId); model.addAttribute("platformAdmin", isPlatformAdmin());
        model.addAttribute("canWrite", hasAuthority("PERMISSION_NOTICE_WRITE")); if (isPlatformAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
        var filters = WebPagination.filters(); filters.put("q", q); filters.put("status", status); filters.put("tenantId", isPlatformAdmin() ? tenantId : null);
        WebPagination.add(model, "/web/notices", size, filters); return "notices-list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERMISSION_NOTICE_WRITE')")
    public String add(@RequestParam(required = false) Long tenantId, Model model) { prepare(new CollegeNoticeRequest(), tenantId, model); return "notices-form"; }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_NOTICE_WRITE')")
    public String create(@Valid @ModelAttribute("form") CollegeNoticeRequest form, BindingResult binding,
                         @RequestParam(required = false) Long tenantId, RedirectAttributes flash) {
        if (binding.hasErrors()) { flash.addFlashAttribute("error", "Complete the required notice fields."); flash.addFlashAttribute("noticeForm", form); return "redirect:/web/notices/add" + tenantQuery(tenantId); }
        try { notices.create(form, tenantId); flash.addFlashAttribute("success", "Notice draft created."); return "redirect:/web/notices" + tenantQuery(tenantId); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); flash.addFlashAttribute("noticeForm", form); return "redirect:/web/notices/add" + tenantQuery(tenantId); }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('PERMISSION_NOTICE_WRITE')")
    public String edit(@PathVariable Long id, @RequestParam(required = false) Long tenantId, Model model, RedirectAttributes flash) {
        try { var item = notices.get(id, tenantId); CollegeNoticeRequest form = new CollegeNoticeRequest(); form.setTitle(item.getTitle()); form.setBody(item.getBody()); form.setAudience(item.getAudience()); form.setDescription(item.getDescription()); prepare(form, tenantId == null ? item.getTenantId() : tenantId, model); model.addAttribute("id", id); return "notices-form"; }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); return "redirect:/web/notices" + tenantQuery(tenantId); }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_NOTICE_WRITE')")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") CollegeNoticeRequest form, BindingResult binding,
                         @RequestParam(required = false) Long tenantId, @RequestParam(required = false) String remarks, RedirectAttributes flash) {
        if (binding.hasErrors()) { flash.addFlashAttribute("error", "Complete the required notice fields."); flash.addFlashAttribute("noticeForm", form); return "redirect:/web/notices/" + id + "/edit" + tenantQuery(tenantId); }
        try { notices.update(id, form, tenantId, remarks); flash.addFlashAttribute("success", "Notice draft updated."); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); }
        return "redirect:/web/notices" + tenantQuery(tenantId);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('PERMISSION_NOTICE_WRITE')")
    public String publish(@PathVariable Long id, @RequestParam(required = false) Long tenantId, RedirectAttributes flash) {
        try { notices.publish(id, tenantId); flash.addFlashAttribute("success", "Notice published and inbox notifications created."); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); }
        return "redirect:/web/notices" + tenantQuery(tenantId);
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('PERMISSION_NOTICE_WRITE')")
    public String archive(@PathVariable Long id, @RequestParam(required = false) Long tenantId, RedirectAttributes flash) {
        try { notices.archive(id, tenantId); flash.addFlashAttribute("success", "Notice archived."); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); }
        return "redirect:/web/notices" + tenantQuery(tenantId);
    }

    private void prepare(CollegeNoticeRequest form, Long tenantId, Model model) {
        if (model.containsAttribute("noticeForm")) form = (CollegeNoticeRequest) model.asMap().get("noticeForm");
        model.addAttribute("form", form); model.addAttribute("audiences", NoticeAudience.values()); model.addAttribute("selectedTenantId", tenantId);
        model.addAttribute("platformAdmin", isPlatformAdmin()); if (isPlatformAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
    }
    private boolean isPlatformAdmin() { Authentication a = SecurityContextHolder.getContext().getAuthentication(); return a != null && a.getPrincipal() instanceof AuthenticatedUserPrincipal p && (p.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SUPER_ADMIN || p.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SYSTEM_ADMIN); }
    private boolean hasAuthority(String authority) { Authentication a = SecurityContextHolder.getContext().getAuthentication(); return a != null && a.getAuthorities().stream().anyMatch(item -> authority.equals(item.getAuthority())); }
    private String tenantQuery(Long id) { return isPlatformAdmin() && id != null ? "?tenantId=" + id : ""; }
    private String message(Exception ex) { return ex.getMessage() == null ? "The request could not be completed." : ex.getMessage(); }
}
