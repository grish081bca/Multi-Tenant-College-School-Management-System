package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.dto.AdmissionApplicationDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.request.AdmissionApplicationRequest;
import com.college.erp.collegemanagementsystem.entity.*;
import com.college.erp.collegemanagementsystem.enums.AdmissionStatus;
import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.repository.AdmissionApplicationRepository;
import com.college.erp.collegemanagementsystem.repository.StudentRepository;
import com.college.erp.collegemanagementsystem.service.AdmissionApplicationService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.TenantAccessService;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class AdmissionApplicationServiceImpl implements AdmissionApplicationService {
    private final AdmissionApplicationRepository admissions;
    private final StudentRepository students;
    private final EntityManager entityManager;
    private final TenantAccessService tenantAccess;
    private final EntityChangeLogService changeLogs;

    public AdmissionApplicationServiceImpl(AdmissionApplicationRepository admissions, StudentRepository students, EntityManager entityManager,
                                           TenantAccessService tenantAccess, EntityChangeLogService changeLogs) {
        this.admissions = admissions;
        this.students = students;
        this.entityManager = entityManager;
        this.tenantAccess = tenantAccess;
        this.changeLogs = changeLogs;
    }

    @Override
    @Transactional(readOnly = true)
    public PagablePage<AdmissionApplicationDTO> search(String q, AdmissionStatus status, Integer page, Integer size, Long requestedTenantId) {
        Long tenantId = tenantAccess.resolve(requestedTenantId, false);
        Specification<AdmissionApplication> spec = (root, query, cb) -> {
            var p = cb.isFalse(root.get("deleted"));
            if (tenantId != null) p = cb.and(p, cb.equal(root.get("tenant").get("id"), tenantId));
            if (status != null) p = cb.and(p, cb.equal(root.get("status"), status));
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                p = cb.and(p, cb.or(cb.like(cb.lower(root.get("applicationNumber")), term), cb.like(cb.lower(root.get("firstName")), term),
                        cb.like(cb.lower(root.get("middleName")), term), cb.like(cb.lower(root.get("lastName")), term),
                        cb.like(cb.lower(root.get("email")), term), cb.like(cb.lower(root.get("phone")), term),
                        cb.like(cb.lower(root.get("program").get("name")), term)));
            }
            return p;
        };
        var rows = admissions.findAll(spec, PageRequest.of(PagablePage.normalizePage(page) - 1,
                PagablePage.normalizeSize(size), Sort.by(Sort.Direction.DESC, "id")));
        return PagablePage.from(rows.map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public AdmissionApplicationDTO get(Long id, Long requestedTenantId) {
        return toDto(find(id, tenantAccess.resolve(requestedTenantId, true)));
    }

    @Override
    public AdmissionApplicationDTO create(AdmissionApplicationRequest request, Long requestedTenantId) {
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        AdmissionApplication application = new AdmissionApplication();
        application.setTenant(entityManager.getReference(Tenant.class, tenantId));
        application.setApplicationNumber("APP-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase(Locale.ROOT));
        application.setStatus(AdmissionStatus.DRAFT);
        apply(application, request, tenantId);
        AdmissionApplication saved = admissions.save(application);
        log(saved, "applicationNumber", null, saved.getApplicationNumber(), EntityChangeAction.CREATED, "Admission application created.");
        log(saved, "status", null, saved.getStatus(), EntityChangeAction.CREATED, "Admission application created.");
        log(saved, "firstName", null, saved.getFirstName(), EntityChangeAction.CREATED, "Admission application created.");
        log(saved, "middleName", null, saved.getMiddleName(), EntityChangeAction.CREATED, "Admission application created.");
        log(saved, "lastName", null, saved.getLastName(), EntityChangeAction.CREATED, "Admission application created.");
        log(saved, "email", null, saved.getEmail(), EntityChangeAction.CREATED, "Admission application created.");
        log(saved, "phone", null, saved.getPhone(), EntityChangeAction.CREATED, "Admission application created.");
        log(saved, "program", null, saved.getProgram().getName(), EntityChangeAction.CREATED, "Admission application created.");
        log(saved, "academicYear", null, saved.getAcademicYear().getName(), EntityChangeAction.CREATED, "Admission application created.");
        log(saved, "semester", null, saved.getSemester().getName(), EntityChangeAction.CREATED, "Admission application created.");
        log(saved, "section", null, saved.getSection().getName(), EntityChangeAction.CREATED, "Admission application created.");
        return toDto(saved);
    }

    @Override
    public AdmissionApplicationDTO update(Long id, AdmissionApplicationRequest request, Long requestedTenantId, String remarks) {
        AdmissionApplication application = find(id, tenantAccess.resolve(requestedTenantId, true));
        if (application.getStatus() != AdmissionStatus.DRAFT)
            throw new ValidationException("Only draft applications can be edited.");
        AdmissionApplicationDTO before = toDto(application);
        apply(application, request, application.getTenant().getId());
        AdmissionApplicationDTO after = toDto(admissions.save(application));
        auditDifferences(before, after, EntityChangeAction.UPDATED, clean(remarks, "Admission application updated."));
        return after;
    }

    @Override
    public AdmissionApplicationDTO transition(Long id, AdmissionStatus next, Long requestedTenantId, String remarks) {
        AdmissionApplication application = find(id, tenantAccess.resolve(requestedTenantId, true));
        AdmissionStatus old = application.getStatus();
        if (!allowed(old, next))
            throw new ValidationException("Admission cannot transition from " + old + " to " + next + ".");
        if (next == AdmissionStatus.ACCEPTED) admit(application, application.getTenant().getId());
        application.setStatus(next);
        if (remarks != null && !remarks.isBlank()) application.setRemarks(remarks.trim());
        AdmissionApplication saved = admissions.save(application);
        log(saved, "status", old, next, EntityChangeAction.STATUS_CHANGED, clean(remarks, "Admission status changed."));
        return toDto(saved);
    }

    @Override
    public void delete(Long id, Long requestedTenantId, String remarks) {
        AdmissionApplication application = find(id, tenantAccess.resolve(requestedTenantId, true));
        if (application.getStatus() == AdmissionStatus.ACCEPTED)
            throw new ValidationException("An accepted admission cannot be deleted.");
        application.setDeleted(true);
        admissions.save(application);
        log(application, "deleted", false, true, EntityChangeAction.DELETED, clean(remarks, "Admission application deleted."));
    }

    private void apply(AdmissionApplication a, AdmissionApplicationRequest r, long tenantId) {
        a.setFirstName(r.getFirstName().trim());
        a.setMiddleName(clean(r.getMiddleName(), null));
        a.setLastName(r.getLastName().trim());
        a.setEmail(clean(r.getEmail(), null));
        a.setPhone(clean(r.getPhone(), null));
        a.setDateOfBirth(r.getDateOfBirth());
        a.setAddress(clean(r.getAddress(), null));
        a.setApplicationDate(r.getApplicationDate());
        a.setRemarks(clean(r.getRemarks(), null));
        Program program = scoped(Program.class, r.getProgramId(), tenantId, "Program");
        AcademicYear year = scoped(AcademicYear.class, r.getAcademicYearId(), tenantId, "Academic year");
        Semester semester = scoped(Semester.class, r.getSemesterId(), tenantId, "Semester");
        Section section = scoped(Section.class, r.getSectionId(), tenantId, "Section");
        if (!section.getProgram().getId().equals(program.getId()) || !section.getAcademicYear().getId().equals(year.getId()) || !section.getSemester().getId().equals(semester.getId())) {
            throw new ValidationException("The selected section must belong to the selected program, academic year, and semester.");
        }
        if (program.getStatus() != Status.ACTIVE || year.getStatus() != Status.ACTIVE || semester.getStatus() != Status.ACTIVE || section.getStatus() != Status.ACTIVE) {
            throw new ValidationException("The selected program, academic year, semester, and section must be active.");
        }
        a.setProgram(program);
        a.setAcademicYear(year);
        a.setSemester(semester);
        a.setSection(section);
    }

    private void admit(AdmissionApplication a, long tenantId) {
        if (a.getAdmittedStudent() != null) return;
        if (a.getEmail() != null && students.existsByTenant_IdAndEmailIgnoreCase(tenantId, a.getEmail())) {
            throw new ValidationException("A student with this email already exists in this tenant.");
        }
        Student student = new Student();
        student.setTenant(a.getTenant());
        student.setRegistrationNumber("ADM-" + a.getId());
        student.setFirstName(a.getFirstName());
        student.setMiddleName(a.getMiddleName());
        student.setLastName(a.getLastName());
        student.setEmail(a.getEmail());
        student.setPhone(a.getPhone());
        student.setDateOfBirth(a.getDateOfBirth());
        student.setAddress(a.getAddress());
        student.setStatus(Status.ACTIVE);
        student = students.save(student);
        StudentEnrollment enrollment = new StudentEnrollment();
        enrollment.setTenant(a.getTenant());
        enrollment.setStudent(student);
        enrollment.setProgram(a.getProgram());
        enrollment.setAcademicYear(a.getAcademicYear());
        enrollment.setSemester(a.getSemester());
        enrollment.setSection(a.getSection());
        enrollment.setStatus(Status.ACTIVE);
        entityManager.persist(enrollment);
        a.setAdmittedStudent(student);
        changeLogs.logChange("Student", student.getId(), EntityChangeAction.CREATED, "registrationNumber", null, student.getRegistrationNumber(), "Created from accepted admission " + a.getApplicationNumber() + ".");
        changeLogs.logChange("Student", student.getId(), EntityChangeAction.CREATED, "firstName", null, student.getFirstName(), "Created from accepted admission " + a.getApplicationNumber() + ".");
        changeLogs.logChange("Student", student.getId(), EntityChangeAction.CREATED, "lastName", null, student.getLastName(), "Created from accepted admission " + a.getApplicationNumber() + ".");
        changeLogs.logChange("Student", student.getId(), EntityChangeAction.CREATED, "email", null, student.getEmail(), "Created from accepted admission " + a.getApplicationNumber() + ".");
        changeLogs.logChange("Student", student.getId(), EntityChangeAction.CREATED, "phone", null, student.getPhone(), "Created from accepted admission " + a.getApplicationNumber() + ".");
        changeLogs.logChange("StudentEnrollment", enrollment.getId(), EntityChangeAction.CREATED, "student", null, student.getRegistrationNumber(), "Created from accepted admission " + a.getApplicationNumber() + ".");
        changeLogs.logChange("StudentEnrollment", enrollment.getId(), EntityChangeAction.CREATED, "program", null, enrollment.getProgram().getName(), "Created from accepted admission " + a.getApplicationNumber() + ".");
        changeLogs.logChange("StudentEnrollment", enrollment.getId(), EntityChangeAction.CREATED, "section", null, enrollment.getSection().getName(), "Created from accepted admission " + a.getApplicationNumber() + ".");
    }

    private boolean allowed(AdmissionStatus current, AdmissionStatus next) {
        if (next == null) return false;
        return switch (current) {
            case DRAFT -> next == AdmissionStatus.SUBMITTED || next == AdmissionStatus.WITHDRAWN;
            case SUBMITTED -> next == AdmissionStatus.UNDER_REVIEW || next == AdmissionStatus.WITHDRAWN;
            case UNDER_REVIEW ->
                    next == AdmissionStatus.ACCEPTED || next == AdmissionStatus.REJECTED || next == AdmissionStatus.WITHDRAWN;
            case ACCEPTED, REJECTED, WITHDRAWN -> false;
        };
    }

    private <T> T scoped(Class<T> type, Long id, long tenantId, String label) {
        if (id == null) throw new ValidationException(label + " is required.");
        T entity = entityManager.find(type, id);
        if (entity == null || !tenantIdEquals(entity, tenantId))
            throw new ResourceNotFoundException(label + " not found in this tenant.");
        return entity;
    }

    private boolean tenantIdEquals(Object entity, long tenantId) {
        if (entity instanceof TenantOwnedEntity owned) return owned.getTenant().getId() == tenantId;
        if (entity instanceof Student student) return student.getTenant().getId() == tenantId;
        return false;
    }

    private AdmissionApplication find(Long id, long tenantId) {
        return admissions.findByIdAndTenantIdAndDeletedFalse(id, tenantId).orElseThrow(() -> new ResourceNotFoundException("Admission application not found."));
    }

    private AdmissionApplicationDTO toDto(AdmissionApplication a) {
        AdmissionApplicationDTO d = new AdmissionApplicationDTO();
        d.setId(a.getId());
        d.setTenantId(a.getTenant().getId());
        d.setTenantName(a.getTenant().getTenantName());
        d.setApplicationNumber(a.getApplicationNumber());
        d.setFirstName(a.getFirstName());
        d.setMiddleName(a.getMiddleName());
        d.setLastName(a.getLastName());
        d.setEmail(a.getEmail());
        d.setPhone(a.getPhone());
        d.setDateOfBirth(a.getDateOfBirth());
        d.setAddress(a.getAddress());
        d.setProgramId(a.getProgram().getId());
        d.setProgramName(a.getProgram().getName());
        d.setAcademicYearId(a.getAcademicYear().getId());
        d.setAcademicYearName(a.getAcademicYear().getName());
        d.setSemesterId(a.getSemester().getId());
        d.setSemesterName(a.getSemester().getName());
        d.setSectionId(a.getSection().getId());
        d.setSectionName(a.getSection().getName());
        d.setApplicationDate(a.getApplicationDate());
        d.setStatus(a.getStatus());
        d.setRemarks(a.getRemarks());
        if (a.getAdmittedStudent() != null) {
            d.setAdmittedStudentId(a.getAdmittedStudent().getId());
            d.setAdmittedRegistrationNumber(a.getAdmittedStudent().getRegistrationNumber());
        }
        return d;
    }

    private void auditDifferences(AdmissionApplicationDTO before, AdmissionApplicationDTO after, EntityChangeAction action, String remarks) {
        diff(after, "firstName", before.getFirstName(), after.getFirstName(), action, remarks);
        diff(after, "middleName", before.getMiddleName(), after.getMiddleName(), action, remarks);
        diff(after, "lastName", before.getLastName(), after.getLastName(), action, remarks);
        diff(after, "email", before.getEmail(), after.getEmail(), action, remarks);
        diff(after, "phone", before.getPhone(), after.getPhone(), action, remarks);
        diff(after, "program", before.getProgramName(), after.getProgramName(), action, remarks);
        diff(after, "academicYear", before.getAcademicYearName(), after.getAcademicYearName(), action, remarks);
        diff(after, "semester", before.getSemesterName(), after.getSemesterName(), action, remarks);
        diff(after, "section", before.getSectionName(), after.getSectionName(), action, remarks);
        diff(after, "applicationDate", before.getApplicationDate(), after.getApplicationDate(), action, remarks);
    }

    private void diff(AdmissionApplicationDTO d, String field, Object oldValue, Object newValue, EntityChangeAction action, String remarks) {
        changeLogs.logChange("AdmissionApplication", d.getId(), action, field, oldValue, newValue, remarks);
    }

    private void log(AdmissionApplication a, String field, Object before, Object after, EntityChangeAction action, String remarks) {
        changeLogs.logChange("AdmissionApplication", a.getId(), action, field, before, after, remarks);
    }

    private String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
