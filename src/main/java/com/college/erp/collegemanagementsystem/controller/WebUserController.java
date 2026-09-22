package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.UserDTO;
import com.college.erp.collegemanagementsystem.enums.UserStatus;
import com.college.erp.collegemanagementsystem.enums.UserType;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.TenantService;
import com.college.erp.collegemanagementsystem.service.UserService;
import com.college.erp.collegemanagementsystem.service.UserTemplateService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

/**
 * @author grish
 *
 */
@Controller
public class WebUserController {
    private static final String USER_ENTITY = "User";

    private final com.college.erp.collegemanagementsystem.service.UserService userService;
    private final TenantService tenantService;
    private final UserTemplateService userTemplateService;
    private final EntityChangeLogService entityChangeLogService;

    public WebUserController(UserService userService,
                             TenantService tenantService,
                             UserTemplateService userTemplateService,
                             EntityChangeLogService entityChangeLogService) {
        this.userService = userService;
        this.tenantService = tenantService;
        this.userTemplateService = userTemplateService;
        this.entityChangeLogService = entityChangeLogService;
    }
    @GetMapping("/web/users")
    public String list(@RequestParam(required = false, name = "q") String search,
                       @RequestParam(required = false) String username,
                       @RequestParam(required = false) String fullName,
                       @RequestParam(required = false) String email,
                       @RequestParam(required = false) String phoneNumber,
                       @RequestParam(required = false) Long tenantId,
                       @RequestParam(required = false) UserType userType,
                       @RequestParam(required = false) Boolean enabled,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                       @RequestParam(required = false) UserStatus status,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer size,
                       Model m) {
        m.addAttribute("page", userService.findPage(search, username, fullName, email, phoneNumber, tenantId, userType, enabled, fromDate, toDate, status, page, size));
        m.addAttribute("search", search);
        m.addAttribute("usernames", userService.getUsernames());
        m.addAttribute("selectedUsername", username);
        m.addAttribute("fullName", fullName);
        m.addAttribute("email", email);
        m.addAttribute("phoneNumber", phoneNumber);
        m.addAttribute("tenants", tenantService.getAllTenants());
        m.addAttribute("tenantId", tenantId);
        m.addAttribute("selectedUserType", userType);
        m.addAttribute("enabled", enabled);
        m.addAttribute("fromDate", fromDate);
        m.addAttribute("toDate", toDate);
        m.addAttribute("selectedStatus", status);
        m.addAttribute("statuses", UserStatus.values());
        m.addAttribute("userTypes", UserType.values());
        var filters = WebPagination.filters();
        filters.put("q", search);
        filters.put("username", username);
        filters.put("fullName", fullName);
        filters.put("email", email);
        filters.put("phoneNumber", phoneNumber);
        filters.put("tenantId", tenantId);
        filters.put("userType", userType);
        filters.put("enabled", enabled);
        filters.put("fromDate", fromDate);
        filters.put("toDate", toDate);
        filters.put("status", status);
        WebPagination.add(m, "/web/users", size, filters);
        return "users";
    }

    private void addModelAttributes(Model m) {
        m.addAttribute("tenants", tenantService.getAllTenants());
        m.addAttribute("userTemplates", userTemplateService.findAll());
        m.addAttribute("userTypes", UserType.values());
        m.addAttribute("statuses", UserStatus.values());
        m.addAttribute("user", new UserDTO());
    }
    @GetMapping("/web/users/add")
    public String addForm(Model m) {
        addModelAttributes(m);
        return "user-add";
    }
    @PostMapping("/web/users")
    public String create(@ModelAttribute UserDTO user,
                         @RequestParam(required = false) Long tenantId,
                         @RequestParam(required = false) Long userTemplateId,
                         RedirectAttributes attributes) {
        try {
            userService.create(user, tenantId, userTemplateId);
            attributes.addFlashAttribute("success", "User saved successfully.");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/users/add";
        }
        return "redirect:/web/users";
    }
    @GetMapping("/web/users/{id}/edit")
    public String editForm(@PathVariable Long id, Model m, RedirectAttributes attributes) {
        Optional<UserDTO> u = userService.findById(id);
        if (u.isEmpty()) {
            attributes.addFlashAttribute("error", "User not found.");
            return "redirect:/web/users";
        }
        addModelAttributes(m);
        m.addAttribute("user", u.get());
        return "user-add"; // reuse add form for edit
    }
    @PostMapping("/web/users/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute UserDTO formUser,
                         @RequestParam(required = false) Long tenantId,
                         @RequestParam(required = false) Long userTemplateId,
                         @RequestParam(required = false) String remarks,
                         RedirectAttributes attributes) {
        try {
            userService.update(id, formUser, tenantId, userTemplateId, remarks);
            attributes.addFlashAttribute("success", "User updated successfully.");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/users";
    }
    @GetMapping("/web/users/{id}")
    public String view(@PathVariable Long id, Model m, RedirectAttributes attributes) {
        Optional<UserDTO> u = userService.findById(id);
        if (u.isEmpty()) {
            attributes.addFlashAttribute("error", "User not found.");
            return "redirect:/web/users";
        }
        m.addAttribute("user", u.get());
        m.addAttribute("changeLogs", entityChangeLogService.getRecentChanges(USER_ENTITY, u.get().getId()));
        return "user-detail";
    }
    @PostMapping("/web/users/{id}/status")
    public String status(@PathVariable Long id,
                         @RequestParam UserStatus status,
                         @RequestParam(required = false) String remarks,
                         RedirectAttributes attributes) {
        try {
            userService.changeStatus(id, status, remarks);
            attributes.addFlashAttribute("success", "User status updated successfully.");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/users";
    }
}
