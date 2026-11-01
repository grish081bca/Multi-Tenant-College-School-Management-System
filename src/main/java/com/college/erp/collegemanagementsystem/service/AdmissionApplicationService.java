package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.AdmissionApplicationDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.request.AdmissionApplicationRequest;
import com.college.erp.collegemanagementsystem.enums.AdmissionStatus;

public interface AdmissionApplicationService {
    PagablePage<AdmissionApplicationDTO> search(String q, AdmissionStatus status, Integer page, Integer size, Long tenantId);

    AdmissionApplicationDTO get(Long id, Long tenantId);

    AdmissionApplicationDTO create(AdmissionApplicationRequest request, Long tenantId);

    AdmissionApplicationDTO update(Long id, AdmissionApplicationRequest request, Long tenantId, String remarks);

    AdmissionApplicationDTO transition(Long id, AdmissionStatus status, Long tenantId, String remarks);

    void delete(Long id, Long tenantId, String remarks);
}
