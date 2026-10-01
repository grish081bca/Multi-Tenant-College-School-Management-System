package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.enums.Status;

public interface AcademicManagementService {
    PagablePage<AcademicRecordDTO> search(String module, String q, Status status, Integer page, Integer size);
    AcademicRecordDTO get(String module, Long id);
    java.util.List<AcademicRecordDTO> options(String module);
    AcademicRecordDTO save(String module, Long id, AcademicRecordDTO request, String remarks);
    void changeStatus(String module, Long id, Status status, String remarks);
    void delete(String module, Long id);
}
