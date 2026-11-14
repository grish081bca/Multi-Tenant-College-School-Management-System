package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.TenantService;
import com.college.erp.collegemanagementsystem.service.TimetableService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/timetable")
public class TimetableWebController extends AbstractAcademicWebController {
    public TimetableWebController(TimetableService service, AcademicManagementService workflow, EntityChangeLogService logs, TenantService tenants) { super(service, workflow, logs, tenants); }
    @Override protected String module() { return "timetable"; }
}
