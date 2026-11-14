package com.college.erp.collegemanagementsystem.repository;
import com.college.erp.collegemanagementsystem.entity.StudentEnrollment;
import com.college.erp.collegemanagementsystem.enums.Status;

import java.util.List;

public interface StudentEnrollmentRepository extends TenantScopedRepository<StudentEnrollment> {
    List<StudentEnrollment> findAllByTenant_IdAndStudent_IdAndStatusOrderByIdDesc(Long tenantId, Long studentId, Status status);
}
