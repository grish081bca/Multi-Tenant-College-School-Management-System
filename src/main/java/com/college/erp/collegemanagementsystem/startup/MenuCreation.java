package com.college.erp.collegemanagementsystem.startup;

import com.college.erp.collegemanagementsystem.entity.Menu;
import com.college.erp.collegemanagementsystem.entity.MenuTemplate;
import com.college.erp.collegemanagementsystem.entity.UserTemplate;
import com.college.erp.collegemanagementsystem.enums.MenuStatus;
import com.college.erp.collegemanagementsystem.enums.MenuType;
import com.college.erp.collegemanagementsystem.enums.UserStatus;
import com.college.erp.collegemanagementsystem.enums.UserType;
import com.college.erp.collegemanagementsystem.repository.MenuRepository;
import com.college.erp.collegemanagementsystem.repository.MenuTemplateRepository;
import com.college.erp.collegemanagementsystem.repository.UserTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author grish
 *
 */
@Component
public class MenuCreation {

    private static final Logger LOGGER = LoggerFactory.getLogger(MenuCreation.class);

    private final MenuRepository menuRepository;
    private final MenuTemplateRepository menuTemplateRepository;
    private final UserTemplateRepository userTemplateRepository;

    public MenuCreation(MenuRepository menuRepository,
                        MenuTemplateRepository menuTemplateRepository,
                        UserTemplateRepository userTemplateRepository) {
        this.menuRepository = menuRepository;
        this.menuTemplateRepository = menuTemplateRepository;
        this.userTemplateRepository = userTemplateRepository;
    }

    public void startupCreator() {
        try {
            Map<String, Menu> menus = createMenus();
            createMenuTemplates(menus);
        } catch (Exception e) {
            LOGGER.error("Menu creation failed.", e);
        }
    }

    private Map<String, Menu> createMenus() {
        Map<String, Menu> createdMenus = new LinkedHashMap<>();

        List<MenuSeed> menuSeeds = List.of(
                superMenu("HOME", "Home", null, "fa-solid fa-house", 10),
                subMenu("DASHBOARD", "Dashboard", "/web/dashboard", "HOME", 11),

                superMenu("TENANT", "Tenant", null, "fa-solid fa-building", 20),
                subMenu("TENANTS_LIST", "List Tenants", "/web/tenants", "TENANT", 21),
                subMenu("TENANTS_ADD", "Add Tenant", "/web/tenants/add", "TENANT", 22),

                superMenu("TENANT_BRANCH", "Tenant Branch", null, "fa-solid fa-building", 23),
                subMenu("TENANT_BRANCHES_LIST", "List Tenant Branches", "/web/tenant-branches", "TENANT_BRANCH", 24),
                subMenu("TENANT_BRANCHES_ADD", "Add Tenant Branch", "/web/tenant-branches/add", "TENANT_BRANCH", 25),

                superMenu("USER", "User", null, "fa-solid fa-users", 30),
                subMenu("USERS_LIST", "List Users", "/web/users", "USER", 31),
                subMenu("USERS_ADD", "Add User", "/web/users/add", "USER", 32),

                superMenu("MENU", "Menu", null, "fa-solid fa-shield-halved", 40),
                subMenu("MENUS_LIST", "List Menus", "/web/menus", "MENU", 41),
                subMenu("MENUS_ADD", "Add Menu", "/web/menus/add", "MENU", 42),

                superMenu("COUNTRY", "Country", null, "fa-solid fa-map-pin", 50),
                subMenu("COUNTRIES_LIST", "List Countries", "/web/countries", "COUNTRY", 51),
                subMenu("COUNTRIES_ADD", "Add Country", "/web/countries/add", "COUNTRY", 52),

                superMenu("USER_TEMPLATE", "User Template", null, "fa-solid fa-user-tag", 60),
                subMenu("USER_TEMPLATES_LIST", "List User Templates", "/web/user-templates", "USER_TEMPLATE", 61),
                subMenu("USER_TEMPLATES_ADD", "Add User Template", "/web/user-templates/add", "USER_TEMPLATE", 62),

                superMenu("MENU_TEMPLATE", "Menu Template", null, "fa-solid fa-shield", 70),
                subMenu("MENU_TEMPLATES_LIST", "List Menu Templates", "/web/menu-templates", "MENU_TEMPLATE", 71),
                subMenu("MENU_TEMPLATES_ADD", "Add Menu Template", "/web/menu-templates/add", "MENU_TEMPLATE", 72),

                superMenu("STATE", "State", null, "fa-solid fa-map-pin", 80),
                subMenu("STATES_LIST", "List States", "/web/states", "STATE", 81),
                subMenu("STATES_ADD", "Add State", "/web/states/add", "STATE", 82),

                superMenu("CITY", "City", null, "fa-solid fa-city", 90),
                subMenu("CITIES_LIST", "List Cities", "/web/cities", "CITY", 91),
                subMenu("CITIES_ADD", "Add City", "/web/cities/add", "CITY", 91),

                superMenu("ACADEMICS", "Academics", null, "fa-solid fa-graduation-cap", 100),
                subMenu("DEPARTMENTS_LIST", "Departments", "/web/departments", "ACADEMICS", 101),
                subMenu("FACULTIES_LIST", "Faculties", "/web/faculties", "ACADEMICS", 102),
                subMenu("PROGRAMS_LIST", "Programs", "/web/programs", "ACADEMICS", 103),
                subMenu("ACADEMIC_YEARS_LIST", "Academic Years", "/web/academic-years", "ACADEMICS", 104),
                subMenu("SEMESTERS_LIST", "Semesters", "/web/semesters", "ACADEMICS", 105),
                subMenu("SECTIONS_LIST", "Sections", "/web/sections", "ACADEMICS", 106),
                subMenu("SUBJECTS_LIST", "Subjects", "/web/subjects", "ACADEMICS", 107),
                subMenu("PROGRAM_SUBJECTS_LIST", "Program Subjects", "/web/program-subjects", "ACADEMICS", 108),
                subMenu("STUDENTS_LIST", "Students", "/web/students", "ACADEMICS", 109),
                subMenu("STUDENT_ENROLLMENTS_LIST", "Student Enrollments", "/web/student-enrollments", "ACADEMICS", 110)
        );

        for (MenuSeed seed : menuSeeds) {
            Menu parentMenu = seed.parentCode() == null ? null : createdMenus.get(seed.parentCode());
            Menu menu = menuRepository.findByMenuCodeIgnoreCase(seed.code()).orElseGet(Menu::new);
            menu.setMenuCode(seed.code());
            menu.setName(seed.name());
            menu.setMenuUrl(seed.url());
            String icon = seed.icon() != null ? seed.icon() : (parentMenu != null ? parentMenu.getIcon() : null);
            menu.setIcon(icon);
            menu.setParentMenu(parentMenu);
            menu.setDisplayOrder(seed.displayOrder());
            menu.setStatus(MenuStatus.ACTIVE);
            menu.setMenuType(seed.parentCode() == null ? MenuType.SUPER_MENU : MenuType.SUB_MENU);
            createdMenus.put(seed.code(), menuRepository.save(menu));
        }

        return createdMenus;
    }

