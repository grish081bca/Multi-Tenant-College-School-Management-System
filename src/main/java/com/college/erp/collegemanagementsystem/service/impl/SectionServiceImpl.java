package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.SectionService;
import org.springframework.stereotype.Service;

@Service
public class SectionServiceImpl extends AbstractAcademicModuleService implements SectionService {
    public SectionServiceImpl(AcademicManagementService workflow) { super(workflow); }
    @Override protected String module() { return "sections"; }
}
