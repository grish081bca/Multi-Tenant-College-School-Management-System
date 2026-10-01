package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.StudentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController extends AbstractAcademicRestController {
    public StudentController(StudentService service) { super(service); }
}
