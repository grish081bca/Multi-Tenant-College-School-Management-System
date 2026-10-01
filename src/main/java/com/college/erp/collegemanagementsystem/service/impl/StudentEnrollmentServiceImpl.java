package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.StudentEnrollmentService;
import org.springframework.stereotype.Service;

@Service
public class StudentEnrollmentServiceImpl extends AbstractAcademicModuleService implements StudentEnrollmentService {
    public StudentEnrollmentServiceImpl(AcademicManagementService workflow) { super(workflow); }
    @Override protected String module() { return "student-enrollments"; }
}
