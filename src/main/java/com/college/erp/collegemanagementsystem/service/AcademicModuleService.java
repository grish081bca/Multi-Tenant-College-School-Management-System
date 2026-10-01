package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.enums.Status;

import java.util.List;
import java.util.Map;

/** Module-scoped service contract used by individual academic controllers. */
public interface AcademicModuleService {
    PagablePage<AcademicRecordDTO> search(String q, Map<String, String> filters, Status status, Integer page, Integer size);
    PagablePage<AcademicRecordDTO> search(String q, Map<String, String> filters, Status status, Integer page, Integer size, Long tenantId);
    AcademicRecordDTO get(Long id);
    AcademicRecordDTO get(Long id, Long tenantId);
    List<AcademicRecordDTO> options();
    List<AcademicRecordDTO> options(Long tenantId);
    AcademicRecordDTO save(Long id, AcademicRecordDTO request, String remarks);
    AcademicRecordDTO save(Long id, AcademicRecordDTO request, String remarks, Long tenantId);
    void changeStatus(Long id, Status status, String remarks);
    void changeStatus(Long id, Status status, String remarks, Long tenantId);
    void delete(Long id);
    void delete(Long id, Long tenantId);
}
