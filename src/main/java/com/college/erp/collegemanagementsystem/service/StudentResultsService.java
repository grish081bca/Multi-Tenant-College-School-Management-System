package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.StudentResultDTO;

import java.util.List;

public interface StudentResultsService {
    PagablePage<StudentResultDTO> search(String q, Integer page, Integer size, Long tenantId);
    StudentResultDTO get(Long id, Long tenantId);
    List<AcademicRecordDTO> enrollmentOptions(Long tenantId);
    StudentResultDTO process(Long enrollmentId, Long tenantId);
}
