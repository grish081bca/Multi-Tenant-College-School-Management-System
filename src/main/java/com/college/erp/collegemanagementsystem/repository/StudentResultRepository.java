package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.StudentResult;
import com.college.erp.collegemanagementsystem.enums.Status;

import java.util.Optional;
import java.util.List;

public interface StudentResultRepository extends TenantScopedRepository<StudentResult> {
    Optional<StudentResult> findByTenant_IdAndEnrollment_Id(Long tenantId, Long enrollmentId);
    List<StudentResult> findAllByTenant_IdAndEnrollment_Student_IdAndStatusOrderByProcessedAtDesc(Long tenantId, Long studentId, Status status);
}
