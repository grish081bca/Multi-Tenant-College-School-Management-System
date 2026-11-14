package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.StudentAttendanceService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController extends AbstractAcademicRestController {
    public AttendanceController(StudentAttendanceService service) { super(service); }
}
