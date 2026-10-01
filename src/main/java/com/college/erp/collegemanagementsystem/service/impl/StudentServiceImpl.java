package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.StudentService;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl extends AbstractAcademicModuleService implements StudentService {
    public StudentServiceImpl(AcademicManagementService workflow) { super(workflow); }
    @Override protected String module() { return "students"; }
}
