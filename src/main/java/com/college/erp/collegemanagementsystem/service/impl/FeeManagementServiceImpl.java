package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.dto.FeeCollectionDTO;
import com.college.erp.collegemanagementsystem.dto.FeeStructureDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.request.FeeCollectionRequest;
import com.college.erp.collegemanagementsystem.dto.request.FeeStructureRequest;
import com.college.erp.collegemanagementsystem.entity.*;
import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.enums.PaymentMethod;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.exception.DuplicateResourceException;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.repository.FeeCollectionRepository;
import com.college.erp.collegemanagementsystem.repository.FeeStructureRepository;
import com.college.erp.collegemanagementsystem.repository.StudentEnrollmentRepository;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.FeeManagementService;
import com.college.erp.collegemanagementsystem.service.TenantAccessService;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class FeeManagementServiceImpl implements FeeManagementService {
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final FeeStructureRepository structures;
    private final FeeCollectionRepository collections;
    private final StudentEnrollmentRepository enrollments;
    private final EntityManager entityManager;
    private final TenantAccessService tenantAccess;
    private final EntityChangeLogService changeLogs;

    public FeeManagementServiceImpl(FeeStructureRepository structures, FeeCollectionRepository collections,
                                    StudentEnrollmentRepository enrollments, EntityManager entityManager,
                                    TenantAccessService tenantAccess, EntityChangeLogService changeLogs) {
        this.structures = structures;
        this.collections = collections;
        this.enrollments = enrollments;
        this.entityManager = entityManager;
        this.tenantAccess = tenantAccess;
        this.changeLogs = changeLogs;
    }

    @Override
    @Transactional(readOnly = true)
    public PagablePage<FeeStructureDTO> searchStructures(String q, Status status, Integer page, Integer size, Long requestedTenantId) {
        Long tenantId = tenantAccess.resolve(requestedTenantId, false);
        Specification<FeeStructure> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (tenantId != null) predicate = cb.and(predicate, cb.equal(root.get("tenant").get("id"), tenantId));
            if (status != null) predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                predicate = cb.and(predicate, cb.or(cb.like(cb.lower(root.get("code")), term), cb.like(cb.lower(root.get("name")), term),
                        cb.like(cb.lower(root.get("program").get("name")), term), cb.like(cb.lower(root.get("academicYear").get("name")), term),
                        cb.like(cb.lower(root.get("semester").get("name")), term)));
            }
            return predicate;
        };
        var result = structures.findAll(spec, PageRequest.of(PagablePage.normalizePage(page) - 1,
                PagablePage.normalizeSize(size), Sort.by(Sort.Direction.DESC, "id")));
        return PagablePage.from(result.map(this::toStructureDto));
    }

    @Override
    @Transactional(readOnly = true)
    public FeeStructureDTO getStructure(Long id, Long requestedTenantId) {
        return toStructureDto(findStructure(id, tenantAccess.resolve(requestedTenantId, true)));
    }

    @Override
    public FeeStructureDTO createStructure(FeeStructureRequest request, Long requestedTenantId) {
        validateStructureRequest(request);
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        String code = request.getCode().trim().toUpperCase(Locale.ROOT);
        if (structures.existsByTenant_IdAndCodeIgnoreCase(tenantId, code))
            throw new DuplicateResourceException("A fee structure with this code already exists in the tenant.");
        FeeStructure structure = new FeeStructure();
        structure.setTenant(entityManager.getReference(Tenant.class, tenantId));
        apply(structure, request, tenantId);
        FeeStructure saved = structures.save(structure);
        changeLogs.logChange("FeeStructure", saved.getId(), EntityChangeAction.CREATED, "code", null, saved.getCode(), "Fee structure created.");
        changeLogs.logChange("FeeStructure", saved.getId(), EntityChangeAction.CREATED, "amount", null, saved.getAmount(), "Fee amount configured.");
        return toStructureDto(saved);
    }

    @Override
    public FeeStructureDTO updateStructure(Long id, FeeStructureRequest request, Long requestedTenantId, String remarks) {
        validateStructureRequest(request);
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        FeeStructure structure = structures.findForUpdate(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found in this tenant."));
        String code = request.getCode().trim().toUpperCase(Locale.ROOT);
        if (structures.existsByTenant_IdAndCodeIgnoreCaseAndIdNot(tenantId, code, id))
            throw new DuplicateResourceException("A fee structure with this code already exists in the tenant.");
        if (collections.existsByTenant_IdAndFeeStructure_Id(tenantId, id)
                && (!Objects.equals(structure.getAmount(), request.getAmount())
                || !Objects.equals(structure.getProgram().getId(), request.getProgramId())
                || !Objects.equals(structure.getAcademicYear().getId(), request.getAcademicYearId())
                || !Objects.equals(structure.getSemester().getId(), request.getSemesterId())))
            throw new ValidationException("The fee amount and academic scope cannot change after a payment has been collected.");
        FeeStructureDTO before = toStructureDto(structure);
        apply(structure, request, tenantId);
        FeeStructure saved = structures.save(structure);
        String note = clean(remarks, "Fee structure updated.");
        logDiff(saved, "code", before.getCode(), saved.getCode(), note);
        logDiff(saved, "name", before.getName(), saved.getName(), note);
        logDiff(saved, "amount", before.getAmount(), saved.getAmount(), note);
        logDiff(saved, "programId", before.getProgramId(), saved.getProgram().getId(), note);
        logDiff(saved, "academicYearId", before.getAcademicYearId(), saved.getAcademicYear().getId(), note);
        logDiff(saved, "semesterId", before.getSemesterId(), saved.getSemester().getId(), note);
        logDiff(saved, "description", before.getDescription(), saved.getDescription(), note);
        logDiff(saved, "dueDate", before.getDueDate(), saved.getDueDate(), note);
        logDiff(saved, "status", before.getStatus(), saved.getStatus(), note);
        return toStructureDto(saved);
    }

    @Override
    public FeeStructureDTO changeStructureStatus(Long id, Status status, Long requestedTenantId, String remarks) {
        if (status == null) throw new ValidationException("Fee structure status is required.");
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        FeeStructure structure = structures.findForUpdate(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found in this tenant."));
        Status before = structure.getStatus();
        structure.setStatus(status);
        FeeStructure saved = structures.save(structure);
        if (before != status) changeLogs.logChange("FeeStructure", saved.getId(), EntityChangeAction.STATUS_CHANGED,
                "status", before, status, clean(remarks, "Fee structure status changed."));
        return toStructureDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagablePage<FeeCollectionDTO> searchCollections(String q, Integer page, Integer size, Long requestedTenantId) {
        Long tenantId = tenantAccess.resolve(requestedTenantId, false);
        Specification<FeeCollection> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (tenantId != null) predicate = cb.and(predicate, cb.equal(root.get("tenant").get("id"), tenantId));
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                var student = root.join("enrollment").join("student");
                predicate = cb.and(predicate, cb.or(cb.like(cb.lower(root.get("receiptNumber")), term),
                        cb.like(cb.lower(student.get("registrationNumber")), term), cb.like(cb.lower(student.get("firstName")), term),
                        cb.like(cb.lower(student.get("lastName")), term), cb.like(cb.lower(root.get("feeStructure").get("name")), term)));
            }
            return predicate;
        };
        var result = collections.findAll(spec, PageRequest.of(PagablePage.normalizePage(page) - 1,
                PagablePage.normalizeSize(size), Sort.by(Sort.Direction.DESC, "id")));
        return PagablePage.from(result.map(this::toCollectionDto));
    }

    @Override
    @Transactional(readOnly = true)
    public FeeCollectionDTO getCollection(Long id, Long requestedTenantId) {
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        FeeCollection item = collections.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee collection not found in this tenant."));
        return toCollectionDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public FeeCollectionDTO getReceipt(String receiptNumber, Long requestedTenantId) {
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        if (receiptNumber == null || receiptNumber.isBlank()) throw new ValidationException("Receipt number is required.");
        FeeCollection item = collections.findByReceiptNumberIgnoreCaseAndTenant_Id(receiptNumber.trim(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found in this tenant."));
        return toCollectionDto(item);
    }

    @Override
    public FeeCollectionDTO collect(FeeCollectionRequest request, Long requestedTenantId) {
        validateCollectionRequest(request);
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        StudentEnrollment enrollment = enrollments.findByIdAndTenant_Id(request.getEnrollmentId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Student enrollment not found in this tenant."));
        FeeStructure structure = structures.findForUpdate(request.getFeeStructureId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found in this tenant."));
        validatePaymentScope(enrollment, structure, tenantId);
        if (enrollment.getStatus() != Status.ACTIVE || enrollment.getStudent().getStatus() != Status.ACTIVE)
            throw new ValidationException("Fees can only be collected for an active student enrollment.");
        if (structure.getStatus() != Status.ACTIVE) throw new ValidationException("Payments cannot be collected against an inactive fee structure.");
        if (request.getAmount().signum() <= 0 || request.getAmount().scale() > 2)
            throw new ValidationException("Payment amount must be positive and use at most two decimal places.");
        if ((request.getPaymentMethod() == PaymentMethod.BANK_TRANSFER || request.getPaymentMethod() == PaymentMethod.CHEQUE)
                && (request.getReferenceNumber() == null || request.getReferenceNumber().isBlank()))
            throw new ValidationException("A reference number is required for bank transfers and cheques.");

        BigDecimal alreadyPaid = collections.totalCollected(tenantId, enrollment.getId(), structure.getId());
        BigDecimal balance = structure.getAmount().subtract(alreadyPaid);
        if (request.getAmount().compareTo(balance) > 0)
            throw new ValidationException("Payment exceeds the outstanding balance of " + balance.toPlainString() + ".");

        FeeCollection collection = new FeeCollection();
        collection.setTenant(entityManager.getReference(Tenant.class, tenantId));
        collection.setEnrollment(enrollment);
        collection.setFeeStructure(structure);
        collection.setAmount(request.getAmount().setScale(2));
        collection.setPaymentMethod(request.getPaymentMethod());
        collection.setReferenceNumber(clean(request.getReferenceNumber(), null));
        collection.setCollectedAt(OffsetDateTime.now());
        collection.setCollectedBy(currentUsername());
        FeeCollection saved = collections.saveAndFlush(collection);
        saved.setReceiptNumber("RCP-" + structure.getTenant().getTenantCode().toUpperCase(Locale.ROOT) + "-"
                + saved.getCollectedAt().getYear() + "-" + String.format(Locale.ROOT, "%08d", saved.getId()));
        saved = collections.save(saved);
        changeLogs.logChange("FeeCollection", saved.getId(), EntityChangeAction.CREATED, "receiptNumber", null,
                saved.getReceiptNumber(), "Fee payment collected; receipt generated.");
        changeLogs.logChange("FeeCollection", saved.getId(), EntityChangeAction.CREATED, "amount", null,
                saved.getAmount(), "Fee payment collected.");
        changeLogs.logChange("FeeCollection", saved.getId(), EntityChangeAction.CREATED, "paymentMethod", null,
                saved.getPaymentMethod(), "Payment method recorded.");
        if (saved.getReferenceNumber() != null) changeLogs.logChange("FeeCollection", saved.getId(), EntityChangeAction.CREATED,
                "referenceNumber", null, saved.getReferenceNumber(), "Payment reference recorded.");
        return toCollectionDto(saved);
    }

    private void validateStructureRequest(FeeStructureRequest request) {
        if (request == null || request.getProgramId() == null || request.getAcademicYearId() == null || request.getSemesterId() == null
                || blank(request.getCode()) || blank(request.getName()) || request.getAmount() == null
                || request.getAmount().signum() <= 0 || request.getAmount().scale() > 2 || request.getStatus() == null)
            throw new ValidationException("Program, academic year, semester, code, name, positive amount, and status are required.");
    }

    private void validateCollectionRequest(FeeCollectionRequest request) {
        if (request == null || request.getEnrollmentId() == null || request.getFeeStructureId() == null
                || request.getAmount() == null || request.getPaymentMethod() == null)
            throw new ValidationException("Student enrollment, fee structure, payment amount, and payment method are required.");
    }

    private void apply(FeeStructure structure, FeeStructureRequest request, long tenantId) {
        Program program = entityManager.find(Program.class, request.getProgramId());
        AcademicYear academicYear = entityManager.find(AcademicYear.class, request.getAcademicYearId());
        Semester semester = entityManager.find(Semester.class, request.getSemesterId());
        if (program == null || academicYear == null || semester == null
                || !Objects.equals(program.getTenant().getId(), tenantId)
                || !Objects.equals(academicYear.getTenant().getId(), tenantId)
                || !Objects.equals(semester.getTenant().getId(), tenantId))
            throw new ResourceNotFoundException("Program, academic year, and semester must exist in the selected tenant.");
        if (program.getStatus() != Status.ACTIVE || academicYear.getStatus() != Status.ACTIVE || semester.getStatus() != Status.ACTIVE)
            throw new ValidationException("Fee structures require active program, academic year, and semester records.");
        structure.setProgram(program);
        structure.setAcademicYear(academicYear);
        structure.setSemester(semester);
        structure.setCode(request.getCode().trim().toUpperCase(Locale.ROOT));
        structure.setName(request.getName().trim());
        structure.setDescription(clean(request.getDescription(), null));
        structure.setAmount(request.getAmount().setScale(2));
        structure.setDueDate(request.getDueDate());
        structure.setStatus(request.getStatus());
    }

    private void validatePaymentScope(StudentEnrollment enrollment, FeeStructure structure, long tenantId) {
        if (!Objects.equals(enrollment.getTenant().getId(), tenantId)
                || !Objects.equals(enrollment.getStudent().getTenant().getId(), tenantId)
                || !Objects.equals(enrollment.getProgram().getTenant().getId(), tenantId)
                || !Objects.equals(enrollment.getAcademicYear().getTenant().getId(), tenantId)
                || !Objects.equals(enrollment.getSemester().getTenant().getId(), tenantId)
                || !Objects.equals(structure.getTenant().getId(), tenantId)
                || !Objects.equals(structure.getProgram().getTenant().getId(), tenantId)
                || !Objects.equals(structure.getAcademicYear().getTenant().getId(), tenantId)
                || !Objects.equals(structure.getSemester().getTenant().getId(), tenantId))
            throw new ValidationException("Enrollment and fee structure references must belong to the selected tenant.");
        if (!Objects.equals(enrollment.getProgram().getId(), structure.getProgram().getId())
                || !Objects.equals(enrollment.getAcademicYear().getId(), structure.getAcademicYear().getId())
                || !Objects.equals(enrollment.getSemester().getId(), structure.getSemester().getId()))
            throw new ValidationException("Fee structure must match the student's program, academic year, and semester.");
    }

    private FeeStructure findStructure(Long id, long tenantId) {
        return structures.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found in this tenant."));
    }

    private FeeStructureDTO toStructureDto(FeeStructure value) {
        FeeStructureDTO dto = new FeeStructureDTO();
        dto.setId(value.getId());
        dto.setTenantId(value.getTenant().getId());
        dto.setTenantName(value.getTenant().getTenantName());
        dto.setProgramId(value.getProgram().getId());
        dto.setProgramName(value.getProgram().getName());
        dto.setAcademicYearId(value.getAcademicYear().getId());
        dto.setAcademicYearName(value.getAcademicYear().getName());
        dto.setSemesterId(value.getSemester().getId());
        dto.setSemesterName(value.getSemester().getName());
        dto.setCode(value.getCode());
        dto.setName(value.getName());
        dto.setDescription(value.getDescription());
        dto.setAmount(value.getAmount());
        dto.setDueDate(value.getDueDate());
        dto.setStatus(value.getStatus());
        return dto;
    }

    private FeeCollectionDTO toCollectionDto(FeeCollection value) {
        FeeCollectionDTO dto = new FeeCollectionDTO();
        dto.setId(value.getId());
        dto.setTenantId(value.getTenant().getId());
        dto.setTenantName(value.getTenant().getTenantName());
        dto.setEnrollmentId(value.getEnrollment().getId());
        dto.setFeeStructureId(value.getFeeStructure().getId());
        dto.setFeeCode(value.getFeeStructure().getCode());
        dto.setFeeName(value.getFeeStructure().getName());
        dto.setRegistrationNumber(value.getEnrollment().getStudent().getRegistrationNumber());
        dto.setStudentName(value.getEnrollment().getStudent().getFirstName() + " " + value.getEnrollment().getStudent().getLastName());
        dto.setReceiptNumber(value.getReceiptNumber());
        dto.setAmount(value.getAmount());
        dto.setFeeAmount(value.getFeeStructure().getAmount());
        BigDecimal totalPaid = collections.totalCollected(value.getTenant().getId(), value.getEnrollment().getId(), value.getFeeStructure().getId());
        dto.setTotalPaid(totalPaid);
        dto.setBalance(value.getFeeStructure().getAmount().subtract(totalPaid).max(BigDecimal.ZERO));
        dto.setPaymentMethod(value.getPaymentMethod());
        dto.setReferenceNumber(value.getReferenceNumber());
        dto.setCollectedAt(value.getCollectedAt());
        dto.setCollectedAtDisplay(value.getCollectedAt() == null ? null : DISPLAY_DATE_TIME.format(value.getCollectedAt()));
        dto.setCollectedBy(value.getCollectedBy());
        return dto;
    }

    private void logDiff(FeeStructure value, String field, Object before, Object after, String remarks) {
        if (!Objects.equals(before, after)) changeLogs.logChange("FeeStructure", value.getId(), EntityChangeAction.UPDATED, field, before, after, remarks);
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUserPrincipal principal) return principal.getUsername();
        return "system";
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
    private String clean(String value, String fallback) { return blank(value) ? fallback : value.trim(); }
}
