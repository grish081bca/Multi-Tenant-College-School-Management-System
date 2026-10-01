package com.college.erp.collegemanagementsystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sections", uniqueConstraints = {
        @UniqueConstraint(name = "uk_section_tenant_code", columnNames = {"tenant_id", "code"}),
        @UniqueConstraint(name = "uk_section_tenant_program_year_sem_name", columnNames = {"tenant_id", "program_id", "academic_year_id", "semester_id", "name"})
}, indexes = @Index(name = "idx_section_tenant_program", columnList = "tenant_id, program_id"))
public class Section extends TenantOwnedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "program_id", nullable = false)
    private Program program;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;
    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }
    public AcademicYear getAcademicYear() { return academicYear; }
    public void setAcademicYear(AcademicYear academicYear) { this.academicYear = academicYear; }
    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }
}
