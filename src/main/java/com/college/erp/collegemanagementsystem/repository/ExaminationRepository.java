package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.Examination;
import com.college.erp.collegemanagementsystem.enums.Status;

import java.util.List;

public interface ExaminationRepository extends TenantScopedRepository<Examination> {
    List<Examination> findAllByTenant_IdAndSection_IdAndStatusOrderByExamDateAscStartTimeAsc(Long tenantId, Long sectionId, Status status);
}
