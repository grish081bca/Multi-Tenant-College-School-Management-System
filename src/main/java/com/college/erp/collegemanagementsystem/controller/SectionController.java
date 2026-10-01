package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.SectionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sections")
public class SectionController extends AbstractAcademicRestController {
    public SectionController(SectionService service) { super(service); }
}
