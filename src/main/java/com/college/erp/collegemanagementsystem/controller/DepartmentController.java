package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.DepartmentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController extends AbstractAcademicRestController {
    public DepartmentController(DepartmentService service) { super(service); }
}
