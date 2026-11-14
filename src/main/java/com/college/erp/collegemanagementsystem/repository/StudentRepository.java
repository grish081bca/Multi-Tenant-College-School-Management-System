package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.Student;
import com.college.erp.collegemanagementsystem.enums.Status;

import java.util.Optional;

public interface StudentRepository extends TenantScopedRepository<Student> {
    boolean existsByTenant_IdAndEmailIgnoreCase(Long tenantId, String email);
    Optional<Student> findByTenant_IdAndEmailIgnoreCaseAndStatus(Long tenantId, String email, Status status);
}
