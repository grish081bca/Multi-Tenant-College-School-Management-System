package com.college.erp.collegemanagementsystem.entity;

import com.college.erp.collegemanagementsystem.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity @Getter @Setter
@Table(name = "student_attendance", uniqueConstraints = @UniqueConstraint(name = "uk_attendance_tenant_enrollment_date", columnNames = {"tenant_id", "enrollment_id", "attendance_date"}), indexes = {
        @Index(name = "idx_attendance_tenant_date", columnList = "tenant_id, attendance_date"),
        @Index(name = "idx_attendance_tenant_enrollment", columnList = "tenant_id, enrollment_id")
})
public class StudentAttendance extends TenantOwnedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "enrollment_id", nullable = false)
    private StudentEnrollment enrollment;
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;
    @Enumerated(EnumType.STRING) @Column(name = "attendance_status", nullable = false, length = 20)
    private AttendanceStatus attendanceStatus;
}
