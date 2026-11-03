package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.dto.MenuDTO;
import com.college.erp.collegemanagementsystem.dto.MenuTemplateDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.entity.Menu;
import com.college.erp.collegemanagementsystem.entity.MenuTemplate;
import com.college.erp.collegemanagementsystem.entity.UserTemplate;
import com.college.erp.collegemanagementsystem.enums.MenuStatus;
import com.college.erp.collegemanagementsystem.enums.UserType;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.repository.MenuRepository;
import com.college.erp.collegemanagementsystem.repository.MenuTemplateRepository;
import com.college.erp.collegemanagementsystem.repository.UserTemplateRepository;
import com.college.erp.collegemanagementsystem.service.MenuTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author grish
 *
 */
@Service
@Transactional
public class MenuTemplateServiceImpl implements MenuTemplateService {

    private final MenuTemplateRepository menuTemplateRepository;
    private final MenuRepository menuRepository;
    private final UserTemplateRepository userTemplateRepository;

    public MenuTemplateServiceImpl(MenuTemplateRepository menuTemplateRepository,
                                   MenuRepository menuRepository,
                                   UserTemplateRepository userTemplateRepository) {
        this.menuTemplateRepository = menuTemplateRepository;
        this.menuRepository = menuRepository;
        this.userTemplateRepository = userTemplateRepository;
    }
    @Override
    public MenuTemplateDTO assignMenuTemplate(MenuTemplateDTO dto) {
        return saveMenuTemplate(dto, null);
    }

