package com.college.erp.collegemanagementsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity @Getter @Setter
@Table(name = "timetable_slots", indexes = {
        @Index(name = "idx_timetable_tenant_section_day", columnList = "tenant_id, section_id, day_of_week"),
        @Index(name = "idx_timetable_tenant_teacher_day", columnList = "tenant_id, teacher_id, day_of_week")
})
public class TimetableSlot extends TenantOwnedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "section_id", nullable = false)
    private Section section;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "program_subject_id", nullable = false)
    private ProgramSubject programSubject;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "teacher_id")
    private Employee teacher;
    @Enumerated(EnumType.STRING) @Column(name = "day_of_week", nullable = false, length = 12)
    private DayOfWeek dayOfWeek;
    @Column(name = "start_time", nullable = false) private LocalTime startTime;
    @Column(name = "end_time", nullable = false) private LocalTime endTime;
    @Column(name = "room", length = 100) private String room;
}
