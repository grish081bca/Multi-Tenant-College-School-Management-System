package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.enums.Status;

import java.util.List;
import java.util.Map;

/** Module-scoped service contract used by individual academic controllers. */
public interface AcademicModuleService {
    PagablePage<AcademicRecordDTO> search(String q, Map<String, String> filters, Status status, Integer page, Integer size);
    AcademicRecordDTO get(Long id);
    List<AcademicRecordDTO> options();
    AcademicRecordDTO save(Long id, AcademicRecordDTO request, String remarks);
    void changeStatus(Long id, Status status, String remarks);
    void delete(Long id);
}
