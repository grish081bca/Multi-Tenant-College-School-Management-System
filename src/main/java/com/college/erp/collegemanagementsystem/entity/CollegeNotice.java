package com.college.erp.collegemanagementsystem.entity;

import com.college.erp.collegemanagementsystem.enums.NoticeAudience;
import com.college.erp.collegemanagementsystem.enums.NoticeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "college_notices", uniqueConstraints =
        @UniqueConstraint(name = "uk_college_notice_tenant_code", columnNames = {"tenant_id", "code"}))
public class CollegeNotice extends TenantOwnedEntity {
    @Column(name = "body", nullable = false, length = 8000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "audience", nullable = false, length = 20)
    private NoticeAudience audience;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_status", nullable = false, length = 20)
    private NoticeStatus noticeStatus = NoticeStatus.DRAFT;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

}
