package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.TenantService;
import com.college.erp.collegemanagementsystem.service.SubjectService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/subjects")
public class SubjectWebController extends AbstractAcademicWebController {
    public SubjectWebController(SubjectService service, AcademicManagementService workflow, EntityChangeLogService changeLogs, TenantService tenants) { super(service, workflow, changeLogs, tenants); }
    @Override protected String module() { return "subjects"; }
}
