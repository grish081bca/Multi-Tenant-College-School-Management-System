package com.college.erp.collegemanagementsystem.service;

import com.college.erp.collegemanagementsystem.entity.Tenant;
import com.college.erp.collegemanagementsystem.enums.UserType;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.security.AuthenticatedUserPrincipal;
import jakarta.persistence.EntityManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Resolves tenant scope from the authenticated principal; a query tenant is only an admin selector.
 */
@Component
public class TenantAccessService {
    private final EntityManager entityManager;

    public TenantAccessService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Long resolve(Long requestedTenantId, boolean required) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)) {
            throw new AccessDeniedException("An authenticated account is required.");
        }
        if (principal.getUserType() == UserType.SYSTEM_ADMIN || principal.getUserType() == UserType.SUPER_ADMIN) {
            if (requestedTenantId == null) {
                if (required) throw new ValidationException("Select a tenant to continue.");
                return null;
            }
            if (entityManager.find(Tenant.class, requestedTenantId) == null)
                throw new ResourceNotFoundException("Tenant not found.");
            return requestedTenantId;
        }
        if (principal.getTenantId() == null) throw new AccessDeniedException("A tenant account is required.");
        if (requestedTenantId != null && !Objects.equals(requestedTenantId, principal.getTenantId())) {
            throw new AccessDeniedException("You cannot access another tenant's records.");
        }
        return principal.getTenantId();
    }
}
