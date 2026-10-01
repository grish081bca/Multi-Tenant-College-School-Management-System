package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.SubjectService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController extends AbstractAcademicRestController {
    public SubjectController(SubjectService service) { super(service); }
}
