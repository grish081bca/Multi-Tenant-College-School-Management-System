package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);
}
