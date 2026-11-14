package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.dto.MarksEntryDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.request.MarksEntryRequest;
import com.college.erp.collegemanagementsystem.entity.Examination;
import com.college.erp.collegemanagementsystem.entity.StudentEnrollment;
import com.college.erp.collegemanagementsystem.entity.StudentMark;
import com.college.erp.collegemanagementsystem.entity.Tenant;
import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.enums.MarkVerificationStatus;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.exception.DuplicateResourceException;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.repository.ExaminationRepository;
import com.college.erp.collegemanagementsystem.repository.StudentEnrollmentRepository;
import com.college.erp.collegemanagementsystem.repository.StudentMarkRepository;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.StudentMarksService;
import com.college.erp.collegemanagementsystem.service.TenantAccessService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class StudentMarksServiceImpl implements StudentMarksService {
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final StudentMarkRepository marks;
    private final StudentEnrollmentRepository enrollments;
    private final ExaminationRepository examinations;
    private final AcademicManagementService academic;
    private final TenantAccessService tenantAccess;
    private final EntityChangeLogService changeLogs;
    private final EntityManager entityManager;

    public StudentMarksServiceImpl(StudentMarkRepository marks, StudentEnrollmentRepository enrollments,
                                   ExaminationRepository examinations, AcademicManagementService academic,
                                   TenantAccessService tenantAccess, EntityChangeLogService changeLogs,
                                   EntityManager entityManager) {
        this.marks = marks;
        this.enrollments = enrollments;
        this.examinations = examinations;
        this.academic = academic;
        this.tenantAccess = tenantAccess;
        this.changeLogs = changeLogs;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public PagablePage<MarksEntryDTO> search(String q, MarkVerificationStatus verificationStatus, Integer page, Integer size, Long requestedTenantId) {
        Long tenantId = tenantAccess.resolve(requestedTenantId, false);
        Specification<StudentMark> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (tenantId != null) predicates.add(cb.equal(root.get("tenant").get("id"), tenantId));
            predicates.add(cb.equal(root.get("status"), Status.ACTIVE));
            if (verificationStatus != null) predicates.add(cb.equal(root.get("verificationStatus"), verificationStatus));
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("enrollment").get("student").get("registrationNumber")), term),
                        cb.like(cb.lower(root.get("enrollment").get("student").get("firstName")), term),
                        cb.like(cb.lower(root.get("enrollment").get("student").get("lastName")), term),
                        cb.like(cb.lower(root.get("examination").get("name")), term),
                        cb.like(cb.lower(root.get("examination").get("programSubject").get("subject").get("name")), term)));
            }
            if (query.getResultType() != Long.class && query.getResultType() != long.class) fetchMarkAssociations(root);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        var results = marks.findAll(spec, PageRequest.of(PagablePage.normalizePage(page) - 1,
                PagablePage.normalizeSize(size), Sort.by(Sort.Direction.DESC, "id")));
        return PagablePage.from(results.map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public MarksEntryDTO get(Long id, Long requestedTenantId) {
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        return toDto(find(id, tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademicRecordDTO> enrollmentOptions(Long requestedTenantId) {
        return academic.options("student-enrollments", tenantAccess.resolve(requestedTenantId, false)).stream()
                .filter(option -> option.getStatus() == Status.ACTIVE).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademicRecordDTO> examinationOptions(Long requestedTenantId) {
        return academic.options("examinations", tenantAccess.resolve(requestedTenantId, false)).stream()
                .filter(option -> option.getStatus() == Status.ACTIVE).toList();
    }

    @Override
    public MarksEntryDTO create(MarksEntryRequest request, Long requestedTenantId) {
        validateRequest(request);
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        Examination examination = examination(request.getExaminationId(), tenantId);
        StudentEnrollment enrollment = enrollment(request.getEnrollmentId(), tenantId);
        validatePair(examination, enrollment, tenantId);
        if (marks.findByTenant_IdAndExamination_IdAndEnrollment_Id(tenantId, examination.getId(), enrollment.getId()).isPresent())
            throw new DuplicateResourceException("Marks have already been entered for this student and examination.");

        StudentMark mark = new StudentMark();
        mark.setTenant(entityManager.getReference(Tenant.class, tenantId));
        mark.setExamination(examination);
        mark.setEnrollment(enrollment);
        mark.setMarksObtained(request.getMarksObtained());
        mark.setVerificationStatus(MarkVerificationStatus.PENDING);
        mark.setStatus(Status.ACTIVE);
        setIdentity(mark);
        StudentMark saved = marks.save(mark);
        changeLogs.logChange("StudentMark", saved.getId(), EntityChangeAction.CREATED, "marksObtained", null,
                saved.getMarksObtained(), "Marks entered and submitted for verification.");
        return toDto(saved);
    }

    @Override
    public MarksEntryDTO update(Long id, MarksEntryRequest request, Long requestedTenantId, String remarks) {
        validateRequest(request);
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        StudentMark mark = find(id, tenantId);
        if (mark.getVerificationStatus() == MarkVerificationStatus.VERIFIED)
            throw new ValidationException("Verified marks cannot be changed.");
        Examination examination = examination(request.getExaminationId(), tenantId);
        StudentEnrollment enrollment = enrollment(request.getEnrollmentId(), tenantId);
        validatePair(examination, enrollment, tenantId);
        StudentMark duplicate = marks.findByTenant_IdAndExamination_IdAndEnrollment_Id(tenantId, examination.getId(), enrollment.getId()).orElse(null);
        if (duplicate != null && !duplicate.getId().equals(id))
            throw new DuplicateResourceException("Marks have already been entered for this student and examination.");

        BigDecimal oldMarks = mark.getMarksObtained();
        Long oldExaminationId = mark.getExamination().getId();
        Long oldEnrollmentId = mark.getEnrollment().getId();
        MarkVerificationStatus oldVerificationStatus = mark.getVerificationStatus();
        mark.setExamination(examination);
        mark.setEnrollment(enrollment);
        mark.setMarksObtained(request.getMarksObtained());
        mark.setVerificationStatus(MarkVerificationStatus.PENDING);
        mark.setReviewedBy(null);
        mark.setReviewedAt(null);
        mark.setVerificationRemarks(null);
        setIdentity(mark);
        StudentMark saved = marks.save(mark);
        String note = clean(remarks, "Marks entry updated and submitted for verification.");
        logDiff(saved, "examinationId", oldExaminationId, examination.getId(), note);
        logDiff(saved, "enrollmentId", oldEnrollmentId, enrollment.getId(), note);
        logDiff(saved, "marksObtained", oldMarks, saved.getMarksObtained(), note);
        if (oldVerificationStatus != MarkVerificationStatus.PENDING)
            changeLogs.logChange("StudentMark", saved.getId(), EntityChangeAction.STATUS_CHANGED,
                    "verificationStatus", oldVerificationStatus, MarkVerificationStatus.PENDING, note);
        return toDto(saved);
    }

    @Override
    public MarksEntryDTO verify(Long id, Long requestedTenantId, String remarks) {
        return review(id, requestedTenantId, MarkVerificationStatus.VERIFIED, remarks);
    }

    @Override
    public MarksEntryDTO reject(Long id, Long requestedTenantId, String remarks) {
        if (remarks == null || remarks.isBlank()) throw new ValidationException("A reason is required when rejecting marks.");
        return review(id, requestedTenantId, MarkVerificationStatus.REJECTED, remarks);
    }

    private MarksEntryDTO review(Long id, Long requestedTenantId, MarkVerificationStatus next, String remarks) {
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        StudentMark mark = find(id, tenantId);
        if (mark.getVerificationStatus() != MarkVerificationStatus.PENDING)
            throw new ValidationException("Only pending marks can be verified or rejected.");
        MarkVerificationStatus old = mark.getVerificationStatus();
        mark.setVerificationStatus(next);
        mark.setReviewedBy(currentUsername());
        mark.setReviewedAt(LocalDateTime.now());
        mark.setVerificationRemarks(clean(remarks, null));
        StudentMark saved = marks.save(mark);
        changeLogs.logChange("StudentMark", saved.getId(), EntityChangeAction.STATUS_CHANGED,
                "verificationStatus", old, next, clean(remarks, "Marks " + next.name().toLowerCase(Locale.ROOT) + "."));
        return toDto(saved);
    }

    private void validateRequest(MarksEntryRequest request) {
        if (request == null || request.getExaminationId() == null || request.getEnrollmentId() == null || request.getMarksObtained() == null)
            throw new ValidationException("Examination, student enrollment, and marks obtained are required.");
        if (request.getMarksObtained().signum() < 0 || request.getMarksObtained().scale() > 2)
            throw new ValidationException("Marks obtained must be zero or greater with at most two decimal places.");
    }

    private void validatePair(Examination examination, StudentEnrollment enrollment, long tenantId) {
        if (!java.util.Objects.equals(examination.getTenant().getId(), tenantId)
                || !java.util.Objects.equals(enrollment.getTenant().getId(), tenantId)
                || !java.util.Objects.equals(examination.getSection().getTenant().getId(), tenantId)
                || !java.util.Objects.equals(examination.getProgramSubject().getTenant().getId(), tenantId)
                || !java.util.Objects.equals(examination.getProgramSubject().getProgram().getTenant().getId(), tenantId)
                || !java.util.Objects.equals(examination.getProgramSubject().getSemester().getTenant().getId(), tenantId)
                || !java.util.Objects.equals(examination.getProgramSubject().getSubject().getTenant().getId(), tenantId)
                || !java.util.Objects.equals(enrollment.getSection().getTenant().getId(), tenantId)
                || !java.util.Objects.equals(enrollment.getProgram().getTenant().getId(), tenantId)
                || !java.util.Objects.equals(enrollment.getSemester().getTenant().getId(), tenantId)
                || !java.util.Objects.equals(enrollment.getStudent().getTenant().getId(), tenantId))
            throw new ValidationException("Examination and enrollment references must belong to the selected tenant.");
        if (examination.getStatus() != Status.ACTIVE) throw new ValidationException("The examination must be active.");
        if (examination.getSection().getStatus() != Status.ACTIVE
                || examination.getProgramSubject().getStatus() != Status.ACTIVE
                || examination.getProgramSubject().getProgram().getStatus() != Status.ACTIVE
                || examination.getProgramSubject().getSemester().getStatus() != Status.ACTIVE
                || examination.getProgramSubject().getSubject().getStatus() != Status.ACTIVE)
            throw new ValidationException("The examination section and subject must be active.");
        if (enrollment.getStatus() != Status.ACTIVE) throw new ValidationException("Marks can only be entered for an active enrollment.");
        if (enrollment.getStudent().getStatus() != Status.ACTIVE) throw new ValidationException("Marks can only be entered for an active student.");
        if (!examination.getSection().getId().equals(enrollment.getSection().getId()))
            throw new ValidationException("The student enrollment must belong to the examination section.");
        if (!examination.getProgramSubject().getProgram().getId().equals(enrollment.getProgram().getId())
                || !examination.getProgramSubject().getSemester().getId().equals(enrollment.getSemester().getId()))
            throw new ValidationException("The examination subject must belong to the student's program and semester.");
    }

    private Examination examination(Long id, long tenantId) {
        return examinations.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found in this tenant."));
    }

    private StudentEnrollment enrollment(Long id, long tenantId) {
        return enrollments.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Student enrollment not found in this tenant."));
    }

    private StudentMark find(Long id, long tenantId) {
        return marks.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Marks entry not found in this tenant."));
    }

    private void setIdentity(StudentMark mark) {
        mark.setCode("MARK-" + mark.getExamination().getId() + "-" + mark.getEnrollment().getId());
        mark.setName("Marks " + mark.getExamination().getCode() + " - " + mark.getEnrollment().getStudent().getRegistrationNumber());
    }

    private void fetchMarkAssociations(jakarta.persistence.criteria.Root<StudentMark> root) {
        root.fetch("tenant", JoinType.LEFT);
        var enrollment = root.fetch("enrollment", JoinType.LEFT);
        enrollment.fetch("student", JoinType.LEFT);
        enrollment.fetch("section", JoinType.LEFT);
        var examination = root.fetch("examination", JoinType.LEFT);
        examination.fetch("section", JoinType.LEFT);
        examination.fetch("programSubject", JoinType.LEFT).fetch("subject", JoinType.LEFT);
    }

    private MarksEntryDTO toDto(StudentMark mark) {
        MarksEntryDTO dto = new MarksEntryDTO();
        dto.setId(mark.getId());
        dto.setTenantId(mark.getTenant().getId());
        dto.setTenantName(mark.getTenant().getTenantName());
        dto.setExaminationId(mark.getExamination().getId());
        dto.setExaminationCode(mark.getExamination().getCode());
        dto.setExaminationName(mark.getExamination().getName());
        dto.setExamDate(mark.getExamination().getExamDate());
        dto.setMaximumMarks(mark.getExamination().getMaximumMarks());
        dto.setEnrollmentId(mark.getEnrollment().getId());
        dto.setStudentId(mark.getEnrollment().getStudent().getId());
        dto.setRegistrationNumber(mark.getEnrollment().getStudent().getRegistrationNumber());
        dto.setStudentName(mark.getEnrollment().getStudent().getFirstName() + " " + mark.getEnrollment().getStudent().getLastName());
        dto.setSectionName(mark.getEnrollment().getSection().getName());
        dto.setSubjectName(mark.getExamination().getProgramSubject().getSubject().getName());
        dto.setMarksObtained(mark.getMarksObtained());
        dto.setVerificationStatus(mark.getVerificationStatus());
        dto.setReviewedBy(mark.getReviewedBy());
        dto.setReviewedAt(mark.getReviewedAt());
        dto.setReviewedAtDisplay(mark.getReviewedAt() == null ? null : DISPLAY_DATE_TIME.format(mark.getReviewedAt()));
        dto.setVerificationRemarks(mark.getVerificationRemarks());
        dto.setStatus(mark.getStatus());
        return dto;
    }

    private void logDiff(StudentMark mark, String field, Object before, Object after, String remarks) {
        if (java.util.Objects.equals(before, after)) return;
        changeLogs.logChange("StudentMark", mark.getId(), EntityChangeAction.UPDATED, field, before, after, remarks);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)
            return principal.getUsername();
        return "system";
    }

    private String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
