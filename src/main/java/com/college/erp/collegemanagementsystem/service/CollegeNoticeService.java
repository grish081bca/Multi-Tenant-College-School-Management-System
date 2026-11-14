package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.dto.CollegeNoticeDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.UserNotificationDTO;
import com.college.erp.collegemanagementsystem.dto.request.CollegeNoticeRequest;
import com.college.erp.collegemanagementsystem.enums.NoticeStatus;

import java.util.List;

public interface CollegeNoticeService {
    PagablePage<CollegeNoticeDTO> search(String q, NoticeStatus status, Integer page, Integer size, Long tenantId);
    CollegeNoticeDTO get(Long id, Long tenantId);
    CollegeNoticeDTO create(CollegeNoticeRequest request, Long tenantId);
    CollegeNoticeDTO update(Long id, CollegeNoticeRequest request, Long tenantId, String remarks);
    CollegeNoticeDTO publish(Long id, Long tenantId);
    CollegeNoticeDTO archive(Long id, Long tenantId);
    List<UserNotificationDTO> notifications();
    UserNotificationDTO markRead(Long notificationId);
}
