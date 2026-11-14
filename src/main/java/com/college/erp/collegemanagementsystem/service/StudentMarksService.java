package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.dto.MarksEntryDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.request.MarksEntryRequest;
import com.college.erp.collegemanagementsystem.enums.MarkVerificationStatus;

import java.util.List;

public interface StudentMarksService {
    PagablePage<MarksEntryDTO> search(String q, MarkVerificationStatus verificationStatus, Integer page, Integer size, Long tenantId);
    MarksEntryDTO get(Long id, Long tenantId);
    List<AcademicRecordDTO> enrollmentOptions(Long tenantId);
    List<AcademicRecordDTO> examinationOptions(Long tenantId);
    MarksEntryDTO create(MarksEntryRequest request, Long tenantId);
    MarksEntryDTO update(Long id, MarksEntryRequest request, Long tenantId, String remarks);
    MarksEntryDTO verify(Long id, Long tenantId, String remarks);
    MarksEntryDTO reject(Long id, Long tenantId, String remarks);
}