    @Override
    public MenuTemplateDTO update(Long id, MenuTemplateDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("Menu template ID is required.");
        }
        return saveMenuTemplate(dto, id);
    }

    private MenuTemplateDTO saveMenuTemplate(MenuTemplateDTO dto, Long id) {
        if (dto == null || dto.getUserType() == null || dto.getUserType().isBlank()) {
            throw new IllegalArgumentException("User type is required.");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Template name is required.");
        }
        UserType userType = UserType.valueOf(dto.getUserType());
        List<Long> menuIds = dto.getMenuIds();
        if ((menuIds == null || menuIds.isEmpty()) && dto.getMenuId() != null) {
            menuIds = List.of(dto.getMenuId());
        }
        if (menuIds == null || menuIds.isEmpty()) {
            throw new IllegalArgumentException("At least one menu is required.");
        }
        List<Menu> menus = menuRepository.findAllById(menuIds);
        if (menus.size() != menuIds.stream().distinct().count()) {
            throw new ResourceNotFoundException("One or more menus were not found.");
        }
        MenuTemplate template;
        if (id == null) {
            template = menuTemplateRepository.findByUserType(userType).orElse(new MenuTemplate());
        } else {
            template = menuTemplateRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Menu template not found."));
            MenuTemplate templateForUserType = menuTemplateRepository.findByUserType(userType).orElse(null);
            if (templateForUserType != null && !templateForUserType.getId().equals(id)) {
                throw new IllegalArgumentException("A menu template already exists for this user type.");
            }
        }
        template.setName(dto.getName().trim());
        template.setUserType(userType);
        template.getMenus().clear();
        template.getMenus().addAll(menus);
        template.setStatus(dto.getStatus() == null || dto.getStatus().isBlank() ? MenuStatus.ACTIVE : MenuStatus.valueOf(dto.getStatus()));
        return toDto(menuTemplateRepository.save(template));
    }

    @Override
    @Transactional(readOnly = true)
    public MenuTemplateDTO findOne(Long id) {
        MenuTemplate template = menuTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu template not found."));
        return toDto(template);
    }

    @Override
    public MenuTemplateDTO changeStatus(Long id, MenuStatus status) {
        MenuTemplate template = menuTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu template not found."));
        template.setStatus(status);
        return toDto(menuTemplateRepository.save(template));
    }
    @Override
    @Transactional(readOnly = true)
    public List<MenuTemplateDTO> findAll() {
        return menuTemplateRepository.findAllByOrderByIdDesc().stream().map(this::toDto).toList();
    }
    @Override
    @Transactional(readOnly = true)
    public PagablePage<MenuTemplateDTO> findPage(String search, UserType userType, MenuStatus status, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(PagablePage.normalizePage(page) - 1, PagablePage.normalizeSize(size), Sort.by(Sort.Direction.DESC, "id"));
        Specification<MenuTemplate> specification = (root, query, builder) -> {
            query.distinct(true);
            var predicate = builder.conjunction();
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                var menuJoin = root.join("menus", jakarta.persistence.criteria.JoinType.LEFT);
                predicate = builder.and(predicate, builder.or(
                                builder.like(builder.lower(root.get("name")), term),
                                builder.like(builder.lower(menuJoin.get("name")), term),
                        builder.like(builder.lower(menuJoin.get("menuCode")), term)
                ));
            }
            if (userType != null) {
                predicate = builder.and(predicate, builder.equal(root.get("userType"), userType));
            }
            if (status != null) {
                predicate = builder.and(predicate, builder.equal(root.get("status"), status));
            }
            return predicate;
        };
        return PagablePage.from(menuTemplateRepository.findAll(specification, pageRequest).map(this::toDto));
    }
    @Override
    @Transactional(readOnly = true)
    public List<MenuDTO> findMenusByUserType(UserType userType) {
        if (userType == null) {
            return List.of();
        }
        List<MenuTemplate> templates = menuTemplateRepository.findActiveTemplates(userType, MenuStatus.ACTIVE);
        Map<Long, MenuDTO> menus = new LinkedHashMap<>();
        for (MenuTemplate template : templates) {
            for (Menu menu : template.getMenus()) {
                menus.putIfAbsent(menu.getId(), toMenuDto(menu));
            }
        }
        return List.copyOf(menus.values());
    }
    @Override
    @Transactional(readOnly = true)
    public List<MenuDTO> findMenusForUserTemplate(Long userTemplateId, UserType userType) {
        if (userTemplateId != null) {
            UserTemplate userTemplate = userTemplateRepository.findById(userTemplateId).orElse(null);
            if (userTemplate != null && userTemplate.getStatus() == com.college.erp.collegemanagementsystem.enums.UserStatus.ACTIVE
                    && userTemplate.getMenuTemplate() != null
                    && userTemplate.getMenuTemplate().getStatus() == MenuStatus.ACTIVE) {
                Map<Long, MenuDTO> menus = new LinkedHashMap<>();
                for (Menu menu : userTemplate.getMenuTemplate().getMenus()) {
                    if (menu.getStatus() == MenuStatus.ACTIVE) {
                        menus.putIfAbsent(menu.getId(), toMenuDto(menu));
                    }
                }
                return List.copyOf(menus.values());
            }
        }
        return findMenusByUserType(userType);
    }

    private MenuTemplateDTO toDto(MenuTemplate template) {
        MenuTemplateDTO dto = new MenuTemplateDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setUserType(template.getUserType().name());
        dto.setMenuIds(template.getMenus().stream().map(Menu::getId).toList());
        dto.setMenuNames(template.getMenus().stream().map(Menu::getName).toList());
        dto.setMenuName(String.join(", ", dto.getMenuNames()));
        dto.setStatus(template.getStatus().name());
        return dto;
    }

    private MenuDTO toMenuDto(Menu menu) {
        MenuDTO dto = new MenuDTO();
        dto.setId(menu.getId());
        dto.setMenuCode(menu.getMenuCode());
        dto.setName(menu.getName());
        dto.setMenuUrl(menu.getMenuUrl());
        dto.setIcon(menu.getIcon());
        dto.setParentMenuId(menu.getParentMenu() != null ? menu.getParentMenu().getId() : null);
        dto.setParentMenuName(menu.getParentMenu() != null ? menu.getParentMenu().getName() : null);
        dto.setDisplayOrder(menu.getDisplayOrder());
        dto.setStatus(menu.getStatus().name());
        dto.setMenuType(menu.getMenuType().name());
        return dto;
    }
}
