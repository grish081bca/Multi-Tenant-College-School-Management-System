package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.StudentCertificate;

import java.util.List;
import java.util.Optional;

public interface StudentCertificateRepository extends TenantScopedRepository<StudentCertificate> {
    Optional<StudentCertificate> findByTenant_IdAndEnrollment_IdAndCertificateTypeIgnoreCase(Long tenantId, Long enrollmentId, String certificateType);
    List<StudentCertificate> findAllByTenant_IdAndEnrollment_Student_IdOrderByIssuedOnDesc(Long tenantId, Long studentId);
}
