package com.college.erp.collegemanagementsystem.mapper.impl;

import java.util.List;

import com.college.erp.collegemanagementsystem.dto.TenantDTO;
import com.college.erp.collegemanagementsystem.util.ConvertUtils;
import com.college.erp.collegemanagementsystem.util.DateFormatUtils;
import org.springframework.stereotype.Component;
import com.college.erp.collegemanagementsystem.entity.Tenant;
import com.college.erp.collegemanagementsystem.dto.request.TenantCreateRequest;
import com.college.erp.collegemanagementsystem.dto.request.TenantUpdateRequest;
import com.college.erp.collegemanagementsystem.dto.response.TenantResponse;
import com.college.erp.collegemanagementsystem.enums.TenantStatus;
import com.college.erp.collegemanagementsystem.entity.City;
import com.college.erp.collegemanagementsystem.entity.Country;
import com.college.erp.collegemanagementsystem.entity.State;
import com.college.erp.collegemanagementsystem.validation.LocationValidationService;
import com.college.erp.collegemanagementsystem.mapper.TenantMapper;

/**
 * @author grish
 *
 */
@Component
public class TenantMapperImpl implements TenantMapper {
    private final LocationValidationService locationValidationService;

    public TenantMapperImpl(LocationValidationService locationValidationService) {
        this.locationValidationService = locationValidationService;
    }

    @Override
    public Tenant toEntity(TenantCreateRequest request) {
        Tenant tenant = new Tenant();
        tenant.setTenantName(ConvertUtils.normalizeText(request.getTenantName()));
        tenant.setContactEmail(ConvertUtils.normalizeText(request.getContactEmail()));
        tenant.setContactEmailSecondary(ConvertUtils.normalizeText(request.getContactEmailSecondary()));
        tenant.setContactPhone(ConvertUtils.normalizeText(request.getContactPhone()));
        tenant.setContactPhoneSecondary(ConvertUtils.normalizeText(request.getContactPhoneSecondary()));
        tenant.setAddressLine1(ConvertUtils.normalizeText(request.getAddressLine1()));
        tenant.setAddressLine2(ConvertUtils.normalizeText(request.getAddressLine2()));
        tenant.setPostalCode(ConvertUtils.normalizeText(request.getPostalCode()));
        tenant.setCountry(resolveCountry(request.getCountryId()));
        tenant.setState(resolveState(request.getStateId(), request.getCountryId()));
        tenant.setCity(resolveCity(request.getCityId(), request.getStateId()));
        tenant.setStatus(TenantStatus.ACTIVE);
        return tenant;
    }

    @Override
    public TenantDTO toDto(Tenant entity) {
        if (entity == null) {
            return null;
        }
        TenantDTO dto = new TenantDTO();
        dto.setId(entity.getId());
        if (entity.getCreatedAt() != null) {
            dto.setCreatedDate(DateFormatUtils.formatDateTime(entity.getCreatedAt()));
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedDate(DateFormatUtils.formatDateTime(entity.getUpdatedAt()));
        }
        // createdBy/updatedBy come from AuditableEntity
        if (entity.getCreatedBy() != null) {
            dto.setCreatedBy(entity.getCreatedBy());
        }
        if (entity.getUpdatedBy() != null) {
            dto.setUpdatedBy(entity.getUpdatedBy());
        }
        dto.setTenantName(entity.getTenantName());
        dto.setTenantCode(entity.getTenantCode());
        if (entity.getTenantType() != null) {
            dto.setTenantType(entity.getTenantType().name());
        }
        if (entity.getParentTenant() != null) {
            dto.setParentTenantId(entity.getParentTenant().getId());
            dto.setParentTenantCode(entity.getParentTenant().getTenantCode());
            dto.setParentTenantName(entity.getParentTenant().getTenantName());
        }
        dto.setContactEmail(entity.getContactEmail());
        if (entity.getContactEmailSecondary() != null) {
            dto.setContactEmailSecondary(entity.getContactEmailSecondary());
        }
        dto.setContactPhone(entity.getContactPhone());
        if (entity.getContactPhoneSecondary() != null) {
            dto.setContactPhoneSecondary(entity.getContactPhoneSecondary());
        }
        if (entity.getAddressLine1() != null) {
            dto.setAddressLine1(entity.getAddressLine1());
        }
        if (entity.getAddressLine2() != null) {
            dto.setAddressLine2(entity.getAddressLine2());
        }
        if (entity.getPostalCode() != null) {
            dto.setPostalCode(entity.getPostalCode());
        }
        if (entity.getCountry() != null) {
            dto.setCountryId(entity.getCountry().getId());
            dto.setCountryName(entity.getCountry().getName());
        }
        if (entity.getState() != null) {
            dto.setStateId(entity.getState().getId());
            dto.setStateName(entity.getState().getName());
        }
        if (entity.getCity() != null) {
            dto.setCityId(entity.getCity().getId());
            dto.setCityName(entity.getCity().getName());
        }
        dto.setStatus(entity.getStatus().name());
        return dto;
    }

