package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.StudentAttendanceService;
import org.springframework.stereotype.Service;

@Service
public class StudentAttendanceServiceImpl extends AbstractAcademicModuleService implements StudentAttendanceService {
    public StudentAttendanceServiceImpl(AcademicManagementService workflow) { super(workflow); }
    @Override protected String module() { return "attendance"; }
}
