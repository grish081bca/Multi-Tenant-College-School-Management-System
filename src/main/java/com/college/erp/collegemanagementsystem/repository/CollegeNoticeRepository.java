package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.CollegeNotice;
import com.college.erp.collegemanagementsystem.enums.NoticeAudience;
import com.college.erp.collegemanagementsystem.enums.NoticeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CollegeNoticeRepository extends TenantScopedRepository<CollegeNotice> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from CollegeNotice n where n.id = :id and n.tenant.id = :tenantId")
    Optional<CollegeNotice> findForUpdate(@Param("id") Long id, @Param("tenantId") Long tenantId);

    Page<CollegeNotice> findAllByTenant_IdAndNoticeStatus(Long tenantId, NoticeStatus status, Pageable pageable);
    Page<CollegeNotice> findAllByTenant_IdAndAudienceInAndNoticeStatus(Long tenantId, java.util.Collection<NoticeAudience> audiences,
                                                                        NoticeStatus status, Pageable pageable);
}
