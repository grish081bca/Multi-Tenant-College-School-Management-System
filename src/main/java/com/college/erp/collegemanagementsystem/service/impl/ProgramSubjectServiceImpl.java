package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.ProgramSubjectService;
import org.springframework.stereotype.Service;

@Service
public class ProgramSubjectServiceImpl extends AbstractAcademicModuleService implements ProgramSubjectService {
    public ProgramSubjectServiceImpl(AcademicManagementService workflow) { super(workflow); }
    @Override protected String module() { return "program-subjects"; }
}
