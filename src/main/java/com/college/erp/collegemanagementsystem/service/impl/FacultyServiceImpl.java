package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.FacultyService;
import org.springframework.stereotype.Service;

@Service
public class FacultyServiceImpl extends AbstractAcademicModuleService implements FacultyService {
    public FacultyServiceImpl(AcademicManagementService workflow) { super(workflow); }
    @Override protected String module() { return "faculties"; }
}
