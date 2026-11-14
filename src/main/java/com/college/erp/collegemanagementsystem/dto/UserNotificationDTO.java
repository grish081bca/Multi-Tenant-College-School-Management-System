package com.college.erp.collegemanagementsystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class UserNotificationDTO {
    private Long id;
    private Long noticeId;
    private String title;
    private String body;
    private OffsetDateTime publishedAt;
    private String publishedAtDisplay;
    private OffsetDateTime readAt;
    private boolean read;
}
