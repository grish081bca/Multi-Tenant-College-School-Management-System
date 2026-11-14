package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.StudentResultCourseDTO;
import com.college.erp.collegemanagementsystem.dto.StudentResultDTO;
import com.college.erp.collegemanagementsystem.entity.*;
import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.enums.MarkVerificationStatus;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.repository.ExaminationRepository;
import com.college.erp.collegemanagementsystem.repository.StudentEnrollmentRepository;
import com.college.erp.collegemanagementsystem.repository.StudentMarkRepository;
import com.college.erp.collegemanagementsystem.repository.StudentResultRepository;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.StudentResultsService;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional
public class StudentResultsServiceImpl implements StudentResultsService {
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final BigDecimal GPA_SCALE = new BigDecimal("4.00");
    private static final BigDecimal PERCENT_PER_GPA = new BigDecimal("25");

    private final StudentResultRepository results;
    private final StudentEnrollmentRepository enrollments;
    private final ExaminationRepository examinations;
    private final StudentMarkRepository marks;
    private final AcademicManagementService academic;
    private final TenantAccessService tenantAccess;
    private final EntityChangeLogService changeLogs;
    private final EntityManager entityManager;

    public StudentResultsServiceImpl(StudentResultRepository results, StudentEnrollmentRepository enrollments,
                                     ExaminationRepository examinations, StudentMarkRepository marks,
                                     AcademicManagementService academic, TenantAccessService tenantAccess,
                                     EntityChangeLogService changeLogs, EntityManager entityManager) {
        this.results = results;
        this.enrollments = enrollments;
        this.examinations = examinations;
        this.marks = marks;
        this.academic = academic;
        this.tenantAccess = tenantAccess;
        this.changeLogs = changeLogs;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public PagablePage<StudentResultDTO> search(String q, Integer page, Integer size, Long requestedTenantId) {
        Long tenantId = tenantAccess.resolve(requestedTenantId, false);
        Specification<StudentResult> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), Status.ACTIVE));
            if (tenantId != null) predicates.add(cb.equal(root.get("tenant").get("id"), tenantId));
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("enrollment").get("student").get("registrationNumber")), term),
                        cb.like(cb.lower(root.get("enrollment").get("student").get("firstName")), term),
                        cb.like(cb.lower(root.get("enrollment").get("student").get("lastName")), term),
                        cb.like(cb.lower(root.get("enrollment").get("program").get("name")), term),
                        cb.like(cb.lower(root.get("enrollment").get("semester").get("name")), term)));
            }
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("tenant", JoinType.LEFT);
                var enrollment = root.fetch("enrollment", JoinType.LEFT);
                enrollment.fetch("student", JoinType.LEFT);
                enrollment.fetch("program", JoinType.LEFT);
                enrollment.fetch("academicYear", JoinType.LEFT);
                enrollment.fetch("semester", JoinType.LEFT);
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        var rows = results.findAll(spec, PageRequest.of(PagablePage.normalizePage(page) - 1,
                PagablePage.normalizeSize(size), Sort.by(Sort.Direction.DESC, "id")));
        return PagablePage.from(rows.map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResultDTO get(Long id, Long requestedTenantId) {
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
    public StudentResultDTO process(Long enrollmentId, Long requestedTenantId) {
        if (enrollmentId == null) throw new ValidationException("Student enrollment is required to process results.");
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        StudentEnrollment enrollment = enrollments.findByIdAndTenant_Id(enrollmentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Student enrollment not found in this tenant."));
        if (enrollment.getStatus() != Status.ACTIVE) throw new ValidationException("Results can only be processed for an active enrollment.");
        validateEnrollmentScope(enrollment, tenantId);
        if (enrollment.getStudent().getStatus() != Status.ACTIVE
                || enrollment.getProgram().getStatus() != Status.ACTIVE
                || enrollment.getAcademicYear().getStatus() != Status.ACTIVE
                || enrollment.getSemester().getStatus() != Status.ACTIVE
                || enrollment.getSection().getStatus() != Status.ACTIVE)
            throw new ValidationException("Results can only be processed for active academic records.");

        List<Examination> activeExaminations = examinations.findAllByTenant_IdAndSection_IdAndStatusOrderByExamDateAscStartTimeAsc(
                tenantId, enrollment.getSection().getId(), Status.ACTIVE).stream()
                .filter(exam -> exam.getProgramSubject().getProgram().getId().equals(enrollment.getProgram().getId()))
                .filter(exam -> exam.getProgramSubject().getSemester().getId().equals(enrollment.getSemester().getId()))
                .toList();
        if (activeExaminations.isEmpty()) throw new ValidationException("There are no active examinations for this enrollment's section and semester.");

        Map<Long, CourseTotals> totalsByProgramSubject = new LinkedHashMap<>();
        for (Examination examination : activeExaminations) {
            if (!java.util.Objects.equals(examination.getTenant().getId(), tenantId)
                    || !java.util.Objects.equals(examination.getSection().getTenant().getId(), tenantId)
                    || !java.util.Objects.equals(examination.getProgramSubject().getTenant().getId(), tenantId)
                    || !java.util.Objects.equals(examination.getProgramSubject().getProgram().getTenant().getId(), tenantId)
                    || !java.util.Objects.equals(examination.getProgramSubject().getSemester().getTenant().getId(), tenantId)
                    || !java.util.Objects.equals(examination.getProgramSubject().getSubject().getTenant().getId(), tenantId))
                throw new ValidationException("Examination references must belong to the selected tenant.");
            if (examination.getSection().getStatus() != Status.ACTIVE
                    || examination.getProgramSubject().getStatus() != Status.ACTIVE
                    || examination.getProgramSubject().getProgram().getStatus() != Status.ACTIVE
                    || examination.getProgramSubject().getSemester().getStatus() != Status.ACTIVE
                    || examination.getProgramSubject().getSubject().getStatus() != Status.ACTIVE)
                throw new ValidationException("Results can only include examinations with active academic records.");
            StudentMark mark = marks.findByTenant_IdAndExamination_IdAndEnrollment_Id(tenantId, examination.getId(), enrollmentId)
                    .orElseThrow(() -> new ValidationException("Marks are missing for examination " + examination.getCode() + "."));
            if (mark.getVerificationStatus() != MarkVerificationStatus.VERIFIED)
                throw new ValidationException("All examination marks must be verified before results can be processed.");
            if (!java.util.Objects.equals(mark.getTenant().getId(), tenantId)
                    || !java.util.Objects.equals(mark.getEnrollment().getTenant().getId(), tenantId)
                    || !java.util.Objects.equals(mark.getExamination().getTenant().getId(), tenantId)
                    || !java.util.Objects.equals(mark.getEnrollment().getId(), enrollmentId)
                    || !java.util.Objects.equals(mark.getExamination().getId(), examination.getId()))
                throw new ValidationException("Marks references must belong to the selected tenant and enrollment.");
            Long programSubjectId = examination.getProgramSubject().getId();
            CourseTotals totals = totalsByProgramSubject.computeIfAbsent(programSubjectId,
                    ignored -> new CourseTotals(examination.getProgramSubject()));
            totals.obtained = totals.obtained.add(mark.getMarksObtained());
            totals.maximum = totals.maximum.add(examination.getMaximumMarks());
        }

        BigDecimal totalObtained = BigDecimal.ZERO;
        BigDecimal totalMaximum = BigDecimal.ZERO;
        BigDecimal weightedGradePoints = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        List<StudentResultCourse> courseRows = new ArrayList<>();
        for (CourseTotals course : totalsByProgramSubject.values()) {
            BigDecimal credits = course.programSubject.getSubject().getCreditHours();
            if (credits == null || credits.signum() <= 0)
                throw new ValidationException("Every result subject must have positive credit hours before GPA can be calculated.");
            BigDecimal percentage = course.obtained.multiply(new BigDecimal("100"))
                    .divide(course.maximum, 2, RoundingMode.HALF_UP);
            BigDecimal gradePoint = percentage.divide(PERCENT_PER_GPA, 4, RoundingMode.HALF_UP).min(GPA_SCALE);
            totalObtained = totalObtained.add(course.obtained);
            totalMaximum = totalMaximum.add(course.maximum);
            totalCredits = totalCredits.add(credits);
            weightedGradePoints = weightedGradePoints.add(gradePoint.multiply(credits));

            StudentResultCourse courseRow = new StudentResultCourse();
            courseRow.setProgramSubject(course.programSubject);
            courseRow.setMarksObtained(course.obtained);
            courseRow.setMaximumMarks(course.maximum);
            courseRow.setPercentage(percentage);
            courseRow.setCreditHours(credits);
            courseRow.setGradePoint(gradePoint);
            courseRows.add(courseRow);
        }
        BigDecimal overallPercentage = totalObtained.multiply(new BigDecimal("100"))
                .divide(totalMaximum, 2, RoundingMode.HALF_UP);
        BigDecimal gpa = weightedGradePoints.divide(totalCredits, 2, RoundingMode.HALF_UP).min(GPA_SCALE);

        StudentResult result = results.findByTenant_IdAndEnrollment_Id(tenantId, enrollmentId).orElseGet(() -> {
            StudentResult created = new StudentResult();
            created.setTenant(entityManager.getReference(Tenant.class, tenantId));
            created.setEnrollment(enrollment);
            created.setCode("RESULT-" + enrollmentId);
            created.setName("Result " + enrollment.getStudent().getRegistrationNumber() + " - " + enrollment.getSemester().getName());
            created.setStatus(Status.ACTIVE);
            return created;
        });
        boolean created = result.getId() == null;
        BigDecimal oldGpa = result.getGpa();
        BigDecimal oldPercentage = result.getPercentage();
        BigDecimal oldObtained = result.getTotalMarksObtained();
        BigDecimal oldMaximum = result.getTotalMaximumMarks();
        LocalDateTime oldProcessedAt = result.getProcessedAt();
        result.getCourseResults().clear();
        if (!created) entityManager.flush();
        result.setTotalMarksObtained(totalObtained);
        result.setTotalMaximumMarks(totalMaximum);
        result.setPercentage(overallPercentage);
        result.setGpa(gpa);
        result.setProcessedAt(LocalDateTime.now());
        result.setProcessedBy(currentUsername());
        courseRows.forEach(result::addCourseResult);
        StudentResult saved = results.save(result);
        String logRemarks = "Result processed from verified examination marks.";
        if (created) {
            changeLogs.logChange("StudentResult", saved.getId(), EntityChangeAction.CREATED, "result",
                    null, "GPA " + saved.getGpa() + "; percentage " + saved.getPercentage(), logRemarks);
        } else {
            changeLogs.logChange("StudentResult", saved.getId(), EntityChangeAction.UPDATED, "processedAt",
                    oldProcessedAt, saved.getProcessedAt(), "Result recalculated from verified examination marks.");
            logResultChange(saved, "totalMarksObtained", oldObtained, saved.getTotalMarksObtained());
            logResultChange(saved, "totalMaximumMarks", oldMaximum, saved.getTotalMaximumMarks());
            logResultChange(saved, "percentage", oldPercentage, saved.getPercentage());
            logResultChange(saved, "gpa", oldGpa, saved.getGpa());
        }
        return toDto(saved);
    }

    private StudentResult find(Long id, long tenantId) {
        return results.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Student result not found in this tenant."));
    }

    private void logResultChange(StudentResult result, String field, Object before, Object after) {
        if (java.util.Objects.equals(before, after)) return;
        changeLogs.logChange("StudentResult", result.getId(), EntityChangeAction.UPDATED, field,
                before, after, "Result recalculated from verified examination marks.");
    }

    private void validateEnrollmentScope(StudentEnrollment enrollment, long tenantId) {
        if (!java.util.Objects.equals(enrollment.getTenant().getId(), tenantId)
                || !java.util.Objects.equals(enrollment.getStudent().getTenant().getId(), tenantId)
                || !java.util.Objects.equals(enrollment.getProgram().getTenant().getId(), tenantId)
                || !java.util.Objects.equals(enrollment.getAcademicYear().getTenant().getId(), tenantId)
                || !java.util.Objects.equals(enrollment.getSemester().getTenant().getId(), tenantId)
                || !java.util.Objects.equals(enrollment.getSection().getTenant().getId(), tenantId))
            throw new ValidationException("Enrollment references must belong to the selected tenant.");
    }

    private StudentResultDTO toDto(StudentResult result) {
        StudentEnrollment enrollment = result.getEnrollment();
        StudentResultDTO dto = new StudentResultDTO();
        dto.setId(result.getId());
        dto.setTenantId(result.getTenant().getId());
        dto.setTenantName(result.getTenant().getTenantName());
        dto.setEnrollmentId(enrollment.getId());
        dto.setRegistrationNumber(enrollment.getStudent().getRegistrationNumber());
        dto.setStudentName(enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName());
        dto.setProgramName(enrollment.getProgram().getName());
        dto.setAcademicYearName(enrollment.getAcademicYear().getName());
        dto.setSemesterName(enrollment.getSemester().getName());
        dto.setTotalMarksObtained(result.getTotalMarksObtained());
        dto.setTotalMaximumMarks(result.getTotalMaximumMarks());
        dto.setPercentage(result.getPercentage());
        dto.setGpa(result.getGpa());
        dto.setProcessedAt(result.getProcessedAt());
        dto.setProcessedAtDisplay(result.getProcessedAt() == null ? null : DISPLAY_DATE_TIME.format(result.getProcessedAt()));
        dto.setProcessedBy(result.getProcessedBy());
        dto.setStatus(result.getStatus());
        dto.setCourses(result.getCourseResults().stream().map(course -> {
            StudentResultCourseDTO courseDTO = new StudentResultCourseDTO();
            courseDTO.setProgramSubjectId(course.getProgramSubject().getId());
            courseDTO.setSubjectCode(course.getProgramSubject().getSubject().getCode());
            courseDTO.setSubjectName(course.getProgramSubject().getSubject().getName());
            courseDTO.setMarksObtained(course.getMarksObtained());
            courseDTO.setMaximumMarks(course.getMaximumMarks());
            courseDTO.setPercentage(course.getPercentage());
            courseDTO.setCreditHours(course.getCreditHours());
            courseDTO.setGradePoint(course.getGradePoint());
            return courseDTO;
        }).sorted(java.util.Comparator.comparing(StudentResultCourseDTO::getSubjectName, String.CASE_INSENSITIVE_ORDER)).toList());
        return dto;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)
            return principal.getUsername();
        return "system";
    }

    private static final class CourseTotals {
        private final ProgramSubject programSubject;
        private BigDecimal obtained = BigDecimal.ZERO;
        private BigDecimal maximum = BigDecimal.ZERO;

        private CourseTotals(ProgramSubject programSubject) { this.programSubject = programSubject; }
    }
}
