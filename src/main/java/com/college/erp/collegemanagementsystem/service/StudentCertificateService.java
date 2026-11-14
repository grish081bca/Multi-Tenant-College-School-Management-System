package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.StudentCertificateDTO;
import com.college.erp.collegemanagementsystem.dto.request.StudentCertificateRequest;
import com.college.erp.collegemanagementsystem.enums.Status;

public interface StudentCertificateService {
    PagablePage<StudentCertificateDTO> search(String q, Status status, Integer page, Integer size, Long tenantId);
    StudentCertificateDTO get(Long id, Long tenantId);
    StudentCertificateDTO issue(StudentCertificateRequest request, Long tenantId);
    StudentCertificateDTO revoke(Long id, String reason, Long tenantId);
}
