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
                                     StudentEnrollmentRepository enrollments) {
        repositories = Map.of("departments", departments, "faculties", faculties, "programs", programs,
                "academic-years", academicYears, "semesters", semesters, "sections", sections, "subjects", subjects,
                "program-subjects", programSubjects, "students", students, "student-enrollments", enrollments);
        repositoriesByType = Map.of(Department.class, departments, Faculty.class, faculties, Program.class, programs,
                AcademicYear.class, academicYears, Semester.class, semesters, Section.class, sections,
                Subject.class, subjects, ProgramSubject.class, programSubjects, Student.class, students,
                StudentEnrollment.class, enrollments);
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
