package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.dto.DashboardDTO;
import com.college.erp.collegemanagementsystem.entity.*;
import com.college.erp.collegemanagementsystem.enums.UserType;
import com.college.erp.collegemanagementsystem.service.DashboardService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final List<String> CHART_COLORS = List.of("#5b6df8", "#16b8a6", "#f5a524", "#f06a8b");

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public DashboardDTO getDashboard(UserType userType, Long tenantId, String displayName) {
        UserType role = userType == null ? UserType.STAFF : userType;
        boolean platformAdmin = role == UserType.SUPER_ADMIN || role == UserType.SYSTEM_ADMIN;
        Long scopeTenantId = platformAdmin ? null : tenantId;

        boolean missingTenantScope = !platformAdmin && tenantId == null;
        Map<String, Long> counts = missingTenantScope ? emptyCounts() : loadCounts(scopeTenantId);
        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setGreeting(greetingName(displayName));
        dashboard.setRoleLabel(roleLabel(role));
        dashboard.setDescription(description(role));
        dashboard.setCards(cards(role, counts));
        dashboard.setBarItems(chartItems(dashboard.getCards()));
        if (missingTenantScope) {
            dashboard.setPieItems(List.of(new DashboardDTO.DashboardChartItemDTO("No enrollments yet", 0, "#e2e8f0", 100, null)));
            dashboard.setPieGradient("conic-gradient(#e2e8f0 0% 100%)");
        } else {
            addEnrollmentPie(dashboard, scopeTenantId);
        }
        dashboard.setActions(actions(role));
        return dashboard;
    }

    private Map<String, Long> loadCounts(Long tenantId) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("tenants", count(Tenant.class, tenantId));
        counts.put("users", count(User.class, tenantId));
        counts.put("students", count(Student.class, tenantId));
        counts.put("enrollments", count(StudentEnrollment.class, tenantId));
        counts.put("programs", count(Program.class, tenantId));
        counts.put("subjects", count(Subject.class, tenantId));
        counts.put("sections", count(Section.class, tenantId));
        counts.put("faculties", count(Faculty.class, tenantId));
        counts.put("departments", count(Department.class, tenantId));
        counts.put("academicYears", count(AcademicYear.class, tenantId));
        counts.put("semesters", count(Semester.class, tenantId));
        counts.put("countries", count(Country.class, tenantId));
        counts.put("states", count(State.class, tenantId));
        counts.put("cities", count(City.class, tenantId));
        return counts;
    }

    private Map<String, Long> emptyCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String key : List.of("tenants", "users", "students", "enrollments", "programs", "subjects", "sections",
                "faculties", "departments", "academicYears", "semesters", "countries", "states", "cities")) {
            counts.put(key, 0L);
        }
        return counts;
    }

    private long count(Class<?> entityClass, Long tenantId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<?> root = query.from(entityClass);
        query.select(builder.count(root));
        if (tenantId != null && hasTenant(entityClass)) {
            query.where(builder.equal(root.get("tenant").get("id"), tenantId));
        }
        return entityManager.createQuery(query).getSingleResult();
    }

    private boolean hasTenant(Class<?> entityClass) {
        return entityClass != Tenant.class && entityClass != Country.class
                && entityClass != State.class && entityClass != City.class;
    }

    private List<DashboardDTO.DashboardCardDTO> cards(UserType role, Map<String, Long> counts) {
        List<String> keys = switch (role) {
            case SUPER_ADMIN, SYSTEM_ADMIN -> List.of("tenants", "users", "students", "enrollments");
            case COLLEGE_ADMIN -> List.of("students", "users", "programs", "enrollments");
            case COLLEGE_BRANCH -> List.of("students", "sections", "users", "enrollments");
            case PRINCIPAL -> List.of("students", "enrollments", "programs", "sections");
            case TEACHER -> List.of("sections", "subjects", "students", "enrollments");
            case ACCOUNTANT -> List.of("students", "enrollments", "programs", "academicYears");
            case LIBRARIAN -> List.of("students", "subjects", "programs", "enrollments");
            case STUDENT, GUARDIAN -> List.of("students", "enrollments", "programs", "semesters");
            case STAFF -> List.of("students", "enrollments", "sections", "subjects");
        };
        List<DashboardDTO.DashboardCardDTO> result = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            CardDefinition definition = cardDefinition(key);
            result.add(new DashboardDTO.DashboardCardDTO(definition.title, counts.getOrDefault(key, 0L),
                    definition.icon, "tone-" + (i + 1), cardHref(role, key, definition.href), definition.caption));
        }
        return result;
    }

    private CardDefinition cardDefinition(String key) {
        return switch (key) {
            case "tenants" -> new CardDefinition("Colleges", "fa-solid fa-building", "/web/tenants", "Across your platform");
            case "users" -> new CardDefinition("Team members", "fa-solid fa-users", "/web/users", "Accounts in your scope");
            case "students" -> new CardDefinition("Students", "fa-solid fa-user-graduate", "/web/students", "Student records in scope");
            case "enrollments" -> new CardDefinition("Enrollments", "fa-solid fa-clipboard-check", "/web/student-enrollments", "Academic enrollment records");
            case "programs" -> new CardDefinition("Programs", "fa-solid fa-graduation-cap", "/web/programs", "Programs available");
            case "subjects" -> new CardDefinition("Subjects", "fa-solid fa-book-open", "/web/subjects", "Subjects in the catalog");
            case "sections" -> new CardDefinition("Sections", "fa-solid fa-people-group", "/web/sections", "Class sections available");
            case "academicYears" -> new CardDefinition("Academic years", "fa-solid fa-calendar-days", "/web/academic-years", "Academic year records");
            case "semesters" -> new CardDefinition("Semesters", "fa-solid fa-calendar-week", "/web/semesters", "Semester records");
            default -> new CardDefinition("Overview", "fa-solid fa-chart-line", "/web/dashboard", "Current workspace total");
        };
    }

    private List<DashboardDTO.DashboardChartItemDTO> chartItems(List<DashboardDTO.DashboardCardDTO> cards) {
        long max = cards.stream().mapToLong(DashboardDTO.DashboardCardDTO::getValue).max().orElse(0L);
        List<DashboardDTO.DashboardChartItemDTO> result = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            DashboardDTO.DashboardCardDTO card = cards.get(i);
            int percentage = max == 0 ? 0 : (int) Math.max(4, Math.round(card.getValue() * 100.0 / max));
            result.add(new DashboardDTO.DashboardChartItemDTO(card.getTitle(), card.getValue(),
                    CHART_COLORS.get(i % CHART_COLORS.size()), percentage, card.getHref()));
        }
        return result;
    }

    private void addEnrollmentPie(DashboardDTO dashboard, Long tenantId) {
        List<StatusCount> statusCounts = statusCounts(StudentEnrollment.class, tenantId);
        if (statusCounts.isEmpty()) {
            dashboard.setPieItems(List.of(new DashboardDTO.DashboardChartItemDTO("No enrollments yet", 0, "#e2e8f0", 100, "/web/student-enrollments")));
            dashboard.setPieGradient("conic-gradient(#e2e8f0 0% 100%)");
            return;
        }
        long total = statusCounts.stream().mapToLong(StatusCount::count).sum();
        dashboard.setPieTotal(total);
        List<DashboardDTO.DashboardChartItemDTO> items = new ArrayList<>();
        List<String> gradients = new ArrayList<>();
        double cursor = 0;
        for (int i = 0; i < statusCounts.size(); i++) {
            StatusCount status = statusCounts.get(i);
            int percent = total == 0 ? 0 : (int) Math.round(status.count * 100.0 / total);
            double end = total == 0 ? cursor : cursor + status.count * 100.0 / total;
            String color = CHART_COLORS.get(i % CHART_COLORS.size());
            items.add(new DashboardDTO.DashboardChartItemDTO(status.label, status.count, color, percent, "/web/student-enrollments"));
            gradients.add(color + " " + String.format(Locale.ROOT, "%.2f", cursor) + "% " + String.format(Locale.ROOT, "%.2f", end) + "%");
            cursor = end;
        }
        dashboard.setPieItems(items);
        dashboard.setPieGradient("conic-gradient(" + String.join(", ", gradients) + ")");
    }

    private List<StatusCount> statusCounts(Class<?> entityClass, Long tenantId) {
        String jpql = "select e.status, count(e) from " + entityClass.getSimpleName() + " e"
                + (tenantId == null ? "" : " where e.tenant.id = :tenantId") + " group by e.status order by e.status";
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        if (tenantId != null) query.setParameter("tenantId", tenantId);
        List<StatusCount> result = new ArrayList<>();
        for (Object[] row : query.getResultList()) {
            String status = String.valueOf(row[0]).toLowerCase(Locale.ROOT);
            String label = Character.toUpperCase(status.charAt(0)) + status.substring(1);
            result.add(new StatusCount(label, ((Number) row[1]).longValue()));
        }
        return result;
    }

    private List<DashboardDTO.DashboardActionDTO> actions(UserType role) {
        List<String> keys = switch (role) {
            case SUPER_ADMIN, SYSTEM_ADMIN -> List.of("tenants", "users", "menuTemplates");
            case COLLEGE_ADMIN -> List.of("students", "enrollments", "programs");
            case COLLEGE_BRANCH -> List.of("students", "enrollments", "users");
            case PRINCIPAL -> List.of("students", "sections", "programs");
            case TEACHER -> List.of("sections", "subjects", "students");
            case ACCOUNTANT -> List.of("enrollments", "students", "academicYears");
            case LIBRARIAN -> List.of("students", "subjects", "programs");
            case STUDENT, GUARDIAN -> List.of("enrollments");
            case STAFF -> List.of("students", "enrollments", "sections");
        };
        List<DashboardDTO.DashboardActionDTO> result = new ArrayList<>();
        for (String key : keys) {
            switch (key) {
                case "tenants" -> result.add(new DashboardDTO.DashboardActionDTO("Manage colleges", "Review your college network", "fa-solid fa-building", "/web/tenants"));
                case "users" -> result.add(new DashboardDTO.DashboardActionDTO("Manage users", "Keep teams and access up to date", "fa-solid fa-users", "/web/users"));
                case "menuTemplates" -> result.add(new DashboardDTO.DashboardActionDTO("Menu templates", "Manage role based navigation", "fa-solid fa-sliders", "/web/menu-templates"));
                case "students" -> result.add(new DashboardDTO.DashboardActionDTO("Students", "Browse student records", "fa-solid fa-user-graduate", "/web/students"));
                case "enrollments" -> result.add(new DashboardDTO.DashboardActionDTO("Enrollments", "View academic registrations", "fa-solid fa-clipboard-check", "/web/student-enrollments"));
                case "sections" -> result.add(new DashboardDTO.DashboardActionDTO("Sections", "Explore class sections", "fa-solid fa-people-group", "/web/sections"));
                case "subjects" -> result.add(new DashboardDTO.DashboardActionDTO("Subjects", "Browse the subject catalog", "fa-solid fa-book-open", "/web/subjects"));
                case "programs" -> result.add(new DashboardDTO.DashboardActionDTO("Programs", "Explore available programs", "fa-solid fa-graduation-cap", "/web/programs"));
                case "academicYears" -> result.add(new DashboardDTO.DashboardActionDTO("Academic years", "Review academic schedules", "fa-solid fa-calendar-days", "/web/academic-years"));
                default -> { }
            }
        }
        return result;
    }

    private String cardHref(UserType role, String key, String defaultHref) {
        if (role == UserType.SUPER_ADMIN || role == UserType.SYSTEM_ADMIN || role == UserType.COLLEGE_BRANCH
                || role == UserType.COLLEGE_ADMIN || role == UserType.PRINCIPAL) {
            return role == UserType.COLLEGE_ADMIN && "users".equals(key) ? null : defaultHref;
        }
        return switch (role) {
            case TEACHER -> List.of("sections", "subjects", "students", "enrollments").contains(key) ? defaultHref : null;
            case ACCOUNTANT -> List.of("students", "enrollments", "academicYears").contains(key) ? defaultHref : null;
            case LIBRARIAN, STAFF -> List.of("students", "enrollments").contains(key) ? defaultHref : null;
            case STUDENT, GUARDIAN -> "enrollments".equals(key) ? defaultHref : null;
            default -> null;
        };
    }

    private String greetingName(String displayName) {
        return displayName == null || displayName.isBlank() ? "there" : displayName.trim().split("\\s+")[0];
    }

    private String roleLabel(UserType role) {
        String[] words = role.name().toLowerCase(Locale.ROOT).split("_");
        StringJoiner label = new StringJoiner(" ");
        for (String word : words) label.add(Character.toUpperCase(word.charAt(0)) + word.substring(1));
        return label.toString();
    }

    private String description(UserType role) {
        return switch (role) {
            case SUPER_ADMIN, SYSTEM_ADMIN -> "A live view of your college network and platform activity.";
            case COLLEGE_ADMIN, COLLEGE_BRANCH -> "Your college at a glance, with the tools you use every day.";
            case PRINCIPAL -> "A clear view of students, programs, and academic operations.";
            case TEACHER -> "Your teaching workspace, classes, subjects, and student activity.";
            case ACCOUNTANT -> "A focused overview of student records and academic enrollment.";
            case LIBRARIAN -> "A quick look at the students and learning resources in your college.";
            case STUDENT -> "Your student workspace and a snapshot of academic activity.";
            case GUARDIAN -> "A helpful snapshot of student and academic activity.";
            case STAFF -> "A simple overview of the academic services you support.";
        };
    }

    private record CardDefinition(String title, String icon, String href, String caption) { }
    private record StatusCount(String label, long count) { }
}
