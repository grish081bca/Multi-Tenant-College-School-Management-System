package com.college.erp.collegemanagementsystem.service.impl;

import java.util.List;

import com.college.erp.collegemanagementsystem.dto.CityDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.request.CityCreateRequest;
import com.college.erp.collegemanagementsystem.dto.request.CityUpdateRequest;
import com.college.erp.collegemanagementsystem.entity.City;
import com.college.erp.collegemanagementsystem.entity.State;
import com.college.erp.collegemanagementsystem.exception.DuplicateResourceException;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.repository.CityRepository;
import com.college.erp.collegemanagementsystem.repository.StateRepository;
import com.college.erp.collegemanagementsystem.repository.TenantRepository;
import com.college.erp.collegemanagementsystem.service.CityService;
import com.college.erp.collegemanagementsystem.util.ConvertUtils;
import com.college.erp.collegemanagementsystem.util.DateFormatUtils;
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
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final StateRepository stateRepository;
    private final TenantRepository tenantRepository;

    public CityServiceImpl(CityRepository cityRepository,
                           StateRepository stateRepository,
                           TenantRepository tenantRepository) {
        this.cityRepository = cityRepository;
        this.stateRepository = stateRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public CityDTO createCity(CityCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request could not be empty.");
        }
        String name = normalizeName(request.getName());
        State state = getStateEntityById(request.getStateId());
        if (cityRepository.existsByNameIgnoreCaseAndState_Id(name, state.getId())) {
            throw new DuplicateResourceException("City name already exists in state: " + name);
        }
        City city = new City();
        city.setName(name);
        city.setState(state);
        return toDto(cityRepository.save(city));
    }

    @Override
    public CityDTO updateCity(Long id, CityUpdateRequest request) {
        City city = getCityEntityById(id);
        if (request == null) {
            throw new IllegalArgumentException("Request could not be empty.");
        }
        State targetState = request.getStateId() != null ? getStateEntityById(request.getStateId()) : city.getState();
        String targetName = request.getName() != null && !request.getName().trim().isEmpty() ? normalizeName(request.getName()) : city.getName();
        if (cityRepository.existsByNameIgnoreCaseAndState_IdAndIdNot(targetName, targetState.getId(), id)) {
            throw new DuplicateResourceException("City name already exists in state: " + targetName);
        }
        city.setState(targetState);
        city.setName(targetName);
        return toDto(cityRepository.save(city));
    }

    @Override
    @Transactional(readOnly = true)
    public CityDTO getCityById(Long id) {
        return toDto(getCityEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityDTO> getAllCities() {
        List<City> cities = cityRepository.findAllByOrderByIdDesc();
        if (cities.isEmpty()) {
            throw new ResourceNotFoundException("No cities found.");
        }
        return cities.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagablePage<CityDTO> getCitiesPage(String search, Long stateId, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(PagablePage.normalizePage(page) - 1, PagablePage.normalizeSize(size), Sort.by(Sort.Direction.DESC, "id"));
        Specification<City> specification = (root, query, builder) -> {
            var predicate = builder.conjunction();
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                var state = root.join("state");
                predicate = builder.and(predicate, builder.or(
                        builder.like(builder.lower(root.get("name")), term),
                        builder.like(builder.lower(state.get("name")), term),
                        builder.like(builder.lower(state.join("country").get("name")), term)
                ));
            }
            if (stateId != null) {
                predicate = builder.and(predicate, builder.equal(root.get("state").get("id"), stateId));
            }
            return predicate;
        };
        return PagablePage.from(cityRepository.findAll(specification, pageRequest).map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityDTO> getCitiesByStateId(Long stateId) {
        State state = getStateEntityById(stateId);
        List<City> cities = cityRepository.findAllByState_IdOrderByIdDesc(state.getId());
        if (cities.isEmpty()) {
            throw new ResourceNotFoundException("No cities found for state.");
        }
        return cities.stream().map(this::toDto).toList();
    }

    @Override
    public CityDTO deleteCity(Long id) {
        City city = getCityEntityById(id);
        if (tenantRepository.existsByCity_Id(id)) {
            throw new ValidationException("City cannot be deleted because tenants reference it.");
        }
        cityRepository.delete(city);
        return toDto(city);
    }

    private City getCityEntityById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("City Id is required.");
        }
        City city = cityRepository.findCityById(id);
        if (city == null) {
            throw new ResourceNotFoundException("City not found.");
        }
        return city;
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

    private CityDTO toDto(City city) {
        if (city == null) {
            return null;
        }
        CityDTO dto = new CityDTO();
        dto.setId(city.getId());
        dto.setCreatedDate(DateFormatUtils.formatDateTime(city.getCreatedAt()));
        if (city.getUpdatedAt() != null) {
            dto.setUpdatedDate(DateFormatUtils.formatDateTime(city.getUpdatedAt()));
        }
        dto.setName(city.getName());
        dto.setStateId(city.getState().getId());
        dto.setStateName(city.getState().getName());
        dto.setCountryId(city.getState().getCountry().getId());
        dto.setCountryName(city.getState().getCountry().getName());
        return dto;
    }

    private String normalizeName(String name) {
        String normalized = ConvertUtils.normalizeText(name);
        if (normalized == null) {
            throw new IllegalArgumentException("City name is required.");
        }
        return normalized;
    }
}
