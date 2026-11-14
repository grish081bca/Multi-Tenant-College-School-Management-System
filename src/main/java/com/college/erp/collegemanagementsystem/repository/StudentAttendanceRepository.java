package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.StudentAttendance;
import com.college.erp.collegemanagementsystem.enums.Status;

import java.util.List;

public interface StudentAttendanceRepository extends TenantScopedRepository<StudentAttendance> {
    List<StudentAttendance> findAllByTenant_IdAndEnrollment_Student_IdAndStatusOrderByAttendanceDateDesc(
            Long tenantId, Long studentId, Status status);
}
