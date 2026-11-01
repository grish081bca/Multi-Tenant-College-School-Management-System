package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.AdmissionApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AdmissionApplicationRepository extends JpaRepository<AdmissionApplication, Long>, JpaSpecificationExecutor<AdmissionApplication> {
    Optional<AdmissionApplication> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);
}
