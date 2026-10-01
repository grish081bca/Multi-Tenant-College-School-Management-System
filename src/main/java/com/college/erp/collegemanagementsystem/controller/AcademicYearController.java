package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.AcademicYearService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/academic-years")
public class AcademicYearController extends AbstractAcademicRestController {
    public AcademicYearController(AcademicYearService service) { super(service); }
}
