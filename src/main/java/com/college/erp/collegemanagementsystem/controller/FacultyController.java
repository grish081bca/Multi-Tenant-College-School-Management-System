package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.FacultyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/faculties")
public class FacultyController extends AbstractAcademicRestController {
    public FacultyController(FacultyService service) { super(service); }
}
