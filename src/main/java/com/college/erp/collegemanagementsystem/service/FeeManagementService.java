package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.FeeCollectionDTO;
import com.college.erp.collegemanagementsystem.dto.FeeStructureDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.request.FeeCollectionRequest;
import com.college.erp.collegemanagementsystem.dto.request.FeeStructureRequest;
import com.college.erp.collegemanagementsystem.enums.Status;

public interface FeeManagementService {
    PagablePage<FeeStructureDTO> searchStructures(String q, Status status, Integer page, Integer size, Long tenantId);
    FeeStructureDTO getStructure(Long id, Long tenantId);
    FeeStructureDTO createStructure(FeeStructureRequest request, Long tenantId);
    FeeStructureDTO updateStructure(Long id, FeeStructureRequest request, Long tenantId, String remarks);
    FeeStructureDTO changeStructureStatus(Long id, Status status, Long tenantId, String remarks);
    PagablePage<FeeCollectionDTO> searchCollections(String q, Integer page, Integer size, Long tenantId);
    FeeCollectionDTO getCollection(Long id, Long tenantId);
    FeeCollectionDTO getReceipt(String receiptNumber, Long tenantId);
    FeeCollectionDTO collect(FeeCollectionRequest request, Long tenantId);
}
