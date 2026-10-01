package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.ProgramService;
import org.springframework.stereotype.Service;

@Service
public class ProgramServiceImpl extends AbstractAcademicModuleService implements ProgramService {
    public ProgramServiceImpl(AcademicManagementService workflow) { super(workflow); }
    @Override protected String module() { return "programs"; }
}
