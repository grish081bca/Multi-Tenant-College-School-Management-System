package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.TimetableService;
import org.springframework.stereotype.Service;

@Service
public class TimetableServiceImpl extends AbstractAcademicModuleService implements TimetableService {
    public TimetableServiceImpl(AcademicManagementService workflow) { super(workflow); }
    @Override protected String module() { return "timetable"; }
}
