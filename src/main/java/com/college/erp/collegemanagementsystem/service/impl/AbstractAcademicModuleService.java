package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.AcademicModuleService;

import java.util.List;
import java.util.Map;

/** Shared tenant-safe workflow adapter; each public module has its own typed service bean. */
public abstract class AbstractAcademicModuleService implements AcademicModuleService {
    private final AcademicManagementService workflow;

    protected AbstractAcademicModuleService(AcademicManagementService workflow) { this.workflow = workflow; }
    protected abstract String module();

    @Override public PagablePage<AcademicRecordDTO> search(String q, Map<String, String> filters, Status status, Integer page, Integer size) { return workflow.search(module(), q, filters, status, page, size); }
    @Override public AcademicRecordDTO get(Long id) { return workflow.get(module(), id); }
    @Override public List<AcademicRecordDTO> options() { return workflow.options(module()); }
    @Override public AcademicRecordDTO save(Long id, AcademicRecordDTO request, String remarks) { return workflow.save(module(), id, request, remarks); }
    @Override public void changeStatus(Long id, Status status, String remarks) { workflow.changeStatus(module(), id, status, remarks); }
    @Override public void delete(Long id) { workflow.delete(module(), id); }
}
