package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.StudentResultDTO;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.StudentResultsService;
import com.college.erp.collegemanagementsystem.service.TenantService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/web/results")
public class ResultsWebController {
    private final StudentResultsService results;
    private final EntityChangeLogService changeLogs;
    private final TenantService tenants;

    public ResultsWebController(StudentResultsService results, EntityChangeLogService changeLogs, TenantService tenants) {
        this.results = results;
        this.changeLogs = changeLogs;
        this.tenants = tenants;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_RESULTS_READ')")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       @RequestParam(required = false) Long tenantId, Model model) {
        int pageSize = PagablePage.normalizeSize(size);
        model.addAttribute("page", results.search(q, page, pageSize, tenantId));
        model.addAttribute("q", q);
        model.addAttribute("pageSize", pageSize);
        populateScope(model, tenantId);
        model.addAttribute("enrollments", results.enrollmentOptions(tenantId));
        Map<String, Object> retained = new LinkedHashMap<>();
        retained.put("q", q); retained.put("tenantId", tenantId);
        WebPagination.add(model, "/web/results", pageSize, retained);
        return "results-list";
    }

    @PostMapping("/process")
    @PreAuthorize("hasAuthority('PERMISSION_RESULTS_PROCESS')")
    public String process(@RequestParam Long enrollmentId, @RequestParam(required = false) Long tenantId,
                          RedirectAttributes flash) {
        try {
            StudentResultDTO result = results.process(enrollmentId, tenantId);
            flash.addFlashAttribute("success", "Result processed successfully. GPA: " + result.getGpa());
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
        }
        return redirectList(tenantId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_RESULTS_READ')")
    public String detail(@PathVariable Long id, @RequestParam(required = false) Long tenantId,
                         Model model, RedirectAttributes flash) {
        try {
            StudentResultDTO result = results.get(id, tenantId);
            model.addAttribute("result", result);
            model.addAttribute("platformAdmin", isPlatformAdmin());
            model.addAttribute("changeLogs", changeLogs.getRecentChanges("StudentResult", id));
            return "results-detail";
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
            return redirectList(tenantId);
        }
    }

    private void populateScope(Model model, Long tenantId) {
        model.addAttribute("platformAdmin", isPlatformAdmin());
        model.addAttribute("selectedTenantId", tenantId);
        model.addAttribute("canProcessResults", hasAuthority("PERMISSION_RESULTS_PROCESS"));
        if (isPlatformAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
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
        return isPlatformAdmin() && tenantId != null ? "redirect:/web/results?tenantId=" + tenantId : "redirect:/web/results";
    }

    private String message(Exception ex) {
        return ex.getMessage() == null ? "The request could not be completed." : ex.getMessage();
    }
}
