package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.dto.CollegeNoticeDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.UserNotificationDTO;
import com.college.erp.collegemanagementsystem.dto.request.CollegeNoticeRequest;
import com.college.erp.collegemanagementsystem.entity.CollegeNotice;
import com.college.erp.collegemanagementsystem.entity.Tenant;
import com.college.erp.collegemanagementsystem.entity.User;
import com.college.erp.collegemanagementsystem.entity.UserNotification;
import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.enums.NoticeAudience;
import com.college.erp.collegemanagementsystem.enums.NoticeStatus;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.enums.UserStatus;
import com.college.erp.collegemanagementsystem.enums.UserType;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.repository.CollegeNoticeRepository;
import com.college.erp.collegemanagementsystem.repository.UserNotificationRepository;
import com.college.erp.collegemanagementsystem.repository.UserRepository;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import com.college.erp.collegemanagementsystem.service.CollegeNoticeService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.TenantAccessService;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class CollegeNoticeServiceImpl implements CollegeNoticeService {
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final CollegeNoticeRepository notices;
    private final UserNotificationRepository notifications;
    private final UserRepository users;
    private final EntityManager entityManager;
    private final TenantAccessService tenantAccess;
    private final EntityChangeLogService changeLogs;

    public CollegeNoticeServiceImpl(CollegeNoticeRepository notices, UserNotificationRepository notifications,
                                   UserRepository users, EntityManager entityManager, TenantAccessService tenantAccess,
                                   EntityChangeLogService changeLogs) {
        this.notices = notices;
        this.notifications = notifications;
        this.users = users;
        this.entityManager = entityManager;
        this.tenantAccess = tenantAccess;
        this.changeLogs = changeLogs;
    }

    @Override
    @Transactional(readOnly = true)
    public PagablePage<CollegeNoticeDTO> search(String q, NoticeStatus status, Integer page, Integer size, Long requestedTenantId) {
        Long tenantId = tenantAccess.resolve(requestedTenantId, false);
        Specification<CollegeNotice> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (tenantId != null) predicate = cb.and(predicate, cb.equal(root.get("tenant").get("id"), tenantId));
            if (status != null) predicate = cb.and(predicate, cb.equal(root.get("noticeStatus"), status));
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                predicate = cb.and(predicate, cb.or(cb.like(cb.lower(root.get("code")), term), cb.like(cb.lower(root.get("name")), term),
                        cb.like(cb.lower(root.get("body")), term)));
            }
            return predicate;
        };
        var result = notices.findAll(spec, PageRequest.of(PagablePage.normalizePage(page) - 1,
                PagablePage.normalizeSize(size), Sort.by(Sort.Direction.DESC, "id")));
        return PagablePage.from(result.map(this::toNoticeDto));
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeNoticeDTO get(Long id, Long requestedTenantId) {
        return toNoticeDto(find(id, tenantAccess.resolve(requestedTenantId, true)));
    }

    @Override
    public CollegeNoticeDTO create(CollegeNoticeRequest request, Long requestedTenantId) {
        validate(request);
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        CollegeNotice notice = new CollegeNotice();
        notice.setTenant(entityManager.getReference(Tenant.class, tenantId));
        notice.setCode("NOTICE-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(Locale.ROOT));
        apply(notice, request);
        notice.setStatus(Status.ACTIVE);
        notice.setNoticeStatus(NoticeStatus.DRAFT);
        CollegeNotice saved = notices.save(notice);
        changeLogs.logChange("CollegeNotice", saved.getId(), EntityChangeAction.CREATED, "title", null, saved.getName(), "Notice created as a draft.");
        changeLogs.logChange("CollegeNotice", saved.getId(), EntityChangeAction.CREATED, "body", null, saved.getBody(), "Notice message saved.");
        changeLogs.logChange("CollegeNotice", saved.getId(), EntityChangeAction.CREATED, "audience", null, saved.getAudience(), "Notice audience selected.");
        return toNoticeDto(saved);
    }

    @Override
    public CollegeNoticeDTO update(Long id, CollegeNoticeRequest request, Long requestedTenantId, String remarks) {
        validate(request);
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        CollegeNotice notice = notices.findForUpdate(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found in this tenant."));
        if (notice.getNoticeStatus() != NoticeStatus.DRAFT)
            throw new ValidationException("Only draft notices can be edited.");
        String oldTitle = notice.getName();
        String oldBody = notice.getBody();
        NoticeAudience oldAudience = notice.getAudience();
        apply(notice, request);
        CollegeNotice saved = notices.save(notice);
        String note = clean(remarks, "Draft notice updated.");
        logDiff(saved, "title", oldTitle, saved.getName(), note);
        logDiff(saved, "body", oldBody, saved.getBody(), note);
        logDiff(saved, "audience", oldAudience, saved.getAudience(), note);
        return toNoticeDto(saved);
    }

    @Override
    public CollegeNoticeDTO publish(Long id, Long requestedTenantId) {
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        CollegeNotice notice = notices.findForUpdate(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found in this tenant."));
        if (notice.getNoticeStatus() != NoticeStatus.DRAFT)
            throw new ValidationException("Only draft notices can be published.");
        notice.setNoticeStatus(NoticeStatus.PUBLISHED);
        notice.setPublishedAt(OffsetDateTime.now());
        CollegeNotice saved = notices.save(notice);
        List<UserType> recipients = switch (notice.getAudience()) {
            case ALL -> List.of(UserType.STUDENT, UserType.TEACHER);
            case STUDENT -> List.of(UserType.STUDENT);
            case TEACHER -> List.of(UserType.TEACHER);
        };
        for (User user : users.findAllByTenant_IdAndUserTypeInAndStatusAndEnabledTrue(
                notice.getTenant().getId(), recipients, UserStatus.ACTIVE)) {
            UserNotification notification = new UserNotification();
            notification.setTenant(notice.getTenant());
            notification.setNotice(saved);
            notification.setUser(user);
            notifications.save(notification);
        }
        changeLogs.logChange("CollegeNotice", saved.getId(), EntityChangeAction.STATUS_CHANGED,
                "noticeStatus", NoticeStatus.DRAFT, NoticeStatus.PUBLISHED, "Notice published and notifications generated.");
        return toNoticeDto(saved);
    }

    @Override
    public CollegeNoticeDTO archive(Long id, Long requestedTenantId) {
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        CollegeNotice notice = notices.findForUpdate(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found in this tenant."));
        if (notice.getNoticeStatus() != NoticeStatus.PUBLISHED)
            throw new ValidationException("Only published notices can be archived.");
        notice.setNoticeStatus(NoticeStatus.ARCHIVED);
        CollegeNotice saved = notices.save(notice);
        changeLogs.logChange("CollegeNotice", saved.getId(), EntityChangeAction.STATUS_CHANGED,
                "noticeStatus", NoticeStatus.PUBLISHED, NoticeStatus.ARCHIVED, "Notice archived.");
        return toNoticeDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserNotificationDTO> notifications() {
        AuthenticatedUserPrincipal principal = portalPrincipal();
        return notifications.findAllByTenant_IdAndUser_IdOrderByNotice_PublishedAtDesc(principal.getTenantId(), principal.getUserId())
                .stream().map(this::toNotificationDto).toList();
    }

    @Override
    public UserNotificationDTO markRead(Long notificationId) {
        AuthenticatedUserPrincipal principal = portalPrincipal();
        UserNotification notification = notifications.findByIdAndTenant_IdAndUser_Id(notificationId,
                        principal.getTenantId(), principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found for this account."));
        if (notification.getReadAt() == null) {
            notification.setReadAt(OffsetDateTime.now());
            notifications.save(notification);
            changeLogs.logChange("UserNotification", notification.getId(), EntityChangeAction.STATUS_CHANGED,
                    "readAt", null, notification.getReadAt(), "Notification marked as read.");
        }
        return toNotificationDto(notification);
    }

    private void validate(CollegeNoticeRequest request) {
        if (request == null || blank(request.getTitle()) || blank(request.getBody()) || request.getAudience() == null)
            throw new ValidationException("Notice title, message, and audience are required.");
    }

    private void apply(CollegeNotice notice, CollegeNoticeRequest request) {
        notice.setName(request.getTitle().trim());
        notice.setBody(request.getBody().trim());
        notice.setAudience(request.getAudience());
        notice.setDescription(clean(request.getDescription(), null));
    }

    private CollegeNotice find(Long id, long tenantId) {
        return notices.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found in this tenant."));
    }

    private AuthenticatedUserPrincipal portalPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal
                && principal.getTenantId() != null
                && (principal.getUserType() == UserType.STUDENT || principal.getUserType() == UserType.TEACHER)) return principal;
        throw new AccessDeniedException("A tenant student or teacher account is required.");
    }

    private CollegeNoticeDTO toNoticeDto(CollegeNotice notice) {
        CollegeNoticeDTO dto = new CollegeNoticeDTO();
        dto.setId(notice.getId());
        dto.setTenantId(notice.getTenant().getId());
        dto.setTenantName(notice.getTenant().getTenantName());
        dto.setCode(notice.getCode());
        dto.setTitle(notice.getName());
        dto.setDescription(notice.getDescription());
        dto.setBody(notice.getBody());
        dto.setAudience(notice.getAudience());
        dto.setStatus(notice.getNoticeStatus());
        dto.setPublishedAt(notice.getPublishedAt());
        dto.setPublishedAtDisplay(notice.getPublishedAt() == null ? null : DISPLAY_DATE_TIME.format(notice.getPublishedAt()));
        return dto;
    }

    private UserNotificationDTO toNotificationDto(UserNotification notification) {
        UserNotificationDTO dto = new UserNotificationDTO();
        dto.setId(notification.getId());
        dto.setNoticeId(notification.getNotice().getId());
        dto.setTitle(notification.getNotice().getName());
        dto.setBody(notification.getNotice().getBody());
        dto.setPublishedAt(notification.getNotice().getPublishedAt());
        dto.setPublishedAtDisplay(notification.getNotice().getPublishedAt() == null ? null : DISPLAY_DATE_TIME.format(notification.getNotice().getPublishedAt()));
        dto.setReadAt(notification.getReadAt());
        dto.setRead(notification.getReadAt() != null);
        return dto;
    }

    private void logDiff(CollegeNotice notice, String field, Object before, Object after, String remarks) {
        if (!java.util.Objects.equals(before, after)) changeLogs.logChange("CollegeNotice", notice.getId(), EntityChangeAction.UPDATED, field, before, after, remarks);
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
    private String clean(String value, String fallback) { return blank(value) ? fallback : value.trim(); }
}
