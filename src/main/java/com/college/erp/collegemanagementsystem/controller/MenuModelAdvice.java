package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.entity.User;
import com.college.erp.collegemanagementsystem.repository.UserRepository;
import com.college.erp.collegemanagementsystem.service.MenuTemplateService;
import com.college.erp.collegemanagementsystem.dto.MenuDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.OffsetDateTime;
import java.util.List;
import com.college.erp.collegemanagementsystem.util.DateFormatUtils;

/**
 * @author grish
 *
 */
@ControllerAdvice
public class MenuModelAdvice {

    private final MenuTemplateService menuTemplateService;
    private final UserRepository userRepository;

    public MenuModelAdvice(MenuTemplateService menuTemplateService, UserRepository userRepository) {
        this.menuTemplateService = menuTemplateService;
        this.userRepository = userRepository;
    }
    @ModelAttribute
    public void addMenus(Model model, Authentication authentication, HttpServletRequest request) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)) {
            model.addAttribute("allowedMenus", List.of());
            return;
        }
        List<MenuDTO> allowedMenus = menuTemplateService.findMenusForUserTemplate(
                principal.getUserTemplateId(),
                principal.getUserType());
        model.addAttribute("allowedMenus", allowedMenus);
        model.addAttribute("currentPageName", resolveCurrentPageName(request, allowedMenus));
        model.addAttribute("loggedInUserType", principal.getUserType());
        model.addAttribute("currentUsername", principal.getUsername());
        model.addAttribute("currentUserFullName", principal.getFullName());
        model.addAttribute("currentUserInitial", initial(principal.getFullName(), principal.getUsername()));
        model.addAttribute("currentTenantCode", principal.getTenantCode());
        model.addAttribute("currentLastLoginAt", DateFormatUtils.formatDateTime(principal.getLastLoginAt()));
        model.addAttribute("currentLastLoginAtIso", formatIsoDateTime(principal.getLastLoginAt()));
        model.addAttribute("currentPasswordChangedAt", DateFormatUtils.formatDateTime(principal.getPasswordChangedAt()));
        userRepository.findById(principal.getUserId()).ifPresent(user -> addFreshUserAttributes(model, user));
    }

    private void addFreshUserAttributes(Model model, User user) {
        model.addAttribute("currentUsername", user.getUsername());
        String fullName = buildFullName(user);
        model.addAttribute("currentUserFullName", fullName);
        model.addAttribute("currentUserInitial", initial(fullName, user.getUsername()));
        model.addAttribute("currentLastLoginAt", DateFormatUtils.formatDateTime(user.getLastLoginAt()));
        model.addAttribute("currentLastLoginAtIso", formatIsoDateTime(user.getLastLoginAt()));
        model.addAttribute("currentPasswordChangedAt", DateFormatUtils.formatDateTime(user.getPasswordChangedAt()));
    }

    private String formatIsoDateTime(OffsetDateTime dateTime) {
        return dateTime == null ? null : dateTime.toInstant().toString();
    }

    private String buildFullName(User user) {
        StringBuilder builder = new StringBuilder();
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            builder.append(user.getFirstName().trim());
        }
        if (user.getMiddleName() != null && !user.getMiddleName().isBlank()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(user.getMiddleName().trim());
        }
        if (user.getLastName() != null && !user.getLastName().isBlank()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(user.getLastName().trim());
        }
        return builder.length() == 0 ? user.getUsername() : builder.toString();
    }

    private String initial(String name, String fallback) {
        String source = name != null && !name.isBlank() ? name : fallback;
        return source == null || source.isBlank() ? "U" : source.trim().substring(0, 1).toUpperCase();
    }

    private String resolveCurrentPageName(HttpServletRequest request, List<MenuDTO> allowedMenus) {
        String uri = request.getRequestURI();
        for (MenuDTO menu : allowedMenus) {
            if (menu.getMenuUrl() != null && uri.equals(request.getContextPath() + menu.getMenuUrl())) {
                            return menu.getName();
            }
        }
        if (uri.endsWith("/dashboard")) {
            return "Dashboard";
        }
        if (uri.endsWith("/my-profile")) {
            return "My profile";
        }
        if (uri.endsWith("/change-password")) {
            return "Change password";
        }
        if (uri.endsWith("/reset-password")) {
            return "Reset password";
        }
        return "College ERP";
    }
}
