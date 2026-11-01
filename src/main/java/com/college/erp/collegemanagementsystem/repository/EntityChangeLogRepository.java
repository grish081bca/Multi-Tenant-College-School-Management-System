package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.EntityChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author grish
 *
 */
public interface EntityChangeLogRepository extends JpaRepository<EntityChangeLog, Long>, JpaSpecificationExecutor<EntityChangeLog> {
    List<EntityChangeLog> findTop25ByEntityNameAndEntityIdOrderByCreatedAtDesc(String entityName, Long entityId);
}
