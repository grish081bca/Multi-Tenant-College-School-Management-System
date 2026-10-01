package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.SectionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/sections")
public class SectionWebController extends AbstractAcademicWebController {
    public SectionWebController(SectionService service, AcademicManagementService workflow, EntityChangeLogService changeLogs) { super(service, workflow, changeLogs); }
    @Override protected String module() { return "sections"; }
}
