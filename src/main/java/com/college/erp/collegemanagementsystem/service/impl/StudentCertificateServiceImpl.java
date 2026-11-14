package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.StudentCertificateDTO;
import com.college.erp.collegemanagementsystem.dto.request.StudentCertificateRequest;
import com.college.erp.collegemanagementsystem.entity.StudentCertificate;
import com.college.erp.collegemanagementsystem.entity.StudentEnrollment;
import com.college.erp.collegemanagementsystem.entity.Tenant;
import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.exception.DuplicateResourceException;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.repository.StudentCertificateRepository;
import com.college.erp.collegemanagementsystem.repository.StudentEnrollmentRepository;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.StudentCertificateService;
import com.college.erp.collegemanagementsystem.service.TenantAccessService;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class StudentCertificateServiceImpl implements StudentCertificateService {
    private final StudentCertificateRepository certificates;
    private final StudentEnrollmentRepository enrollments;
    private final EntityManager entityManager;
    private final TenantAccessService tenantAccess;
    private final EntityChangeLogService changeLogs;

    public StudentCertificateServiceImpl(StudentCertificateRepository certificates, StudentEnrollmentRepository enrollments,
                                         EntityManager entityManager, TenantAccessService tenantAccess,
                                         EntityChangeLogService changeLogs) {
        this.certificates = certificates;
        this.enrollments = enrollments;
        this.entityManager = entityManager;
        this.tenantAccess = tenantAccess;
        this.changeLogs = changeLogs;
    }

    @Override
    @Transactional(readOnly = true)
    public PagablePage<StudentCertificateDTO> search(String q, Status status, Integer page, Integer size, Long requestedTenantId) {
        Long tenantId = tenantAccess.resolve(requestedTenantId, false);
        Specification<StudentCertificate> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (tenantId != null) predicate = cb.and(predicate, cb.equal(root.get("tenant").get("id"), tenantId));
            if (status != null) predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                var enrollment = root.join("enrollment");
                var student = enrollment.join("student");
                predicate = cb.and(predicate, cb.or(cb.like(cb.lower(root.get("code")), term),
                        cb.like(cb.lower(root.get("name")), term), cb.like(cb.lower(root.get("certificateType")), term),
                        cb.like(cb.lower(student.get("registrationNumber")), term),
                        cb.like(cb.lower(student.get("firstName")), term), cb.like(cb.lower(student.get("lastName")), term)));
            }
            return predicate;
        };
        var pageResult = certificates.findAll(spec, PageRequest.of(PagablePage.normalizePage(page) - 1,
                PagablePage.normalizeSize(size), Sort.by(Sort.Direction.DESC, "id")));
        return PagablePage.from(pageResult.map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentCertificateDTO get(Long id, Long requestedTenantId) {
        return toDto(find(id, tenantAccess.resolve(requestedTenantId, true)));
    }

    @Override
    public StudentCertificateDTO issue(StudentCertificateRequest request, Long requestedTenantId) {
        if (request == null || request.getEnrollmentId() == null || blank(request.getCertificateType()) || blank(request.getTitle()))
            throw new ValidationException("Student enrollment, certificate type, and title are required.");
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        StudentEnrollment enrollment = enrollments.findByIdAndTenant_Id(request.getEnrollmentId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Student enrollment not found in this tenant."));
        if (enrollment.getStatus() != Status.ACTIVE || enrollment.getStudent().getStatus() != Status.ACTIVE)
            throw new ValidationException("A certificate can only be issued for an active student enrollment.");
        if (enrollment.getProgram().getStatus() != Status.ACTIVE || enrollment.getAcademicYear().getStatus() != Status.ACTIVE
                || enrollment.getSemester().getStatus() != Status.ACTIVE || enrollment.getSection().getStatus() != Status.ACTIVE)
            throw new ValidationException("The enrollment's program, academic year, semester, and section must be active.");
        validateEnrollmentScope(enrollment, tenantId);
        String certificateType = request.getCertificateType().trim();
        if (certificates.findByTenant_IdAndEnrollment_IdAndCertificateTypeIgnoreCase(tenantId, enrollment.getId(), certificateType).isPresent())
            throw new DuplicateResourceException("A certificate of this type has already been issued for the enrollment.");

        StudentCertificate certificate = new StudentCertificate();
        certificate.setTenant(entityManager.getReference(Tenant.class, tenantId));
        certificate.setEnrollment(enrollment);
        certificate.setCertificateType(certificateType);
        certificate.setName(request.getTitle().trim());
        certificate.setDescription(clean(request.getDescription(), null));
        certificate.setIssuedOn(request.getIssuedOn() == null ? LocalDate.now() : request.getIssuedOn());
        certificate.setCode("CERT-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(Locale.ROOT));
        certificate.setStatus(Status.ACTIVE);
        StudentCertificate saved = certificates.save(certificate);
        changeLogs.logChange("StudentCertificate", saved.getId(), EntityChangeAction.CREATED, "certificateNumber", null,
                saved.getCode(), "Student certificate issued.");
        changeLogs.logChange("StudentCertificate", saved.getId(), EntityChangeAction.CREATED, "certificateType", null,
                saved.getCertificateType(), "Certificate type: " + saved.getCertificateType());
        return toDto(saved);
    }

    @Override
    public StudentCertificateDTO revoke(Long id, String reason, Long requestedTenantId) {
        if (blank(reason)) throw new ValidationException("A reason is required to revoke a certificate.");
        StudentCertificate certificate = find(id, tenantAccess.resolve(requestedTenantId, true));
        if (certificate.getStatus() != Status.ACTIVE) throw new ValidationException("Only an active certificate can be revoked.");
        certificate.setStatus(Status.INACTIVE);
        certificate.setRevocationReason(reason.trim());
        StudentCertificate saved = certificates.save(certificate);
        changeLogs.logChange("StudentCertificate", saved.getId(), EntityChangeAction.STATUS_CHANGED, "status",
                Status.ACTIVE, Status.INACTIVE, reason.trim());
        changeLogs.logChange("StudentCertificate", saved.getId(), EntityChangeAction.UPDATED, "revocationReason",
                null, reason.trim(), "Certificate revocation reason.");
        return toDto(saved);
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

    private StudentCertificate find(Long id, long tenantId) {
        return certificates.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Student certificate not found in this tenant."));
    }

    private StudentCertificateDTO toDto(StudentCertificate certificate) {
        StudentCertificateDTO dto = new StudentCertificateDTO();
        dto.setId(certificate.getId());
        dto.setTenantId(certificate.getTenant().getId());
        dto.setTenantName(certificate.getTenant().getTenantName());
        dto.setEnrollmentId(certificate.getEnrollment().getId());
        dto.setRegistrationNumber(certificate.getEnrollment().getStudent().getRegistrationNumber());
        dto.setStudentName(certificate.getEnrollment().getStudent().getFirstName() + " " + certificate.getEnrollment().getStudent().getLastName());
        dto.setCertificateNumber(certificate.getCode());
        dto.setCertificateType(certificate.getCertificateType());
        dto.setTitle(certificate.getName());
        dto.setIssuedOn(certificate.getIssuedOn());
        dto.setStatus(certificate.getStatus());
        dto.setRevocationReason(certificate.getRevocationReason());
        return dto;
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
    private String clean(String value, String fallback) { return blank(value) ? fallback : value.trim(); }
}
