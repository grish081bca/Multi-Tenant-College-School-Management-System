package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.ExaminationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/examinations")
public class ExaminationController extends AbstractAcademicRestController {
    public ExaminationController(ExaminationService service) { super(service); }
}
