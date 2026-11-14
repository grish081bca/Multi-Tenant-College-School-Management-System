package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.FeeCollection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FeeCollectionRepository extends TenantScopedRepository<FeeCollection> {
    boolean existsByTenant_IdAndFeeStructure_Id(Long tenantId, Long feeStructureId);
    @Query("select coalesce(sum(c.amount), 0) from FeeCollection c where c.tenant.id = :tenantId and c.enrollment.id = :enrollmentId and c.feeStructure.id = :feeStructureId")
    BigDecimal totalCollected(@Param("tenantId") Long tenantId, @Param("enrollmentId") Long enrollmentId,
                              @Param("feeStructureId") Long feeStructureId);

    List<FeeCollection> findAllByTenant_IdAndEnrollment_Student_IdOrderByCollectedAtDesc(Long tenantId, Long studentId);
    Optional<FeeCollection> findByReceiptNumberIgnoreCaseAndTenant_Id(String receiptNumber, Long tenantId);
}
