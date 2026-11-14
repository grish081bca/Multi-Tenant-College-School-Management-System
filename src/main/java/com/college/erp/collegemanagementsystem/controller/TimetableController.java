package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.service.TimetableService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/timetable")
public class TimetableController extends AbstractAcademicRestController {
    public TimetableController(TimetableService service) { super(service); }
}
