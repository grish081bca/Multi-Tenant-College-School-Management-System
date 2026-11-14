package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.FeeStructure;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeeStructureRepository extends TenantScopedRepository<FeeStructure> {
    boolean existsByTenant_IdAndCodeIgnoreCase(Long tenantId, String code);
    boolean existsByTenant_IdAndCodeIgnoreCaseAndIdNot(Long tenantId, String code, Long id);
    List<FeeStructure> findAllByTenant_IdAndProgram_IdAndAcademicYear_IdAndSemester_IdAndStatusOrderByIdDesc(
            Long tenantId, Long programId, Long academicYearId, Long semesterId, com.college.erp.collegemanagementsystem.enums.Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FeeStructure f where f.id = :id and f.tenant.id = :tenantId")
    Optional<FeeStructure> findForUpdate(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
