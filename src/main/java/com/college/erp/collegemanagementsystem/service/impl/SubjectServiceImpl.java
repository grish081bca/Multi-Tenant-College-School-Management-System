package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.SubjectService;
import org.springframework.stereotype.Service;

@Service
public class SubjectServiceImpl extends AbstractAcademicModuleService implements SubjectService {
    public SubjectServiceImpl(AcademicManagementService workflow) { super(workflow); }
    @Override protected String module() { return "subjects"; }
}
