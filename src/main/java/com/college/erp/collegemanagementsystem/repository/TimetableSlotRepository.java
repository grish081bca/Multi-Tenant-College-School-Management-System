package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.TimetableSlot;
import com.college.erp.collegemanagementsystem.enums.Status;

import java.util.List;

public interface TimetableSlotRepository extends TenantScopedRepository<TimetableSlot> {
    List<TimetableSlot> findAllByTenant_IdAndTeacher_IdAndStatusOrderByDayOfWeekAscStartTimeAsc(Long tenantId, Long teacherId, Status status);
}
