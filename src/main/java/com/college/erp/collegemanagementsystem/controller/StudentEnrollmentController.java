package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.StudentEnrollmentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student-enrollments")
public class StudentEnrollmentController extends AbstractAcademicRestController {
    public StudentEnrollmentController(StudentEnrollmentService service) { super(service); }
}
