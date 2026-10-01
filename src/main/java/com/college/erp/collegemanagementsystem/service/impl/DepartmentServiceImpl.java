package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.DepartmentService;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl extends AbstractAcademicModuleService implements DepartmentService {
    public DepartmentServiceImpl(AcademicManagementService workflow) { super(workflow); }
    @Override protected String module() { return "departments"; }
}
