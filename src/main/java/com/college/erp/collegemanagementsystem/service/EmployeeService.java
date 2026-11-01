package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.EmployeeDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.request.EmployeeRequest;
import com.college.erp.collegemanagementsystem.enums.EmployeeType;

public interface EmployeeService {
    PagablePage<EmployeeDTO> search(String q, EmployeeType type, Integer page, Integer size, Long tenantId);

    EmployeeDTO get(Long id, Long tenantId);

    EmployeeDTO create(EmployeeRequest request, Long tenantId);

    EmployeeDTO update(Long id, EmployeeRequest request, Long tenantId, String remarks);

    EmployeeDTO changeStatus(Long id, com.college.erp.collegemanagementsystem.enums.Status status, Long tenantId, String remarks);

    void delete(Long id, Long tenantId, String remarks);
}
