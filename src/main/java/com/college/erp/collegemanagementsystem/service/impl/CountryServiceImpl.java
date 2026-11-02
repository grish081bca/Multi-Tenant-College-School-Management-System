package com.college.erp.collegemanagementsystem.service.impl;

import java.util.List;

import com.college.erp.collegemanagementsystem.dto.CountryDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.request.CountryCreateRequest;
import com.college.erp.collegemanagementsystem.dto.request.CountryUpdateRequest;
import com.college.erp.collegemanagementsystem.entity.Country;
import com.college.erp.collegemanagementsystem.exception.DuplicateResourceException;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.repository.CountryRepository;
import com.college.erp.collegemanagementsystem.repository.StateRepository;
import com.college.erp.collegemanagementsystem.repository.TenantRepository;
import com.college.erp.collegemanagementsystem.service.CountryService;
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
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final TenantRepository tenantRepository;

    public CountryServiceImpl(CountryRepository countryRepository,
                              StateRepository stateRepository,
                              TenantRepository tenantRepository) {
        this.countryRepository = countryRepository;
        this.stateRepository = stateRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public CountryDTO createCountry(CountryCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request could not be empty.");
        }
        String name = normalizeName(request.getName());
        String isoCode = normalizeIsoCode(request.getIsoCode());
        if (countryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Country name already exists: " + name);
        }
        if (countryRepository.existsByIsoCodeIgnoreCase(isoCode)) {
            throw new DuplicateResourceException("Country ISO code already exists: " + isoCode);
        }
        Country country = new Country();
        country.setName(name);
        country.setIsoCode(isoCode);
        return toDto(countryRepository.save(country));
    }

    @Override
    public CountryDTO updateCountry(Long id, CountryUpdateRequest request) {
        Country country = getCountryEntityById(id);
        if (request == null) {
            throw new IllegalArgumentException("Request could not be empty.");
        }
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String name = normalizeName(request.getName());
            if (countryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
                throw new DuplicateResourceException("Country name already exists: " + name);
            }
            country.setName(name);
        }
        if (request.getIsoCode() != null && !request.getIsoCode().trim().isEmpty()) {
            String isoCode = normalizeIsoCode(request.getIsoCode());
            if (countryRepository.existsByIsoCodeIgnoreCaseAndIdNot(isoCode, id)) {
                throw new DuplicateResourceException("Country ISO code already exists: " + isoCode);
            }
            country.setIsoCode(isoCode);
        }
        return toDto(countryRepository.save(country));
    }

    @Override
    @Transactional(readOnly = true)
    public CountryDTO getCountryById(Long id) {
        return toDto(getCountryEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountryDTO> getAllCountries() {
        List<Country> countries = countryRepository.findAllByOrderByIdDesc();
        if (countries.isEmpty()) {
            throw new ResourceNotFoundException("No countries found.");
        }
        return countries.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagablePage<CountryDTO> getCountriesPage(String search, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(PagablePage.normalizePage(page) - 1, PagablePage.normalizeSize(size), Sort.by(Sort.Direction.DESC, "id"));
        Specification<Country> specification = (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }
            String term = "%" + search.trim().toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("name")), term),
                    builder.like(builder.lower(root.get("isoCode")), term)
            );
        };
        return PagablePage.from(countryRepository.findAll(specification, pageRequest).map(this::toDto));
    }

    @Override
    public CountryDTO deleteCountry(Long id) {
        Country country = getCountryEntityById(id);
        if (stateRepository.existsByCountry_Id(id)) {
            throw new ValidationException("Country cannot be deleted because states exist under it.");
        }
        if (tenantRepository.existsByCountry_Id(id)) {
            throw new ValidationException("Country cannot be deleted because tenants reference it.");
        }
        countryRepository.delete(country);
        return toDto(country);
    }

    private Country getCountryEntityById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Country Id is required.");
        }
        Country country = countryRepository.findCountryById(id);
        if (country == null) {
            throw new ResourceNotFoundException("Country not found.");
        }
        return country;
    }

    private CountryDTO toDto(Country country) {
        if (country == null) {
            return null;
        }
        CountryDTO dto = new CountryDTO();
        dto.setId(country.getId());
        dto.setCreatedDate(DateFormatUtils.formatDateTime(country.getCreatedAt()));
        if (country.getUpdatedAt() != null) {
            dto.setUpdatedDate(DateFormatUtils.formatDateTime(country.getUpdatedAt()));
        }
        dto.setName(country.getName());
        dto.setIsoCode(country.getIsoCode());
        return dto;
    }

    private String normalizeName(String name) {
        String normalized = ConvertUtils.normalizeText(name);
        if (normalized == null) {
            throw new IllegalArgumentException("Country name is required.");
        }
        return normalized;
    }

    private String normalizeIsoCode(String isoCode) {
        String normalized = ConvertUtils.normalizeToUppercase(isoCode);
        if (normalized == null) {
            throw new IllegalArgumentException("Country ISO code is required.");
        }
        return normalized;
    }
}
