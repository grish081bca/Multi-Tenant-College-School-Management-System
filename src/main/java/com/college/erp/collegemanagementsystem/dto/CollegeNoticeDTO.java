package com.college.erp.collegemanagementsystem.dto;

import com.college.erp.collegemanagementsystem.enums.NoticeAudience;
import com.college.erp.collegemanagementsystem.enums.NoticeStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class CollegeNoticeDTO {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private String code;
    private String title;
    private String description;
    private String body;
    private NoticeAudience audience;
    private NoticeStatus status;
    private OffsetDateTime publishedAt;
    private String publishedAtDisplay;
}
