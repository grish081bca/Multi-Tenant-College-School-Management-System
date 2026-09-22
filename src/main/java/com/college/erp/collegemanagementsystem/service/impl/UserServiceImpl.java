package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.UserDTO;
import com.college.erp.collegemanagementsystem.entity.Tenant;
import com.college.erp.collegemanagementsystem.entity.User;
import com.college.erp.collegemanagementsystem.entity.UserTemplate;
import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.enums.UserStatus;
import com.college.erp.collegemanagementsystem.enums.UserType;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.repository.TenantRepository;
import com.college.erp.collegemanagementsystem.repository.UserRepository;
import com.college.erp.collegemanagementsystem.repository.UserTemplateRepository;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.UserService;
import com.college.erp.collegemanagementsystem.util.ConvertUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.criteria.JoinType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * @author grish
 *
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {
    private static final String USER_ENTITY = "User";
    private static final String CREATE_REMARKS = "User created.";
    private static final String UPDATE_REMARKS = "User updated.";
    private static final String STATUS_REMARKS = "User status changed.";

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserTemplateRepository userTemplateRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityChangeLogService entityChangeLogService;

    public UserServiceImpl(UserRepository userRepository,
                           TenantRepository tenantRepository,
                           UserTemplateRepository userTemplateRepository,
                           PasswordEncoder passwordEncoder,
                           EntityChangeLogService entityChangeLogService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.userTemplateRepository = userTemplateRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityChangeLogService = entityChangeLogService;
    }
    @Override
    @Transactional(readOnly = true)
    public PagablePage<UserDTO> findPage(String q, UserStatus status, Integer page, Integer size) {
        return findPage(q, null, null, null, null, null, null, null, null, null, status, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PagablePage<UserDTO> findPage(String q,
                                         String username,
                                         String fullName,
                                         String email,
                                         String phoneNumber,
                                         Long tenantId,
                                         UserType userType,
                                         Boolean enabled,
                                         LocalDate fromDate,
                                         LocalDate toDate,
                                         UserStatus status,
                                         Integer page,
                                         Integer size) {
        PageRequest pageRequest = PageRequest.of(PagablePage.normalizePage(page) - 1, PagablePage.normalizeSize(size), Sort.by(Sort.Direction.DESC, "id"));
        Specification<User> specification = (root, query, builder) -> {
            var predicate = builder.conjunction();
            var tenantJoin = root.join("tenant", JoinType.LEFT);
            var userTemplateJoin = root.join("userTemplate", JoinType.LEFT);
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim().toLowerCase() + "%";
                predicate = builder.and(predicate, builder.or(
                        builder.like(root.get("id").as(String.class), term),
                        builder.like(root.get("createdAt").as(String.class), term),
                        builder.like(builder.lower(root.get("username")), term),
                        builder.like(builder.lower(root.get("firstName")), term),
                        builder.like(builder.lower(root.get("middleName")), term),
                        builder.like(builder.lower(root.get("lastName")), term),
                        builder.like(builder.lower(root.get("email")), term),
                        builder.like(builder.lower(root.get("phoneNumber")), term),
                        builder.like(builder.lower(tenantJoin.get("tenantName")), term),
                        builder.like(builder.lower(tenantJoin.get("tenantCode")), term),
                        builder.like(builder.lower(userTemplateJoin.get("userType").as(String.class)), term),
                        builder.like(builder.lower(root.get("userType").as(String.class)), term),
                        builder.like(builder.lower(root.get("status").as(String.class)), term)
                ));
                if ("true".equals(q.trim().toLowerCase()) || "false".equals(q.trim().toLowerCase())) {
                    predicate = builder.or(predicate, builder.equal(root.get("enabled"), Boolean.valueOf(q.trim())));
                }
            }
            if (username != null && !username.isBlank()) {
                predicate = builder.and(predicate, builder.equal(builder.lower(root.get("username")), username.trim().toLowerCase()));
            }
            if (fullName != null && !fullName.isBlank()) {
                String term = "%" + fullName.trim().toLowerCase() + "%";
                predicate = builder.and(predicate, builder.or(
                        builder.like(builder.lower(root.get("firstName")), term),
                        builder.like(builder.lower(root.get("middleName")), term),
                        builder.like(builder.lower(root.get("lastName")), term),
                        builder.like(builder.lower(builder.concat(builder.concat(root.get("firstName"), " "), root.get("lastName"))), term)
                ));
            }
            if (email != null && !email.isBlank()) {
                predicate = builder.and(predicate, builder.like(builder.lower(root.get("email")), "%" + email.trim().toLowerCase() + "%"));
            }
            if (phoneNumber != null && !phoneNumber.isBlank()) {
                predicate = builder.and(predicate, builder.like(builder.lower(root.get("phoneNumber")), "%" + phoneNumber.trim().toLowerCase() + "%"));
            }
            if (tenantId != null) {
                predicate = builder.and(predicate, builder.equal(tenantJoin.get("id"), tenantId));
            }
            if (userType != null) {
                predicate = builder.and(predicate, builder.equal(root.get("userType"), userType));
            }
            if (enabled != null) {
                predicate = builder.and(predicate, builder.equal(root.get("enabled"), enabled));
            }
            if (fromDate != null) {
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("createdAt"), toOffsetDateTime(fromDate)));
            }
            if (toDate != null) {
                predicate = builder.and(predicate, builder.lessThan(root.get("createdAt"), toOffsetDateTime(toDate.plusDays(1))));
            }
            if (status != null) {
                predicate = builder.and(predicate, builder.equal(root.get("status"), status));
            }
            return predicate;
        };
        Page<User> entities = userRepository.findAll(specification, pageRequest);
        return PagablePage.from(entities.map(ConvertUtils::toUserDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUsernames() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "username")).stream()
                .map(User::getUsername)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();
    }
    @Override
    public UserDTO create(UserDTO userDto, Long tenantId, Long userTemplateId) {
        if (userDto == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setMiddleName(userDto.getMiddleName());
        user.setLastName(userDto.getLastName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setUserType(userDto.getUserType());
        user.setStatus(userDto.getStatus());
        user.setEnabled(userDto.isEnabled());

        UserType type = user.getUserType();
        // tenant rules: SUPER_ADMIN and SYSTEM_ADMIN may have null tenant, others require tenant
        if (type == UserType.SUPER_ADMIN || type == UserType.SYSTEM_ADMIN) {
            user.setTenant(null);
        } else {
            if (tenantId == null) {
                throw new IllegalArgumentException("Tenant is required for user type: " + type);
            }
            Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
            user.setTenant(tenant);
        }
        if (userTemplateId != null) {
            UserTemplate ut = userTemplateRepository.findById(userTemplateId).orElse(null);
            user.setUserTemplate(ut);
        }
        // password handling
        if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode("changeme"));
        } else {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        User saved = userRepository.save(user);
        logCreatedUser(saved);
        return ConvertUtils.toUserDTO(saved);
    }
    @Override
    public UserDTO update(Long id, UserDTO userDto, Long tenantId, Long userTemplateId, String remarks) {
        if (id == null) {
            throw new IllegalArgumentException("User id is required");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserDTO before = ConvertUtils.toUserDTO(user);
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setMiddleName(userDto.getMiddleName());
        user.setLastName(userDto.getLastName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setUserType(userDto.getUserType());
        user.setStatus(userDto.getStatus());
        user.setEnabled(userDto.isEnabled());
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        UserType type = user.getUserType();
        if (type == UserType.SUPER_ADMIN || type == UserType.SYSTEM_ADMIN) {
            user.setTenant(null);
        } else {
            if (tenantId == null) {
                throw new IllegalArgumentException("Tenant is required for user type: " + type);
            }
            Tenant t = tenantRepository.findById(tenantId).orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
            user.setTenant(t);
        }
        if (userTemplateId != null) {
            UserTemplate ut = userTemplateRepository.findById(userTemplateId).orElse(null);
            user.setUserTemplate(ut);
        } else {
            user.setUserTemplate(null);
        }
        User saved = userRepository.save(user);
        logUpdatedUser(before, ConvertUtils.toUserDTO(saved), effectiveRemarks(remarks, UPDATE_REMARKS));
        return ConvertUtils.toUserDTO(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findById(Long id) {
        if (id == null) return Optional.empty();
        return userRepository.findById(id).map(ConvertUtils::toUserDTO);
    }
    @Override
    public UserDTO changeStatus(Long id, UserStatus status, String remarks) {
        if (id == null) throw new IllegalArgumentException("User id is required");
        if (status == null) throw new IllegalArgumentException("Status is required");
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserStatus oldStatus = user.getStatus();
        user.setStatus(status);
        User saved = userRepository.save(user);
        entityChangeLogService.logChange(USER_ENTITY, user.getId(), EntityChangeAction.STATUS_CHANGED, "status", oldStatus, status, effectiveRemarks(remarks, STATUS_REMARKS));
        return ConvertUtils.toUserDTO(saved);
    }

    private void logCreatedUser(User user) {
        UserDTO saved = ConvertUtils.toUserDTO(user);
        logField(saved.getId(), EntityChangeAction.CREATED, "username", null, saved.getUsername(), CREATE_REMARKS);
        logField(saved.getId(), EntityChangeAction.CREATED, "email", null, saved.getEmail(), CREATE_REMARKS);
        logField(saved.getId(), EntityChangeAction.CREATED, "phoneNumber", null, saved.getPhoneNumber(), CREATE_REMARKS);
        logField(saved.getId(), EntityChangeAction.CREATED, "firstName", null, saved.getFirstName(), CREATE_REMARKS);
        logField(saved.getId(), EntityChangeAction.CREATED, "middleName", null, saved.getMiddleName(), CREATE_REMARKS);
        logField(saved.getId(), EntityChangeAction.CREATED, "lastName", null, saved.getLastName(), CREATE_REMARKS);
        logField(saved.getId(), EntityChangeAction.CREATED, "tenant", null, saved.getTenantName(), CREATE_REMARKS);
        logField(saved.getId(), EntityChangeAction.CREATED, "userTemplate", null, saved.getUserTemplateName(), CREATE_REMARKS);
        logField(saved.getId(), EntityChangeAction.CREATED, "userType", null, saved.getUserType(), CREATE_REMARKS);
        logField(saved.getId(), EntityChangeAction.CREATED, "enabled", null, saved.isEnabled() ? "Enabled" : "Disabled", CREATE_REMARKS);
        logField(saved.getId(), EntityChangeAction.CREATED, "status", null, saved.getStatus(), CREATE_REMARKS);
    }

    private void logUpdatedUser(UserDTO before, UserDTO after, String remarks) {
        logField(after.getId(), EntityChangeAction.UPDATED, "username", before.getUsername(), after.getUsername(), remarks);
        logField(after.getId(), EntityChangeAction.UPDATED, "email", before.getEmail(), after.getEmail(), remarks);
        logField(after.getId(), EntityChangeAction.UPDATED, "phoneNumber", before.getPhoneNumber(), after.getPhoneNumber(), remarks);
        logField(after.getId(), EntityChangeAction.UPDATED, "firstName", before.getFirstName(), after.getFirstName(), remarks);
        logField(after.getId(), EntityChangeAction.UPDATED, "middleName", before.getMiddleName(), after.getMiddleName(), remarks);
        logField(after.getId(), EntityChangeAction.UPDATED, "lastName", before.getLastName(), after.getLastName(), remarks);
        logField(after.getId(), EntityChangeAction.UPDATED, "tenant", before.getTenantName(), after.getTenantName(), remarks);
        logField(after.getId(), EntityChangeAction.UPDATED, "userTemplate", before.getUserTemplateName(), after.getUserTemplateName(), remarks);
        logField(after.getId(), EntityChangeAction.UPDATED, "userType", before.getUserType(), after.getUserType(), remarks);
        logField(after.getId(), EntityChangeAction.UPDATED, "enabled", before.isEnabled() ? "Enabled" : "Disabled", after.isEnabled() ? "Enabled" : "Disabled", remarks);
        logField(after.getId(), EntityChangeAction.UPDATED, "status", before.getStatus(), after.getStatus(), remarks);
    }

    private void logField(Long userId, EntityChangeAction action, String fieldName, Object oldValue, Object newValue, String remarks) {
        entityChangeLogService.logChange(USER_ENTITY, userId, action, fieldName, oldValue, newValue, remarks);
    }

    private String effectiveRemarks(String remarks, String fallback) {
        String normalized = ConvertUtils.normalizeText(remarks);
        return normalized == null ? fallback : normalized;
    }

    private OffsetDateTime toOffsetDateTime(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
