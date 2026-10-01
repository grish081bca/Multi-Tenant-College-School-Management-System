package com.college.erp.collegemanagementsystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "program_subjects", uniqueConstraints = @UniqueConstraint(name = "uk_program_subject_tenant_program_subject_semester", columnNames = {"tenant_id", "program_id", "subject_id", "semester_id"}), indexes = @Index(name = "idx_program_subject_program", columnList = "tenant_id, program_id"))
public class ProgramSubject extends TenantOwnedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "program_id", nullable = false)
    private Program program;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;
    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }
}
