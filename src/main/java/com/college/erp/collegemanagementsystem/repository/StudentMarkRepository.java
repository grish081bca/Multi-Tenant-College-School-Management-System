package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.StudentMark;
import com.college.erp.collegemanagementsystem.enums.MarkVerificationStatus;

import java.util.List;
import java.util.Optional;

public interface StudentMarkRepository extends TenantScopedRepository<StudentMark> {
    Optional<StudentMark> findByTenant_IdAndExamination_IdAndEnrollment_Id(Long tenantId, Long examinationId, Long enrollmentId);
    List<StudentMark> findAllByTenant_IdAndEnrollment_IdAndVerificationStatus(Long tenantId, Long enrollmentId, MarkVerificationStatus verificationStatus);
}
