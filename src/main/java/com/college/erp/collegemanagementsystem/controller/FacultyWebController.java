package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.FacultyService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/faculties")
public class FacultyWebController extends AbstractAcademicWebController {
    public FacultyWebController(FacultyService service, AcademicManagementService workflow, EntityChangeLogService changeLogs) { super(service, workflow, changeLogs); }
    @Override protected String module() { return "faculties"; }
}
