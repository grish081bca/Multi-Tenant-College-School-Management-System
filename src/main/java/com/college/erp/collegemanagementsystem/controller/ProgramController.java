package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.ProgramService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/programs")
public class ProgramController extends AbstractAcademicRestController {
    public ProgramController(ProgramService service) { super(service); }
}
