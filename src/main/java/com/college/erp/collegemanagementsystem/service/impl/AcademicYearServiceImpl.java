package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.AcademicYearService;
import org.springframework.stereotype.Service;

@Service
public class AcademicYearServiceImpl extends AbstractAcademicModuleService implements AcademicYearService {
    public AcademicYearServiceImpl(AcademicManagementService workflow) { super(workflow); }
    @Override protected String module() { return "academic-years"; }
}
