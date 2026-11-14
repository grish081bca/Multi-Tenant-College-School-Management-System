package com.college.erp.collegemanagementsystem.repository;

import com.college.erp.collegemanagementsystem.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findAllByTenant_IdAndUser_IdOrderByNotice_PublishedAtDesc(Long tenantId, Long userId);
    Optional<UserNotification> findByIdAndTenant_IdAndUser_Id(Long id, Long tenantId, Long userId);
}
