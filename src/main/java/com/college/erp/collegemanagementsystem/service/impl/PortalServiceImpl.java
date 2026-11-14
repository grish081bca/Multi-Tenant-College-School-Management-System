package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.dto.*;
import com.college.erp.collegemanagementsystem.entity.*;
import com.college.erp.collegemanagementsystem.enums.EmployeeType;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.enums.UserType;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.repository.*;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.CollegeNoticeService;
import com.college.erp.collegemanagementsystem.service.PortalService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class PortalServiceImpl implements PortalService {
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final UserRepository users;
    private final StudentRepository students;
    private final EmployeeRepository employees;
    private final StudentEnrollmentRepository enrollments;
    private final StudentResultRepository results;
    private final StudentAttendanceRepository attendance;
    private final StudentCertificateRepository certificates;
    private final FeeStructureRepository feeStructures;
    private final FeeCollectionRepository feeCollections;
    private final TimetableSlotRepository timetableSlots;
    private final CollegeNoticeService notices;

    public PortalServiceImpl(UserRepository users, StudentRepository students, EmployeeRepository employees,
                             StudentEnrollmentRepository enrollments, StudentResultRepository results,
                             StudentAttendanceRepository attendance, StudentCertificateRepository certificates,
                             FeeStructureRepository feeStructures, FeeCollectionRepository feeCollections,
                             TimetableSlotRepository timetableSlots, CollegeNoticeService notices) {
        this.users = users;
        this.students = students;
        this.employees = employees;
        this.enrollments = enrollments;
        this.results = results;
        this.attendance = attendance;
        this.certificates = certificates;
        this.feeStructures = feeStructures;
        this.feeCollections = feeCollections;
        this.timetableSlots = timetableSlots;
        this.notices = notices;
    }

    @Override
    public StudentPortalDTO studentPortal() {
        AuthenticatedUserPrincipal principal = requirePrincipal(UserType.STUDENT);
        User account = account(principal);
        Student student = students.findByTenant_IdAndEmailIgnoreCaseAndStatus(principal.getTenantId(), account.getEmail(), Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active student profile matches this account's email in the current tenant."));
        validateStudentScope(student, principal.getTenantId());
        List<StudentEnrollment> activeEnrollments = enrollments.findAllByTenant_IdAndStudent_IdAndStatusOrderByIdDesc(
                principal.getTenantId(), student.getId(), Status.ACTIVE).stream()
                .filter(enrollment -> Objects.equals(enrollment.getTenant().getId(), principal.getTenantId())
                        && Objects.equals(enrollment.getStudent().getTenant().getId(), principal.getTenantId())
                        && Objects.equals(enrollment.getProgram().getTenant().getId(), principal.getTenantId())
                        && Objects.equals(enrollment.getAcademicYear().getTenant().getId(), principal.getTenantId())
                        && Objects.equals(enrollment.getSemester().getTenant().getId(), principal.getTenantId())
                        && Objects.equals(enrollment.getSection().getTenant().getId(), principal.getTenantId()))
                .filter(enrollment -> enrollment.getProgram().getStatus() == Status.ACTIVE
                        && enrollment.getAcademicYear().getStatus() == Status.ACTIVE
                        && enrollment.getSemester().getStatus() == Status.ACTIVE
                        && enrollment.getSection().getStatus() == Status.ACTIVE)
                .toList();

        StudentPortalDTO dto = new StudentPortalDTO();
        dto.setStudentName(student.getFirstName() + " " + student.getLastName());
        dto.setRegistrationNumber(student.getRegistrationNumber());
        dto.setEmail(student.getEmail());
        StudentEnrollment current = activeEnrollments.stream().findFirst().orElse(null);
        if (current != null) {
            dto.setProgramName(current.getProgram().getName());
            dto.setAcademicYearName(current.getAcademicYear().getName());
            dto.setSemesterName(current.getSemester().getName());
            dto.setSectionName(current.getSection().getName());
        }
        dto.setResults(results.findAllByTenant_IdAndEnrollment_Student_IdAndStatusOrderByProcessedAtDesc(
                        principal.getTenantId(), student.getId(), Status.ACTIVE).stream()
                .filter(result -> Objects.equals(result.getTenant().getId(), principal.getTenantId())
                        && Objects.equals(result.getEnrollment().getStudent().getId(), student.getId()))
                .map(this::toResultDto).toList());
        dto.setAttendance(attendance.findAllByTenant_IdAndEnrollment_Student_IdAndStatusOrderByAttendanceDateDesc(
                        principal.getTenantId(), student.getId(), Status.ACTIVE).stream()
                .filter(record -> Objects.equals(record.getEnrollment().getTenant().getId(), principal.getTenantId())
                        && Objects.equals(record.getEnrollment().getStudent().getId(), student.getId()))
                .limit(100).map(this::toAttendanceDto).toList());
        dto.setCertificates(certificates.findAllByTenant_IdAndEnrollment_Student_IdOrderByIssuedOnDesc(
                        principal.getTenantId(), student.getId()).stream()
                .filter(value -> value.getStatus() == Status.ACTIVE)
                .map(this::toCertificateDto).toList());
        dto.setFees(activeEnrollments.stream().flatMap(enrollment -> feeStructures
                        .findAllByTenant_IdAndProgram_IdAndAcademicYear_IdAndSemester_IdAndStatusOrderByIdDesc(
                                principal.getTenantId(), enrollment.getProgram().getId(), enrollment.getAcademicYear().getId(),
                                enrollment.getSemester().getId(), Status.ACTIVE).stream()
                        .map(structure -> toFeeBalance(enrollment, structure, principal.getTenantId())))
                .sorted(Comparator.comparing(FeeBalanceDTO::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))).toList());
        dto.setReceipts(feeCollections.findAllByTenant_IdAndEnrollment_Student_IdOrderByCollectedAtDesc(
                        principal.getTenantId(), student.getId()).stream()
                .map(this::toReceiptDto).toList());
        dto.setNotifications(notices.notifications());
        return dto;
    }

    @Override
    public TeacherPortalDTO teacherPortal() {
        AuthenticatedUserPrincipal principal = requirePrincipal(UserType.TEACHER);
        User account = account(principal);
        Employee teacher = employees.findByTenant_IdAndEmailIgnoreCaseAndEmployeeTypeAndStatusAndDeletedFalse(
                        principal.getTenantId(), account.getEmail(), EmployeeType.TEACHER, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active teacher profile matches this account's email in the current tenant."));
        if (!Objects.equals(teacher.getTenant().getId(), principal.getTenantId()))
            throw new AccessDeniedException("Teacher profile is outside the authenticated tenant.");
        TeacherPortalDTO dto = new TeacherPortalDTO();
        dto.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
        dto.setEmployeeNumber(teacher.getEmployeeNumber());
        dto.setDesignation(teacher.getDesignation());
        dto.setDepartmentName(teacher.getDepartment() == null ? null : teacher.getDepartment().getName());
        dto.setEmail(teacher.getEmail());
        dto.setTimetable(timetableSlots.findAllByTenant_IdAndTeacher_IdAndStatusOrderByDayOfWeekAscStartTimeAsc(
                        principal.getTenantId(), teacher.getId(), Status.ACTIVE).stream()
                .filter(slot -> Objects.equals(slot.getSection().getTenant().getId(), principal.getTenantId())
                        && Objects.equals(slot.getProgramSubject().getTenant().getId(), principal.getTenantId())
                        && Objects.equals(slot.getProgramSubject().getSubject().getTenant().getId(), principal.getTenantId()))
                .map(this::toTimetableDto).toList());
        dto.setNotifications(notices.notifications());
        return dto;
    }

    private AuthenticatedUserPrincipal requirePrincipal(UserType expected) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal
                && principal.getTenantId() != null && principal.getUserType() == expected) return principal;
        throw new AccessDeniedException("A tenant " + expected.name().toLowerCase() + " account is required.");
    }

    private User account(AuthenticatedUserPrincipal principal) {
        User user = users.findByIdAndTenant_Id(principal.getUserId(), principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated account not found in this tenant."));
        if (user.getUserType() != principal.getUserType() || user.getEmail() == null || user.getEmail().isBlank())
            throw new ResourceNotFoundException("This account has no linked email address for its portal profile.");
        return user;
    }

    private void validateStudentScope(Student student, Long tenantId) {
        if (!Objects.equals(student.getTenant().getId(), tenantId)) throw new AccessDeniedException("Student profile is outside the authenticated tenant.");
    }

    private StudentResultDTO toResultDto(StudentResult result) {
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
            StudentResultCourseDTO courseDto = new StudentResultCourseDTO();
            courseDto.setProgramSubjectId(course.getProgramSubject().getId());
            courseDto.setSubjectCode(course.getProgramSubject().getSubject().getCode());
            courseDto.setSubjectName(course.getProgramSubject().getSubject().getName());
            courseDto.setMarksObtained(course.getMarksObtained());
            courseDto.setMaximumMarks(course.getMaximumMarks());
            courseDto.setPercentage(course.getPercentage());
            courseDto.setCreditHours(course.getCreditHours());
            courseDto.setGradePoint(course.getGradePoint());
            return courseDto;
        }).sorted(Comparator.comparing(StudentResultCourseDTO::getSubjectName, String.CASE_INSENSITIVE_ORDER)).toList());
        return dto;
    }

    private PortalAttendanceDTO toAttendanceDto(StudentAttendance record) {
        PortalAttendanceDTO dto = new PortalAttendanceDTO();
        dto.setAttendanceDate(record.getAttendanceDate());
        dto.setStatus(record.getAttendanceStatus());
        dto.setSectionName(record.getEnrollment().getSection().getName());
        return dto;
    }

    private StudentCertificateDTO toCertificateDto(StudentCertificate certificate) {
        StudentCertificateDTO dto = new StudentCertificateDTO();
        dto.setId(certificate.getId());
        dto.setTenantId(certificate.getTenant().getId());
        dto.setEnrollmentId(certificate.getEnrollment().getId());
        dto.setRegistrationNumber(certificate.getEnrollment().getStudent().getRegistrationNumber());
        dto.setStudentName(certificate.getEnrollment().getStudent().getFirstName() + " " + certificate.getEnrollment().getStudent().getLastName());
        dto.setCertificateNumber(certificate.getCode());
        dto.setCertificateType(certificate.getCertificateType());
        dto.setTitle(certificate.getName());
        dto.setIssuedOn(certificate.getIssuedOn());
        dto.setStatus(certificate.getStatus());
        return dto;
    }

    private FeeBalanceDTO toFeeBalance(StudentEnrollment enrollment, FeeStructure structure, Long tenantId) {
        BigDecimal paid = feeCollections.totalCollected(tenantId, enrollment.getId(), structure.getId());
        FeeBalanceDTO dto = new FeeBalanceDTO();
        dto.setFeeStructureId(structure.getId());
        dto.setFeeCode(structure.getCode());
        dto.setFeeName(structure.getName());
        dto.setAmount(structure.getAmount());
        dto.setPaid(paid);
        dto.setBalance(structure.getAmount().subtract(paid).max(BigDecimal.ZERO));
        dto.setDueDate(structure.getDueDate());
        return dto;
    }

    private FeeCollectionDTO toReceiptDto(FeeCollection collection) {
        BigDecimal paid = feeCollections.totalCollected(collection.getTenant().getId(), collection.getEnrollment().getId(), collection.getFeeStructure().getId());
        FeeCollectionDTO dto = new FeeCollectionDTO();
        dto.setId(collection.getId());
        dto.setTenantId(collection.getTenant().getId());
        dto.setTenantName(collection.getTenant().getTenantName());
        dto.setEnrollmentId(collection.getEnrollment().getId());
        dto.setFeeStructureId(collection.getFeeStructure().getId());
        dto.setFeeCode(collection.getFeeStructure().getCode());
        dto.setFeeName(collection.getFeeStructure().getName());
        dto.setRegistrationNumber(collection.getEnrollment().getStudent().getRegistrationNumber());
        dto.setStudentName(collection.getEnrollment().getStudent().getFirstName() + " " + collection.getEnrollment().getStudent().getLastName());
        dto.setReceiptNumber(collection.getReceiptNumber());
        dto.setAmount(collection.getAmount());
        dto.setFeeAmount(collection.getFeeStructure().getAmount());
        dto.setTotalPaid(paid);
        dto.setBalance(collection.getFeeStructure().getAmount().subtract(paid).max(BigDecimal.ZERO));
        dto.setPaymentMethod(collection.getPaymentMethod());
        dto.setReferenceNumber(collection.getReferenceNumber());
        dto.setCollectedAt(collection.getCollectedAt());
        dto.setCollectedAtDisplay(collection.getCollectedAt() == null ? null : DISPLAY_DATE_TIME.format(collection.getCollectedAt()));
        dto.setCollectedBy(collection.getCollectedBy());
        return dto;
    }

    private PortalTimetableDTO toTimetableDto(TimetableSlot slot) {
        PortalTimetableDTO dto = new PortalTimetableDTO();
        dto.setClassName(slot.getSection().getName());
        dto.setSubjectName(slot.getProgramSubject().getSubject().getName());
        dto.setDayOfWeek(slot.getDayOfWeek());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setRoom(slot.getRoom());
        return dto;
    }
}
