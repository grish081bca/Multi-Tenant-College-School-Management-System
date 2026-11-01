package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.Student;

public interface StudentRepository extends TenantScopedRepository<Student> {
    boolean existsByTenant_IdAndEmailIgnoreCase(Long tenantId, String email);
}
