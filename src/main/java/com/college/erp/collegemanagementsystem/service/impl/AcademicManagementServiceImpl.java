package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.entity.*;
import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.repository.AcademicRepositoryRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/** Tenant-scoped application service for the academic and student modules. */
@Service
@Transactional
public class AcademicManagementServiceImpl implements AcademicManagementService {
    private final EntityManager entityManager;
    private final EntityChangeLogService changeLogService;
    private final AcademicRepositoryRegistry repositories;

    public AcademicManagementServiceImpl(EntityManager entityManager, EntityChangeLogService changeLogService, AcademicRepositoryRegistry repositories) {
        this.entityManager = entityManager;
        this.changeLogService = changeLogService;
        this.repositories = repositories;
    }

    @Override @Transactional(readOnly = true)
    public PagablePage<AcademicRecordDTO> search(String module, String q, Status status, Integer page, Integer size) {
        return search(module, q, Map.of(), status, page, size);
    }

    @Override @Transactional(readOnly = true)
    public PagablePage<AcademicRecordDTO> search(String module, String q, Map<String, String> filters, Status status, Integer page, Integer size) {
        String moduleKey = normalizeModule(module);
        long currentTenantId = tenantId();
        Specification<Object> specification = (root, query, builder) -> {
            Predicate predicate = builder.equal(root.get("tenant").get("id"), currentTenantId);
            if (status != null) predicate = builder.and(predicate, builder.equal(root.get("status"), status));
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                List<Predicate> searches = new ArrayList<>();
                for (String field : searchFields(moduleKey)) searches.add(builder.like(builder.lower(searchPath(root, field).as(String.class)), term));
                predicate = builder.and(predicate, builder.or(searches.toArray(Predicate[]::new)));
            }
            if (filters != null) {
                Map<String, String> allowedFilters = searchFilters(moduleKey);
                for (Map.Entry<String, String> filter : filters.entrySet()) {
                    String field = allowedFilters.get(filter.getKey());
                    if (field == null || filter.getValue() == null || filter.getValue().isBlank()) continue;
                    String term = "%" + filter.getValue().trim().toLowerCase(Locale.ROOT) + "%";
                    predicate = builder.and(predicate, builder.like(builder.lower(searchPath(root, field).as(String.class)), term));
                }
            }
            if (query.getResultType() != Long.class && query.getResultType() != long.class) fetchAssociations(root, moduleKey);
            return predicate;
        };
        int currentPage = PagablePage.normalizePage(page), pageSize = PagablePage.normalizeSize(size);
        Page<?> results = repositories.findAll(moduleKey, specification, PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.DESC, "id")));
        return PagablePage.from(results.map(row -> toDto(moduleKey, row)));
    }

    private Map<String, String> searchFilters(String module) {
        return switch (module) {
            case "departments" -> Map.of("code", "code", "name", "name", "faculty", "faculty.name", "description", "description");
            case "faculties" -> Map.of("code", "code", "name", "name", "description", "description");
            case "programs" -> Map.of("code", "code", "name", "name", "faculty", "faculty.name", "department", "department.name", "description", "description");
            case "academic-years" -> Map.of("code", "code", "name", "name", "description", "description");
            case "semesters" -> Map.of("code", "code", "name", "name", "sequenceNumber", "sequenceNumber", "description", "description");
            case "sections" -> Map.of("code", "code", "name", "name", "program", "program.name", "academicYear", "academicYear.name", "semester", "semester.name", "description", "description");
            case "subjects" -> Map.of("code", "code", "name", "name", "creditHours", "creditHours", "description", "description");
            case "program-subjects" -> Map.of("program", "program.name", "subjectCode", "subject.code", "subject", "subject.name", "semester", "semester.name");
            case "students" -> Map.of("registrationNumber", "registrationNumber", "firstName", "firstName", "middleName", "middleName", "lastName", "lastName", "email", "email", "phone", "phone", "address", "address");
            case "student-enrollments" -> Map.of("student", "student.registrationNumber", "program", "program.name", "academicYear", "academicYear.name", "semester", "semester.name", "section", "section.name");
            default -> Map.of();
        };
    }

    @Override @Transactional(readOnly = true)
    public AcademicRecordDTO get(String module, Long id) { return toDto(module, scopedEntity(module, id)); }

    @Override @Transactional(readOnly = true)
    public List<AcademicRecordDTO> options(String module) {
        String moduleKey = normalizeModule(module); long currentTenantId = tenantId();
        Specification<Object> specification = (root, query, builder) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) fetchAssociations(root, moduleKey);
            return builder.equal(root.get("tenant").get("id"), currentTenantId);
        };
        return repositories.findAll(moduleKey, specification, Sort.by(Sort.Direction.ASC, "id")).stream().map(row -> toDto(moduleKey, row)).toList();
    }

    @Override
    public AcademicRecordDTO save(String module, Long id, AcademicRecordDTO request, String remarks) {
        if (request == null) throw new ValidationException("Form data is required.");
        Object entity = id == null ? newEntity(module) : scopedEntity(module, id);
        AcademicRecordDTO before = id == null ? null : toDto(module, entity);
        Status oldStatus = before == null ? null : before.getStatus();
        Status targetStatus = request.getStatus() != null ? request.getStatus() : (oldStatus != null ? oldStatus : Status.ACTIVE);
        Tenant tenant = entityManager.getReference(Tenant.class, tenantId());
        if (entity instanceof ProgramSubject ps) {
            if (id == null) ps.setTenant(tenant);
            ps.setStatus(targetStatus);
        } else if (entity instanceof TenantOwnedEntity owned) {
            if (id == null) owned.setTenant(tenant);
            owned.setCode(required(request.getCode(), "Code"));
            owned.setName(required(request.getName(), "Name"));
            owned.setDescription(clean(request.getDescription()));
            owned.setStatus(targetStatus);
        } else if (entity instanceof Student student) {
            if (id == null) student.setTenant(tenant);
            student.setRegistrationNumber(required(request.getRegistrationNumber(), "Registration number"));
            student.setFirstName(required(request.getFirstName(), "First name"));
            student.setMiddleName(clean(request.getMiddleName()));
            student.setLastName(required(request.getLastName(), "Last name"));
            student.setEmail(clean(request.getEmail())); student.setPhone(clean(request.getPhone()));
            student.setDateOfBirth(request.getDateOfBirth()); student.setAddress(clean(request.getAddress()));
            student.setStatus(targetStatus);
        } else if (entity instanceof StudentEnrollment enrollment) {
            if (id == null) enrollment.setTenant(tenant);
            enrollment.setStatus(targetStatus);
        }
        validateInput(module, request);
        assignRelationships(module, entity, request, id);
        if (entity instanceof ProgramSubject ps) { ps.setCode(ps.getSubject().getCode()); ps.setName(ps.getSubject().getName()); ps.setDescription(null); }
        if (entity instanceof Semester semester) semester.setSequenceNumber(request.getSequenceNumber());
        if (entity instanceof Subject subject) subject.setCreditHours(request.getCreditHours());
        validateUnique(module, id, entity);
        if (entity instanceof Department department) validateDepartmentFacultyMove(department, department.getFaculty());
        if (entity instanceof Section section && id != null) validateSectionMove(before, section);
        Object saved = repositories.save(normalizeModule(module), entity);
        AcademicRecordDTO after = toDto(module, saved);
        logDiff(module, before, after, id == null ? EntityChangeAction.CREATED : EntityChangeAction.UPDATED, remarks);
        if (before != null && oldStatus != after.getStatus()) changeLogService.logChange(entityName(module), after.getId(), EntityChangeAction.STATUS_CHANGED, "status", oldStatus, after.getStatus(), remarks);
        return after;
    }

    @Override
    public void changeStatus(String module, Long id, Status status, String remarks) {
        if (status == null) throw new ValidationException("Status is required.");
        Object entity = scopedEntity(module, id); Status old;
        if (entity instanceof TenantOwnedEntity owned) { old = owned.getStatus(); owned.setStatus(status); }
        else if (entity instanceof Student student) { old = student.getStatus(); student.setStatus(status); }
        else if (entity instanceof StudentEnrollment enrollment) { old = enrollment.getStatus(); enrollment.setStatus(status); }
        else { ProgramSubject ps = (ProgramSubject) entity; old = ps.getStatus(); ps.setStatus(status); }
        repositories.save(normalizeModule(module), entity);
        changeLogService.logChange(entityName(module), id, EntityChangeAction.STATUS_CHANGED, "status", old, status, remarks);
    }

    @Override
    public void delete(String module, Long id) {
        Object entity = scopedEntity(module, id);
        AcademicRecordDTO deleted = toDto(module, entity);
        changeLogService.logChange(entityName(module), id, EntityChangeAction.DELETED, "record", deleted.getName() != null ? deleted.getName() : deleted.getCode(), null, "Record deleted.");
        repositories.delete(normalizeModule(module), entity);
        try {
            entityManager.flush();
        } catch (DataIntegrityViolationException | PersistenceException exception) {
            throw new ValidationException("This record cannot be deleted while other records depend on it.");
        }
    }

    private void assignRelationships(String module, Object entity, AcademicRecordDTO dto, Long id) {
        long tenant = tenantId();
        if (entity instanceof Department x) x.setFaculty(ref(Faculty.class, dto.getFacultyId(), tenant, "Faculty"));
        if (entity instanceof Program x) {
            Faculty faculty = ref(Faculty.class, dto.getFacultyId(), tenant, "Faculty");
            Department department = ref(Department.class, dto.getDepartmentId(), tenant, "Department");
            if (!Objects.equals(department.getFaculty().getId(), faculty.getId())) throw new ValidationException("Department must belong to the selected faculty.");
            x.setFaculty(faculty); x.setDepartment(department);
        }
        if (entity instanceof Section x) {
            x.setProgram(ref(Program.class, dto.getProgramId(), tenant, "Program"));
            x.setAcademicYear(ref(AcademicYear.class, dto.getAcademicYearId(), tenant, "Academic year"));
            x.setSemester(ref(Semester.class, dto.getSemesterId(), tenant, "Semester"));
        }
        if (entity instanceof ProgramSubject x) {
            x.setProgram(ref(Program.class, dto.getProgramId(), tenant, "Program"));
            x.setSubject(ref(Subject.class, dto.getSubjectId(), tenant, "Subject"));
            x.setSemester(ref(Semester.class, dto.getSemesterId(), tenant, "Semester"));
        }
        if (entity instanceof StudentEnrollment x) {
            Student student = ref(Student.class, dto.getStudentId(), tenant, "Student");
            Program program = ref(Program.class, dto.getProgramId(), tenant, "Program");
            AcademicYear year = ref(AcademicYear.class, dto.getAcademicYearId(), tenant, "Academic year");
            Semester semester = ref(Semester.class, dto.getSemesterId(), tenant, "Semester");
            Section section = ref(Section.class, dto.getSectionId(), tenant, "Section");
            if (!Objects.equals(section.getProgram().getId(), program.getId()) || !Objects.equals(section.getAcademicYear().getId(), year.getId()) || !Objects.equals(section.getSemester().getId(), semester.getId())) throw new ValidationException("Section must match the selected program, academic year and semester.");
            x.setStudent(student); x.setProgram(program); x.setAcademicYear(year); x.setSemester(semester); x.setSection(section);
            Map<String, Object> enrollmentKey = new LinkedHashMap<>();
            enrollmentKey.put("student", student); enrollmentKey.put("program", program);
            enrollmentKey.put("academicYear", year); enrollmentKey.put("semester", semester);
            if (hasDuplicate(StudentEnrollment.class, id, enrollmentKey)) throw new com.college.erp.collegemanagementsystem.exception.DuplicateResourceException("Student already has an enrollment for this program, academic year and semester.");
        }
    }

    private <T> T ref(Class<T> type, Long id, long tenant, String label) {
        if (id == null) throw new ValidationException(label + " is required.");
        T entity = type.cast(repositories.findByTypeAndTenantId(type, id, tenant));
        if (entity == null) throw new ValidationException(label + " is invalid for this tenant.");
        return entity;
    }

    private Object scopedEntity(String module, Long id) {
        if (id == null) throw new ValidationException("Record id is required.");
        Object entity = repositories.findByIdAndTenantId(normalizeModule(module), id, tenantId());
        if (entity == null) throw new ResourceNotFoundException(entityName(module) + " not found.");
        return entity;
    }
    private Object newEntity(String module) { try { return entityType(module).getDeclaredConstructor().newInstance(); } catch (ReflectiveOperationException e) { throw new IllegalArgumentException("Unsupported module.", e); } }
    private long tenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUserPrincipal p) || p.getTenantId() == null) throw new org.springframework.security.access.AccessDeniedException("A tenant account is required.");
        return p.getTenantId();
    }
    private Class<?> entityType(String module) {
        return switch (normalizeModule(module)) {
            case "departments" -> Department.class; case "faculties" -> Faculty.class; case "programs" -> Program.class;
            case "academic-years" -> AcademicYear.class; case "semesters" -> Semester.class; case "sections" -> Section.class;
            case "subjects" -> Subject.class; case "program-subjects" -> ProgramSubject.class; case "students" -> Student.class;
            case "student-enrollments" -> StudentEnrollment.class; default -> throw new IllegalArgumentException("Unknown academic module.");
        };
    }
    private String normalizeModule(String module) { return module == null ? "" : module.trim().toLowerCase(Locale.ROOT); }
    private String entityName(String module) { return entityType(module).getSimpleName(); }
    private List<String> searchFields(String module) {
        return switch (normalizeModule(module)) {
            case "students" -> List.of("registrationNumber", "firstName", "middleName", "lastName", "email", "phone", "address", "dateOfBirth", "status");
            case "departments" -> List.of("code", "name", "description", "faculty.name", "status");
            case "faculties" -> List.of("code", "name", "description", "status");
            case "programs" -> List.of("code", "name", "description", "faculty.name", "department.name", "status");
            case "academic-years" -> List.of("code", "name", "description", "status");
            case "semesters" -> List.of("code", "name", "sequenceNumber", "description", "status");
            case "sections" -> List.of("code", "name", "description", "program.name", "academicYear.name", "semester.name", "status");
            case "program-subjects" -> List.of("program.name", "subject.code", "subject.name", "semester.name", "status");
            case "subjects" -> List.of("code", "name", "creditHours", "description", "status");
            case "student-enrollments" -> List.of("student.registrationNumber", "student.firstName", "student.middleName", "student.lastName", "program.name", "academicYear.name", "semester.name", "section.name", "status");
            default -> List.of("code", "name", "description", "status");
        };
    }
    private Path<String> searchPath(Root<?> root, String field) {
        String[] parts = field.split("\\."); Path<?> path = root;
        for (String part : parts) path = path.get(part);
        @SuppressWarnings("unchecked") Path<String> textPath = (Path<String>) path;
        return textPath;
    }
    private String required(String value, String label) { String result = clean(value); if (result == null) throw new ValidationException(label + " is required."); return result; }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private void validateInput(String module, AcademicRecordDTO dto) {
        if (!"students".equals(module) && !"student-enrollments".equals(module) && !"program-subjects".equals(module)) {
            checkLength(dto.getCode(), 50, "Code"); checkLength(dto.getName(), 160, "Name"); checkLength(dto.getDescription(), 1000, "Description");
        }
        if ("students".equals(module)) {
            checkLength(dto.getRegistrationNumber(), 50, "Registration number");
            checkLength(dto.getFirstName(), 100, "First name"); checkLength(dto.getMiddleName(), 100, "Middle name"); checkLength(dto.getLastName(), 100, "Last name");
            checkLength(dto.getEmail(), 200, "Email"); checkLength(dto.getPhone(), 30, "Phone"); checkLength(dto.getAddress(), 500, "Address");
            if (dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new ValidationException("Email address is invalid.");
            if (dto.getDateOfBirth() != null && dto.getDateOfBirth().isAfter(LocalDate.now())) throw new ValidationException("Date of birth cannot be in the future.");
        }
        if ("semesters".equals(module) && dto.getSequenceNumber() != null && dto.getSequenceNumber() < 1) throw new ValidationException("Sequence number must be greater than zero.");
        if ("subjects".equals(module) && dto.getCreditHours() != null && (dto.getCreditHours().signum() < 0 || dto.getCreditHours().compareTo(new BigDecimal("999.99")) > 0 || dto.getCreditHours().scale() > 2)) throw new ValidationException("Credit hours must be between 0 and 999.99 with at most two decimal places.");
    }

    private void checkLength(String value, int max, String label) {
        if (value != null && value.trim().length() > max) throw new ValidationException(label + " must be at most " + max + " characters.");
    }

    private void validateUnique(String module, Long id, Object entity) {
        if (entity instanceof TenantOwnedEntity owned) {
            if (!(entity instanceof ProgramSubject) && hasDuplicate(entity.getClass(), id, Map.of("code", owned.getCode()))) throw new com.college.erp.collegemanagementsystem.exception.DuplicateResourceException("Code already exists in this tenant.");
            if (!(entity instanceof Section) && !(entity instanceof ProgramSubject) && hasDuplicate(entity.getClass(), id, Map.of("name", owned.getName()))) throw new com.college.erp.collegemanagementsystem.exception.DuplicateResourceException("Name already exists in this tenant.");
            if (entity instanceof Section section) {
                Map<String, Object> sectionKey = new LinkedHashMap<>(); sectionKey.put("program", section.getProgram()); sectionKey.put("academicYear", section.getAcademicYear()); sectionKey.put("semester", section.getSemester()); sectionKey.put("name", section.getName());
                if (hasDuplicate(Section.class, id, sectionKey)) throw new com.college.erp.collegemanagementsystem.exception.DuplicateResourceException("Section name already exists for this program, academic year and semester.");
            }
            if (entity instanceof ProgramSubject ps) {
                Map<String, Object> curriculumKey = new LinkedHashMap<>(); curriculumKey.put("program", ps.getProgram()); curriculumKey.put("subject", ps.getSubject()); curriculumKey.put("semester", ps.getSemester());
                if (hasDuplicate(ProgramSubject.class, id, curriculumKey)) throw new com.college.erp.collegemanagementsystem.exception.DuplicateResourceException("Subject is already assigned to this program and semester.");
            }
        } else if (entity instanceof Student student) {
            if (hasDuplicate(Student.class, id, Map.of("registrationNumber", student.getRegistrationNumber()))) throw new com.college.erp.collegemanagementsystem.exception.DuplicateResourceException("Registration number already exists in this tenant.");
            if (student.getEmail() != null && hasDuplicate(Student.class, id, Map.of("email", student.getEmail()))) throw new com.college.erp.collegemanagementsystem.exception.DuplicateResourceException("Email already exists in this tenant.");
        }
    }

    private boolean hasDuplicate(Class<?> type, Long id, Map<String, Object> fields) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder(); CriteriaQuery<Long> query = cb.createQuery(Long.class); Root<?> root = query.from(type);
        List<Predicate> terms = new ArrayList<>(); terms.add(cb.equal(root.get("tenant").get("id"), tenantId()));
        fields.forEach((field, value) -> {
            Path<?> path = root.get(field);
            if (value instanceof TenantOwnedEntity || value instanceof Student || value instanceof StudentEnrollment || value instanceof Tenant) {
                path = path.get("id");
                Long referenceId = value instanceof TenantOwnedEntity x ? x.getId() : value instanceof Student x ? x.getId() : value instanceof StudentEnrollment x ? x.getId() : ((Tenant) value).getId();
                terms.add(cb.equal(path, referenceId));
            } else if (value instanceof String text) terms.add(cb.equal(cb.lower(path.as(String.class)), text.trim().toLowerCase(Locale.ROOT)));
            else terms.add(cb.equal(path, value));
        });
        if (id != null) terms.add(cb.notEqual(root.get("id"), id));
        query.select(cb.count(root)).where(terms.toArray(Predicate[]::new));
        return entityManager.createQuery(query).getSingleResult() > 0;
    }

    private void validateDepartmentFacultyMove(Department department, Faculty targetFaculty) {
        if (department.getId() == null || targetFaculty == null) return;
        CriteriaBuilder cb = entityManager.getCriteriaBuilder(); CriteriaQuery<Long> query = cb.createQuery(Long.class); Root<Program> root = query.from(Program.class);
        query.select(cb.count(root)).where(cb.equal(root.get("tenant").get("id"), tenantId()), cb.equal(root.get("department").get("id"), department.getId()), cb.notEqual(root.get("faculty").get("id"), targetFaculty.getId()));
        if (entityManager.createQuery(query).getSingleResult() > 0) throw new ValidationException("Department faculty cannot be changed while programs reference it. Update the programs first.");
    }

    private void validateSectionMove(AcademicRecordDTO previous, Section updated) {
        boolean structureChanged = !Objects.equals(previous.getProgramId(), updated.getProgram().getId()) || !Objects.equals(previous.getAcademicYearId(), updated.getAcademicYear().getId()) || !Objects.equals(previous.getSemesterId(), updated.getSemester().getId());
        if (!structureChanged) return;
        CriteriaBuilder cb = entityManager.getCriteriaBuilder(); CriteriaQuery<Long> query = cb.createQuery(Long.class); Root<StudentEnrollment> root = query.from(StudentEnrollment.class);
        query.select(cb.count(root)).where(cb.equal(root.get("tenant").get("id"), tenantId()), cb.equal(root.get("section").get("id"), previous.getId()));
        if (entityManager.createQuery(query).getSingleResult() > 0) throw new ValidationException("Section program, academic year and semester cannot change while student enrollments reference it.");
    }

    private void fetchAssociations(Root<?> root, String module) {
        switch (normalizeModule(module)) {
            case "departments" -> root.fetch("faculty", JoinType.LEFT);
            case "programs" -> { root.fetch("faculty", JoinType.LEFT); root.fetch("department", JoinType.LEFT); }
            case "sections" -> { root.fetch("program", JoinType.LEFT); root.fetch("academicYear", JoinType.LEFT); root.fetch("semester", JoinType.LEFT); }
            case "program-subjects" -> { root.fetch("program", JoinType.LEFT); root.fetch("subject", JoinType.LEFT); root.fetch("semester", JoinType.LEFT); }
            case "student-enrollments" -> { root.fetch("student", JoinType.LEFT); root.fetch("program", JoinType.LEFT); root.fetch("academicYear", JoinType.LEFT); root.fetch("semester", JoinType.LEFT); root.fetch("section", JoinType.LEFT); }
            default -> { }
        }
    }

    private AcademicRecordDTO toDto(String module, Object entity) {
        AcademicRecordDTO d = new AcademicRecordDTO(); d.setModule(normalizeModule(module));
        if (entity instanceof TenantOwnedEntity x) {
            d.setId(x.getId()); d.setCode(x.getCode()); d.setName(x.getName()); d.setDescription(x.getDescription()); d.setStatus(x.getStatus());
            if (x instanceof Department y) { d.setFacultyId(y.getFaculty().getId()); d.setFacultyName(y.getFaculty().getName()); }
            if (x instanceof Program y) { d.setFacultyId(y.getFaculty().getId()); d.setFacultyName(y.getFaculty().getName()); d.setDepartmentId(y.getDepartment().getId()); d.setDepartmentName(y.getDepartment().getName()); }
            if (x instanceof Semester y) d.setSequenceNumber(y.getSequenceNumber());
            if (x instanceof Subject y) d.setCreditHours(y.getCreditHours());
            if (x instanceof Section y) { d.setProgramId(y.getProgram().getId()); d.setProgramName(y.getProgram().getName()); d.setAcademicYearId(y.getAcademicYear().getId()); d.setAcademicYearName(y.getAcademicYear().getName()); d.setSemesterId(y.getSemester().getId()); d.setSemesterName(y.getSemester().getName()); }
            if (x instanceof ProgramSubject y) { d.setCode(y.getSubject().getCode()); d.setName(y.getSubject().getName()); d.setProgramId(y.getProgram().getId()); d.setProgramName(y.getProgram().getName()); d.setSubjectId(y.getSubject().getId()); d.setSubjectName(y.getSubject().getName()); d.setSemesterId(y.getSemester().getId()); d.setSemesterName(y.getSemester().getName()); }
            if (x instanceof ProgramSubject y) { d.setProgramId(y.getProgram().getId()); d.setProgramName(y.getProgram().getName()); d.setSubjectId(y.getSubject().getId()); d.setSubjectName(y.getSubject().getName()); d.setSemesterId(y.getSemester().getId()); d.setSemesterName(y.getSemester().getName()); }
        } else if (entity instanceof Student x) {
            d.setId(x.getId()); d.setRegistrationNumber(x.getRegistrationNumber()); d.setFirstName(x.getFirstName()); d.setMiddleName(x.getMiddleName()); d.setLastName(x.getLastName()); d.setEmail(x.getEmail()); d.setPhone(x.getPhone()); d.setDateOfBirth(x.getDateOfBirth()); d.setAddress(x.getAddress()); d.setStatus(x.getStatus()); d.setName(x.getFirstName() + " " + x.getLastName());
        } else if (entity instanceof StudentEnrollment x) {
            d.setId(x.getId()); d.setCode("ENR-" + x.getId()); d.setName(x.getStudent().getRegistrationNumber() + " - " + x.getProgram().getName()); d.setStatus(x.getStatus()); d.setStudentId(x.getStudent().getId()); d.setStudentName(x.getStudent().getFirstName() + " " + x.getStudent().getLastName()); d.setProgramId(x.getProgram().getId()); d.setProgramName(x.getProgram().getName()); d.setAcademicYearId(x.getAcademicYear().getId()); d.setAcademicYearName(x.getAcademicYear().getName()); d.setSemesterId(x.getSemester().getId()); d.setSemesterName(x.getSemester().getName()); d.setSectionId(x.getSection().getId()); d.setSectionName(x.getSection().getName());
        }
        return d;
    }
    private void logDiff(String module, AcademicRecordDTO old, AcademicRecordDTO now, EntityChangeAction action, String remarks) {
        String name = entityName(module);
        if (old == null) { changeLogService.logChange(name, now.getId(), action, "record", null, now.getName() != null ? now.getName() : now.getCode(), remarks); return; }
        Map<String,Object> before = values(old), after = values(now);
        after.forEach((field, value) -> {
            if (!"status".equals(field)) changeLogService.logChange(name, now.getId(), action, field, before.get(field), value, remarks);
        });
    }
    private Map<String,Object> values(AcademicRecordDTO d) {
        Map<String,Object> values = new LinkedHashMap<>();
        values.put("code",d.getCode()); values.put("name",d.getName()); values.put("description",d.getDescription()); values.put("status",d.getStatus()); values.put("facultyId",d.getFacultyId()); values.put("departmentId",d.getDepartmentId()); values.put("programId",d.getProgramId()); values.put("academicYearId",d.getAcademicYearId()); values.put("semesterId",d.getSemesterId()); values.put("subjectId",d.getSubjectId()); values.put("sectionId",d.getSectionId()); values.put("studentId",d.getStudentId()); values.put("registrationNumber",d.getRegistrationNumber()); values.put("firstName",d.getFirstName()); values.put("middleName",d.getMiddleName()); values.put("lastName",d.getLastName()); values.put("email",d.getEmail()); values.put("phone",d.getPhone()); values.put("creditHours",d.getCreditHours()); values.put("sequenceNumber",d.getSequenceNumber()); return values;
    }
}
