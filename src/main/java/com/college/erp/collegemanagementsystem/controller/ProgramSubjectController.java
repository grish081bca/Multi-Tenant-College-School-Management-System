package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.ProgramSubjectService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/program-subjects")
public class ProgramSubjectController extends AbstractAcademicRestController {
    public ProgramSubjectController(ProgramSubjectService service) { super(service); }
}
