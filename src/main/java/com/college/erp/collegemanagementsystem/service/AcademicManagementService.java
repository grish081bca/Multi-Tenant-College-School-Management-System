package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.enums.Status;

public interface AcademicManagementService {
    PagablePage<AcademicRecordDTO> search(String module, String q, Status status, Integer page, Integer size);
    PagablePage<AcademicRecordDTO> search(String module, String q, java.util.Map<String, String> filters, Status status, Integer page, Integer size);
    PagablePage<AcademicRecordDTO> search(String module, String q, java.util.Map<String, String> filters, Status status, Integer page, Integer size, Long tenantId);
    AcademicRecordDTO get(String module, Long id);
    AcademicRecordDTO get(String module, Long id, Long tenantId);
    java.util.List<AcademicRecordDTO> options(String module);
    java.util.List<AcademicRecordDTO> options(String module, Long tenantId);
    AcademicRecordDTO save(String module, Long id, AcademicRecordDTO request, String remarks);
    AcademicRecordDTO save(String module, Long id, AcademicRecordDTO request, String remarks, Long tenantId);
    void changeStatus(String module, Long id, Status status, String remarks);
    void changeStatus(String module, Long id, Status status, String remarks, Long tenantId);
    void delete(String module, Long id);
    void delete(String module, Long id, Long tenantId);
}
