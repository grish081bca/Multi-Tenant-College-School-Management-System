package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.ExaminationService;
import org.springframework.stereotype.Service;

@Service
public class ExaminationServiceImpl extends AbstractAcademicModuleService implements ExaminationService {
    public ExaminationServiceImpl(AcademicManagementService workflow) { super(workflow); }
    @Override protected String module() { return "examinations"; }
}
