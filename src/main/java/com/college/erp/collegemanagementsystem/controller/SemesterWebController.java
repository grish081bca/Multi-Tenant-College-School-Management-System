package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.SemesterService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/semesters")
public class SemesterWebController extends AbstractAcademicWebController {
    public SemesterWebController(SemesterService service, AcademicManagementService workflow, EntityChangeLogService changeLogs) { super(service, workflow, changeLogs); }
    @Override protected String module() { return "semesters"; }
}
