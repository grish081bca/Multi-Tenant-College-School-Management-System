package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.TenantService;
import com.college.erp.collegemanagementsystem.service.StudentEnrollmentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/student-enrollments")
public class StudentEnrollmentWebController extends AbstractAcademicWebController {
    public StudentEnrollmentWebController(StudentEnrollmentService service, AcademicManagementService workflow, EntityChangeLogService changeLogs, TenantService tenants) { super(service, workflow, changeLogs, tenants); }
    @Override protected String module() { return "student-enrollments"; }
}
