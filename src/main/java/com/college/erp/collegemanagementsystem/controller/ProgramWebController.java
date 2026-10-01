package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.ProgramService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/programs")
public class ProgramWebController extends AbstractAcademicWebController {
    public ProgramWebController(ProgramService service, AcademicManagementService workflow, EntityChangeLogService changeLogs) { super(service, workflow, changeLogs); }
    @Override protected String module() { return "programs"; }
}
