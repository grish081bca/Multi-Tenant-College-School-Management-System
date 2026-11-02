package com.college.erp.collegemanagementsystem.service.impl;

import java.util.List;

import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.StateDTO;
import com.college.erp.collegemanagementsystem.dto.request.StateCreateRequest;
import com.college.erp.collegemanagementsystem.dto.request.StateUpdateRequest;
import com.college.erp.collegemanagementsystem.entity.Country;
import com.college.erp.collegemanagementsystem.entity.State;
import com.college.erp.collegemanagementsystem.exception.DuplicateResourceException;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.repository.StateRepository;
import com.college.erp.collegemanagementsystem.repository.TenantRepository;
import com.college.erp.collegemanagementsystem.repository.CityRepository;
import com.college.erp.collegemanagementsystem.service.StateService;
import com.college.erp.collegemanagementsystem.util.ConvertUtils;
import com.college.erp.collegemanagementsystem.util.DateFormatUtils;
import com.college.erp.collegemanagementsystem.validation.LocationValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author grish
 *
 */
@Service
@Transactional
public class StateServiceImpl implements StateService {

    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final TenantRepository tenantRepository;
    private final LocationValidationService locationValidationService;

    public StateServiceImpl(StateRepository stateRepository,
                            CityRepository cityRepository,
                            TenantRepository tenantRepository,
                            LocationValidationService locationValidationService) {
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.tenantRepository = tenantRepository;
        this.locationValidationService = locationValidationService;
    }

    @Override
    public StateDTO createState(StateCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request could not be empty.");
        }
        String name = normalizeName(request.getName());
        Country country = locationValidationService.getCountry(request.getCountryId());
        if (stateRepository.existsByNameIgnoreCaseAndCountry_Id(name, country.getId())) {
            throw new DuplicateResourceException("State name already exists in country: " + name);
        }
        State state = new State();
        state.setName(name);
        state.setCountry(country);
        return toDto(stateRepository.save(state));
    }

    @Override
    public StateDTO updateState(Long id, StateUpdateRequest request) {
        State state = getStateEntityById(id);
        if (request == null) {
            throw new IllegalArgumentException("Request could not be empty.");
        }
        Country targetCountry = request.getCountryId() != null ? locationValidationService.getCountry(request.getCountryId()) : state.getCountry();
        String targetName = request.getName() != null && !request.getName().trim().isEmpty()
                ? normalizeName(request.getName())
                : state.getName();
        if (stateRepository.existsByNameIgnoreCaseAndCountry_IdAndIdNot(targetName, targetCountry.getId(), id)) {
            throw new DuplicateResourceException("State name already exists in country: " + targetName);
        }
        state.setCountry(targetCountry);
        state.setName(targetName);
        return toDto(stateRepository.save(state));
    }

    @Override
    @Transactional(readOnly = true)
    public StateDTO getStateById(Long id) {
        return toDto(getStateEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StateDTO> getAllStates() {
        List<State> states = stateRepository.findAllByOrderByIdDesc();
        if (states.isEmpty()) {
            throw new ResourceNotFoundException("No states found.");
        }
        return states.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagablePage<StateDTO> getStatesPage(String search, Long countryId, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(PagablePage.normalizePage(page) - 1, PagablePage.normalizeSize(size), Sort.by(Sort.Direction.DESC, "id"));
        Specification<State> specification = (root, query, builder) -> {
            var predicate = builder.conjunction();
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicate = builder.and(predicate, builder.or(
                        builder.like(builder.lower(root.get("name")), term),
                        builder.like(builder.lower(root.join("country").get("name")), term)
                ));
            }
            if (countryId != null) {
                predicate = builder.and(predicate, builder.equal(root.get("country").get("id"), countryId));
            }
            return predicate;
        };
        return PagablePage.from(stateRepository.findAll(specification, pageRequest).map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StateDTO> getStatesByCountryId(Long countryId) {
        locationValidationService.getCountry(countryId);
        List<State> states = stateRepository.findAllByCountry_IdOrderByIdDesc(countryId);
        if (states.isEmpty()) {
            throw new ResourceNotFoundException("No states found for country.");
        }
        return states.stream().map(this::toDto).toList();
    }

    @Override
    public StateDTO deleteState(Long id) {
        State state = getStateEntityById(id);
        if (cityRepository.existsByState_Id(id)) {
            throw new ValidationException("State cannot be deleted because cities exist under it.");
        }
        if (tenantRepository.existsByState_Id(id)) {
            throw new ValidationException("State cannot be deleted because tenants reference it.");
        }
        stateRepository.delete(state);
        return toDto(state);
    }

    private State getStateEntityById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("State Id is required.");
        }
        State state = stateRepository.findStateById(id);
        if (state == null) {
            throw new ResourceNotFoundException("State not found.");
        }
        return state;
    }

    private StateDTO toDto(State state) {
        if (state == null) {
            return null;
        }
        StateDTO dto = new StateDTO();
        dto.setId(state.getId());
        dto.setCreatedDate(DateFormatUtils.formatDateTime(state.getCreatedAt()));
        if (state.getUpdatedAt() != null) {
            dto.setUpdatedDate(DateFormatUtils.formatDateTime(state.getUpdatedAt()));
        }
        dto.setName(state.getName());
        dto.setCountryId(state.getCountry().getId());
        dto.setCountryName(state.getCountry().getName());
        return dto;
    }

    private String normalizeName(String name) {
        String normalized = ConvertUtils.normalizeText(name);
        if (normalized == null) {
            throw new IllegalArgumentException("State name is required.");
        }
        return normalized;
    }
}
