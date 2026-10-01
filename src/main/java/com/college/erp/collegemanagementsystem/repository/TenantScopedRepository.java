package com.college.erp.collegemanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.Optional;

@NoRepositoryBean
public interface TenantScopedRepository<T> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    Optional<T> findByIdAndTenant_Id(Long id, Long tenantId);
}
