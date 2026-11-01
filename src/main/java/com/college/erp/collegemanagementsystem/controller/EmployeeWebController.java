package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.EmployeeDTO;
import com.college.erp.collegemanagementsystem.dto.request.EmployeeRequest;
import com.college.erp.collegemanagementsystem.enums.EmployeeType;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EmployeeService;
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
@RequestMapping("/web/employees")
@PreAuthorize("hasAuthority('PERMISSION_EMPLOYEE_READ')")
public class EmployeeWebController {
    private final EmployeeService service;
    private final AcademicManagementService academics;
    private final TenantService tenants;
    private final TenantAccessService tenantAccess;
    private final EntityChangeLogService changeLogs;

    public EmployeeWebController(EmployeeService service, AcademicManagementService academics, TenantService tenants,
                                 TenantAccessService tenantAccess, EntityChangeLogService changeLogs) {
        this.service = service;
        this.academics = academics;
        this.tenants = tenants;
        this.tenantAccess = tenantAccess;
        this.changeLogs = changeLogs;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, @RequestParam(required = false) EmployeeType type,
                       @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) Long tenantId, Model model) {
        model.addAttribute("page", service.search(q, type, page, size, tenantId));
        model.addAttribute("q", q);
        model.addAttribute("selectedType", type);
        model.addAttribute("employeeTypes", EmployeeType.values());
        model.addAttribute("statuses", Status.values());
        model.addAttribute("canWrite", canWrite());
        model.addAttribute("systemAdmin", isSystemAdmin());
        model.addAttribute("selectedTenantId", tenantId);
        if (isSystemAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
        var filters = WebPagination.filters();
        filters.put("q", q);
        filters.put("type", type);
        filters.put("tenantId", isSystemAdmin() ? tenantId : null);
        WebPagination.add(model, "/web/employees", size, filters);
        return "employees-list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERMISSION_EMPLOYEE_WRITE')")
    public String add(@RequestParam(required = false) Long tenantId, Model model) {
        prepare(new EmployeeRequest(), tenantId, model);
        model.addAttribute("isEdit", false);
        return "employees-form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_EMPLOYEE_WRITE')")
    public String create(@Valid @ModelAttribute("employee") EmployeeRequest request, BindingResult binding, @RequestParam(required = false) Long tenantId, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            flash.addFlashAttribute("error", "Please correct the highlighted employee fields.");
            flash.addFlashAttribute("employeeForm", request);
            return "redirect:/web/employees/add" + tenantQuery(tenantId);
        }
        try {
            service.create(request, tenantId);
            flash.addFlashAttribute("success", "Employee profile created.");
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
            flash.addFlashAttribute("employeeForm", request);
            return "redirect:/web/employees/add" + tenantQuery(tenantId);
        }
        return "redirect:/web/employees" + tenantQuery(tenantId);
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('PERMISSION_EMPLOYEE_WRITE')")
    public String edit(@PathVariable Long id, @RequestParam(required = false) Long tenantId, Model model, RedirectAttributes flash) {
        try {
            EmployeeDTO item = service.get(id, tenantId);
            prepare(from(item), tenantId, model);
            model.addAttribute("isEdit", true);
            model.addAttribute("id", id);
            return "employees-form";
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
            return "redirect:/web/employees" + tenantQuery(tenantId);
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_EMPLOYEE_WRITE')")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("employee") EmployeeRequest request, BindingResult binding,
                         @RequestParam(required = false) Long tenantId, @RequestParam(required = false) String remarks, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            flash.addFlashAttribute("error", "Please correct the highlighted employee fields.");
            flash.addFlashAttribute("employeeForm", request);
            return "redirect:/web/employees/" + id + "/edit" + tenantQuery(tenantId);
        }
        try {
            service.update(id, request, tenantId, remarks);
            flash.addFlashAttribute("success", "Employee profile updated.");
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
        }
        return "redirect:/web/employees" + tenantQuery(tenantId);
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @RequestParam(required = false) Long tenantId, Model model, RedirectAttributes flash) {
        try {
            model.addAttribute("employee", service.get(id, tenantId));
            model.addAttribute("statuses", Status.values());
            model.addAttribute("canWrite", canWrite());
            model.addAttribute("systemAdmin", isSystemAdmin());
            model.addAttribute("changeLogs", changeLogs.getRecentChanges("Employee", id));
            return "employees-detail";
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
            return "redirect:/web/employees" + tenantQuery(tenantId);
        }
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PERMISSION_EMPLOYEE_WRITE')")
    public String status(@PathVariable Long id, @RequestParam Status status, @RequestParam(required = false) String remarks, @RequestParam(required = false) Long tenantId, RedirectAttributes flash) {
        try {
            service.changeStatus(id, status, tenantId, remarks);
            flash.addFlashAttribute("success", "Employee status updated.");
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
        }
        return "redirect:/web/employees/" + id + tenantQuery(tenantId);
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('PERMISSION_EMPLOYEE_WRITE')")
    public String delete(@PathVariable Long id, @RequestParam(required = false) String remarks, @RequestParam(required = false) Long tenantId, RedirectAttributes flash) {
        try {
            service.delete(id, tenantId, remarks);
            flash.addFlashAttribute("success", "Employee profile deleted.");
        } catch (Exception ex) {
            flash.addFlashAttribute("error", message(ex));
        }
        return "redirect:/web/employees" + tenantQuery(tenantId);
    }

    private void prepare(EmployeeRequest form, Long requestedTenant, Model model) {
        if (model.containsAttribute("employeeForm")) form = (EmployeeRequest) model.asMap().get("employeeForm");
        Long tenantId = tenantAccess.resolve(requestedTenant, false);
        if (form.getStatus() == null) form.setStatus(Status.ACTIVE);
        model.addAttribute("employee", form);
        model.addAttribute("tenantId", tenantId);
        model.addAttribute("systemAdmin", isSystemAdmin());
        model.addAttribute("employeeTypes", EmployeeType.values());
        model.addAttribute("statuses", Status.values());
        if (isSystemAdmin()) model.addAttribute("tenants", tenants.getAllTenants());
        model.addAttribute("departments", academics.options("departments", tenantId));
    }

    private EmployeeRequest from(EmployeeDTO d) {
        var r = new EmployeeRequest();
        r.setEmployeeNumber(d.getEmployeeNumber());
        r.setEmployeeType(d.getEmployeeType());
        r.setFirstName(d.getFirstName());
        r.setMiddleName(d.getMiddleName());
        r.setLastName(d.getLastName());
        r.setEmail(d.getEmail());
        r.setPhone(d.getPhone());
        r.setDesignation(d.getDesignation());
        r.setDepartmentId(d.getDepartmentId());
        r.setHireDate(d.getHireDate());
        r.setStatus(d.getStatus());
        return r;
    }

    private boolean isSystemAdmin() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getPrincipal() instanceof AuthenticatedUserPrincipal p && (p.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SYSTEM_ADMIN || p.getUserType() == com.college.erp.collegemanagementsystem.enums.UserType.SUPER_ADMIN);
    }

    private boolean canWrite() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("PERMISSION_EMPLOYEE_WRITE"));
    }

    private String tenantQuery(Long id) {
        return isSystemAdmin() && id != null ? "?tenantId=" + id : "";
    }

    private String message(Exception e) {
        return e.getMessage() == null ? "The request could not be completed." : e.getMessage();
    }
}
