package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.SemesterService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/semesters")
public class SemesterController extends AbstractAcademicRestController {
    public SemesterController(SemesterService service) { super(service); }
}
