package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import java.util.Map;

/** Selects the typed Spring Data repository for the shared academic CRUD service. */
@Component
public class AcademicRepositoryRegistry {
    private final Map<String, TenantScopedRepository<?>> repositories;
    private final Map<Class<?>, TenantScopedRepository<?>> repositoriesByType;
    public AcademicRepositoryRegistry(DepartmentRepository departments, FacultyRepository faculties, ProgramRepository programs,
                                     AcademicYearRepository academicYears, SemesterRepository semesters, SectionRepository sections,
                                     SubjectRepository subjects, ProgramSubjectRepository programSubjects, StudentRepository students,
                                     StudentEnrollmentRepository enrollments, StudentAttendanceRepository attendance,
                                     TimetableSlotRepository timetable, ExaminationRepository examinations) {
        repositories = Map.ofEntries(Map.entry("departments", departments), Map.entry("faculties", faculties),
                Map.entry("programs", programs), Map.entry("academic-years", academicYears),
                Map.entry("semesters", semesters), Map.entry("sections", sections), Map.entry("subjects", subjects),
                Map.entry("program-subjects", programSubjects), Map.entry("students", students),
                Map.entry("student-enrollments", enrollments), Map.entry("attendance", attendance),
                Map.entry("timetable", timetable), Map.entry("examinations", examinations));
        repositoriesByType = Map.ofEntries(Map.entry(Department.class, departments), Map.entry(Faculty.class, faculties),
                Map.entry(Program.class, programs), Map.entry(AcademicYear.class, academicYears),
                Map.entry(Semester.class, semesters), Map.entry(Section.class, sections), Map.entry(Subject.class, subjects),
                Map.entry(ProgramSubject.class, programSubjects), Map.entry(Student.class, students),
                Map.entry(StudentEnrollment.class, enrollments), Map.entry(StudentAttendance.class, attendance),
                Map.entry(TimetableSlot.class, timetable), Map.entry(Examination.class, examinations));
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object findByIdAndTenantId(String module, Long id, Long tenantId) { return ((TenantScopedRepository) repository(module)).findByIdAndTenant_Id(id, tenantId).orElse(null); }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Page<?> findAll(String module, Specification<?> specification, Pageable pageable) {
        return ((JpaSpecificationExecutor) repository(module)).findAll((Specification) specification, pageable);
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public java.util.List<?> findAll(String module, Specification<?> specification, Sort sort) {
        return ((JpaSpecificationExecutor) repository(module)).findAll((Specification) specification, sort);
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object findByTypeAndTenantId(Class<?> type, Long id, Long tenantId) {
        TenantScopedRepository repository = repositoriesByType.get(type);
        return repository == null ? null : repository.findByIdAndTenant_Id(id, tenantId).orElse(null);
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object save(String module, Object entity) { return ((JpaRepository) repository(module)).save(entity); }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void delete(String module, Object entity) { ((JpaRepository) repository(module)).delete(entity); }
    private TenantScopedRepository<?> repository(String module) {
        TenantScopedRepository<?> repository = repositories.get(module);
        if (repository == null) throw new IllegalArgumentException("Unknown academic module.");
        return repository;
    }
}
