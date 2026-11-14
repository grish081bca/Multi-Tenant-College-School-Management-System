package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.StudentResult;

import java.util.Optional;

public interface StudentResultRepository extends TenantScopedRepository<StudentResult> {
    Optional<StudentResult> findByTenant_IdAndEnrollment_Id(Long tenantId, Long enrollmentId);
}
