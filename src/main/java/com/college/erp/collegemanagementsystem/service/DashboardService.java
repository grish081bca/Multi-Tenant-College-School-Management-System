package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.DashboardDTO;
import com.college.erp.collegemanagementsystem.enums.UserType;

public interface DashboardService {
    DashboardDTO getDashboard(UserType userType, Long tenantId, String displayName);
}
