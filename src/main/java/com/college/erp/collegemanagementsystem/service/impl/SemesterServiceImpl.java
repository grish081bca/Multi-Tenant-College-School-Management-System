package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.SemesterService;
import org.springframework.stereotype.Service;

@Service
public class SemesterServiceImpl extends AbstractAcademicModuleService implements SemesterService {
    public SemesterServiceImpl(AcademicManagementService workflow) { super(workflow); }
    @Override protected String module() { return "semesters"; }
}
