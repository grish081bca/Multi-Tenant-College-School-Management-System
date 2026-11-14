package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.StudentPortalDTO;
import com.college.erp.collegemanagementsystem.dto.TeacherPortalDTO;

public interface PortalService {
    StudentPortalDTO studentPortal();
    TeacherPortalDTO teacherPortal();
}
