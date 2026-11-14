package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.StudentAttendanceService;
import com.college.erp.collegemanagementsystem.service.TenantService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/attendance")
public class AttendanceWebController extends AbstractAcademicWebController {
    public AttendanceWebController(StudentAttendanceService service, AcademicManagementService workflow, EntityChangeLogService logs, TenantService tenants) { super(service, workflow, logs, tenants); }
    @Override protected String module() { return "attendance"; }
}
