package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.DashboardDTO;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author grish
 *
 */
@Controller
public class WebDashboardController {
    private final DashboardService dashboardService;

    public WebDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/web/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal) {
            DashboardDTO dashboard = dashboardService.getDashboard(
                    principal.getUserType(), principal.getTenantId(), principal.getFullName());
            model.addAttribute("dashboard", dashboard);
        }
        return "dashboard";
    }
}