    private void createMenuTemplates(Map<String, Menu> menus) {
        List<String> allMenus = new ArrayList<>(menus.keySet());
        List<String> academicMenuCodes = List.of("ACADEMICS", "DEPARTMENTS_LIST", "FACULTIES_LIST", "PROGRAMS_LIST", "ACADEMIC_YEARS_LIST", "SEMESTERS_LIST", "SECTIONS_LIST", "SUBJECTS_LIST", "PROGRAM_SUBJECTS_LIST", "STUDENTS_LIST", "STUDENT_ENROLLMENTS_LIST");
        List<String> academics = academicMenuCodes;
        List<String> collegeAdminMenus = new ArrayList<>(List.of("DASHBOARD"));
        collegeAdminMenus.addAll(academics);
        List<String> staffMenus = List.of("DASHBOARD");
        List<String> basicMenus = List.of("DASHBOARD");

        List<String> globalMenus = allMenus.stream().filter(code -> !academicMenuCodes.contains(code)).toList();
        createTemplate(UserType.SUPER_ADMIN, "Super Admin Menu Template", globalMenus, menus);
        createTemplate(UserType.SYSTEM_ADMIN, "System Admin Menu Template", globalMenus, menus);
        createTemplate(UserType.COLLEGE_ADMIN, "College Admin Menu Template", collegeAdminMenus, menus);
        createTemplate(UserType.COLLEGE_BRANCH, "College Branch Menu Template", staffMenus, menus);
        createTemplate(UserType.PRINCIPAL, "Principal Menu Template", staffMenus, menus);
        createTemplate(UserType.TEACHER, "Teacher Menu Template", basicMenus, menus);
        createTemplate(UserType.ACCOUNTANT, "Accountant Menu Template", basicMenus, menus);
        createTemplate(UserType.LIBRARIAN, "Librarian Menu Template", basicMenus, menus);
        createTemplate(UserType.STUDENT, "Student Menu Template", basicMenus, menus);
        createTemplate(UserType.GUARDIAN, "Guardian Menu Template", basicMenus, menus);
        createTemplate(UserType.STAFF, "Staff Menu Template", staffMenus, menus);
    }

    private void createTemplate(UserType userType,
                                String templateName,
                                List<String> menuCodes,
                                Map<String, Menu> menus) {
        MenuTemplate menuTemplate = menuTemplateRepository.findAllByUserTypeOrderByIdAsc(userType).stream()
                .findFirst()
                .orElseGet(MenuTemplate::new);
        menuTemplate.setName(templateName);
        menuTemplate.setUserType(userType);
        menuTemplate.setStatus(MenuStatus.ACTIVE);
        menuTemplate.getMenus().clear();
        for (String menuCode : menuCodes) {
            Menu menu = menus.get(menuCode);
            if (menu != null && !menuTemplate.getMenus().contains(menu)) {
                menuTemplate.getMenus().add(menu);
            }
        }
        menuTemplate = menuTemplateRepository.save(menuTemplate);

        UserTemplate userTemplate = userTemplateRepository.findAllByUserTypeOrderByIdAsc(userType).stream()
                .findFirst()
                .orElseGet(UserTemplate::new);
        userTemplate.setUserType(userType);
        userTemplate.setMenuTemplate(menuTemplate);
        userTemplate.setStatus(UserStatus.ACTIVE);
        userTemplateRepository.save(userTemplate);
    }

    private static MenuSeed superMenu(String code, String name, String url, String icon, int displayOrder) {
        return new MenuSeed(code, name, url, icon, null, displayOrder);
    }

    private static MenuSeed subMenu(String code, String name, String url, String parentCode, int displayOrder) {
        return new MenuSeed(code, name, url, null, parentCode, displayOrder);
    }

    private record MenuSeed(String code,
                            String name,
                            String url,
                            String icon,
                            String parentCode,
                            int displayOrder) {
    }
}