    @Override
    public void updateEntity(TenantUpdateRequest request, Tenant entity) {
        if (request.getTenantName() != null && !request.getTenantName().trim().isEmpty()) {
            entity.setTenantName(ConvertUtils.normalizeText(request.getTenantName()));
        } else {
            entity.setTenantName(entity.getTenantName());
        }
        if (request.getContactEmail() != null && !request.getContactEmail().trim().isEmpty()) {
            entity.setContactEmail(ConvertUtils.normalizeText(request.getContactEmail()));
        } else {
            entity.setContactEmail(entity.getContactEmail());
        }
        if (request.getContactEmailSecondary() != null && !request.getContactEmailSecondary().trim().isEmpty()) {
            entity.setContactEmailSecondary(ConvertUtils.normalizeText(request.getContactEmailSecondary()));
        } else {
            entity.setContactEmailSecondary(entity.getContactEmailSecondary());
        }
        if (request.getContactPhone() != null && !request.getContactPhone().trim().isEmpty()) {
            entity.setContactPhone(ConvertUtils.normalizeText(request.getContactPhone()));
        }
        else {
            entity.setContactPhone(entity.getContactPhone());
        }
        if (request.getContactPhoneSecondary() != null && !request.getContactPhoneSecondary().trim().isEmpty()) {
            entity.setContactPhoneSecondary(ConvertUtils.normalizeText(request.getContactPhoneSecondary()));
        } else {
            entity.setContactPhoneSecondary(entity.getContactPhoneSecondary());
        }
        if(request.getAddressLine1() != null && !request.getAddressLine1().trim().isEmpty()) {
            entity.setAddressLine1(ConvertUtils.normalizeText(request.getAddressLine1()));
        } else {
            entity.setAddressLine1(entity.getAddressLine1());
        }
        if(request.getAddressLine2() != null && !request.getAddressLine2().trim().isEmpty()) {
            entity.setAddressLine2(ConvertUtils.normalizeText(request.getAddressLine2()));
        } else {
            entity.setAddressLine2(entity.getAddressLine2());
        }
        if (request.getPostalCode() != null && !request.getPostalCode().trim().isEmpty()) {
            entity.setPostalCode(ConvertUtils.normalizeText(request.getPostalCode()));
        } else {
            entity.setPostalCode(entity.getPostalCode());
        }
        if (request.getCountryId() != null) {
            entity.setCountry(resolveCountry(request.getCountryId()));
        }else {
            entity.setCountry(entity.getCountry());
        }
        if(request.getStateId() != null) {
            entity.setState(resolveState(request.getStateId(), request.getCountryId()));
        }else {
            entity.setState(entity.getState());
        }
        if (request.getCityId() != null) {
            entity.setCity(resolveCity(request.getCityId(), request.getStateId()));
        }else {
            entity.setCity(entity.getCity());
        }
    }

    @Override
    public List<TenantDTO> toTenantList(List<Tenant> entities) {
        return entities.stream().map(this::toDto).toList();
    }

    private Country resolveCountry(Long countryId) {
        return locationValidationService.getCountry(countryId);
    }

    private State resolveState(Long stateId, Long countryId) {
        return locationValidationService.getState(stateId, countryId);
    }

    private City resolveCity(Long cityId, Long stateId) {
        return locationValidationService.getCity(cityId, stateId);
    }
}
