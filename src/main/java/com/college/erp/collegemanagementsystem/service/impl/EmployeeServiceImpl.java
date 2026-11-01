package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.dto.EmployeeDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.dto.request.EmployeeRequest;
import com.college.erp.collegemanagementsystem.entity.Department;
import com.college.erp.collegemanagementsystem.entity.Employee;
import com.college.erp.collegemanagementsystem.entity.Tenant;
import com.college.erp.collegemanagementsystem.enums.EmployeeType;
import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.exception.ResourceNotFoundException;
import com.college.erp.collegemanagementsystem.exception.ValidationException;
import com.college.erp.collegemanagementsystem.repository.EmployeeRepository;
import com.college.erp.collegemanagementsystem.service.EmployeeService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.service.TenantAccessService;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employees;
    private final EntityManager entityManager;
    private final TenantAccessService tenantAccess;
    private final EntityChangeLogService changeLogs;

    public EmployeeServiceImpl(EmployeeRepository employees, EntityManager entityManager,
                               TenantAccessService tenantAccess, EntityChangeLogService changeLogs) {
        this.employees = employees;
        this.entityManager = entityManager;
        this.tenantAccess = tenantAccess;
        this.changeLogs = changeLogs;
    }

    @Override
    @Transactional(readOnly = true)
    public PagablePage<EmployeeDTO> search(String q, EmployeeType type, Integer page, Integer size, Long requestedTenantId) {
        Long tenantId = tenantAccess.resolve(requestedTenantId, false);
        Specification<Employee> spec = (root, query, cb) -> {
            var p = cb.isFalse(root.get("deleted"));
            if (tenantId != null) p = cb.and(p, cb.equal(root.get("tenant").get("id"), tenantId));
            if (type != null) p = cb.and(p, cb.equal(root.get("employeeType"), type));
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                p = cb.and(p, cb.or(cb.like(cb.lower(root.get("employeeNumber")), term), cb.like(cb.lower(root.get("firstName")), term),
                        cb.like(cb.lower(root.get("middleName")), term), cb.like(cb.lower(root.get("lastName")), term),
                        cb.like(cb.lower(root.get("email")), term), cb.like(cb.lower(root.get("phone")), term),
                        cb.like(cb.lower(root.get("designation")), term), cb.like(cb.lower(root.get("department").get("name")), term)));
            }
            return p;
        };
        var rows = employees.findAll(spec, PageRequest.of(PagablePage.normalizePage(page) - 1,
                PagablePage.normalizeSize(size), Sort.by(Sort.Direction.DESC, "id")));
        return PagablePage.from(rows.map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDTO get(Long id, Long requestedTenantId) {
        return toDto(find(id, tenantAccess.resolve(requestedTenantId, true)));
    }

    @Override
    public EmployeeDTO create(EmployeeRequest request, Long requestedTenantId) {
        long tenantId = tenantAccess.resolve(requestedTenantId, true);
        Employee employee = new Employee();
        employee.setTenant(entityManager.getReference(Tenant.class, tenantId));
        apply(employee, request, tenantId);
        Employee saved = employees.save(employee);
        log(saved, "employeeNumber", null, saved.getEmployeeNumber(), EntityChangeAction.CREATED, "Employee profile created.");
        log(saved, "employeeType", null, saved.getEmployeeType(), EntityChangeAction.CREATED, "Employee profile created.");
        log(saved, "firstName", null, saved.getFirstName(), EntityChangeAction.CREATED, "Employee profile created.");
        log(saved, "middleName", null, saved.getMiddleName(), EntityChangeAction.CREATED, "Employee profile created.");
        log(saved, "lastName", null, saved.getLastName(), EntityChangeAction.CREATED, "Employee profile created.");
        log(saved, "email", null, saved.getEmail(), EntityChangeAction.CREATED, "Employee profile created.");
        log(saved, "phone", null, saved.getPhone(), EntityChangeAction.CREATED, "Employee profile created.");
        log(saved, "designation", null, saved.getDesignation(), EntityChangeAction.CREATED, "Employee profile created.");
        log(saved, "department", null, saved.getDepartment() == null ? null : saved.getDepartment().getName(), EntityChangeAction.CREATED, "Employee profile created.");
        log(saved, "hireDate", null, saved.getHireDate(), EntityChangeAction.CREATED, "Employee profile created.");
        log(saved, "status", null, saved.getStatus(), EntityChangeAction.CREATED, "Employee profile created.");
        return toDto(saved);
    }

    @Override
    public EmployeeDTO update(Long id, EmployeeRequest request, Long requestedTenantId, String remarks) {
        Employee employee = find(id, tenantAccess.resolve(requestedTenantId, true));
        EmployeeDTO before = toDto(employee);
        apply(employee, request, employee.getTenant().getId());
        EmployeeDTO after = toDto(employees.save(employee));
        String note = clean(remarks, "Employee profile updated.");
        diff(after, "employeeNumber", before.getEmployeeNumber(), after.getEmployeeNumber(), note);
        diff(after, "employeeType", before.getEmployeeType(), after.getEmployeeType(), note);
        diff(after, "firstName", before.getFirstName(), after.getFirstName(), note);
        diff(after, "middleName", before.getMiddleName(), after.getMiddleName(), note);
        diff(after, "lastName", before.getLastName(), after.getLastName(), note);
        diff(after, "email", before.getEmail(), after.getEmail(), note);
        diff(after, "phone", before.getPhone(), after.getPhone(), note);
        diff(after, "designation", before.getDesignation(), after.getDesignation(), note);
        diff(after, "department", before.getDepartmentName(), after.getDepartmentName(), note);
        diff(after, "hireDate", before.getHireDate(), after.getHireDate(), note);
        diff(after, "status", before.getStatus(), after.getStatus(), note);
        return after;
    }

    @Override
    public EmployeeDTO changeStatus(Long id, Status status, Long requestedTenantId, String remarks) {
        if (status == null) throw new ValidationException("Status is required.");
        Employee employee = find(id, tenantAccess.resolve(requestedTenantId, true));
        Status before = employee.getStatus();
        employee.setStatus(status);
        employees.save(employee);
        log(employee, "status", before, status, EntityChangeAction.STATUS_CHANGED, clean(remarks, "Employee status changed."));
        return toDto(employee);
    }

    @Override
    public void delete(Long id, Long requestedTenantId, String remarks) {
        Employee employee = find(id, tenantAccess.resolve(requestedTenantId, true));
        employee.setDeleted(true);
        employees.save(employee);
        log(employee, "deleted", false, true, EntityChangeAction.DELETED, clean(remarks, "Employee profile deleted."));
    }

    private void apply(Employee employee, EmployeeRequest request, long tenantId) {
        employee.setEmployeeNumber(request.getEmployeeNumber().trim());
        employee.setEmployeeType(request.getEmployeeType());
        employee.setFirstName(request.getFirstName().trim());
        employee.setMiddleName(clean(request.getMiddleName(), null));
        employee.setLastName(request.getLastName().trim());
        employee.setEmail(clean(request.getEmail(), null));
        employee.setPhone(clean(request.getPhone(), null));
        employee.setDesignation(request.getDesignation().trim());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus());
        if (request.getDepartmentId() == null) employee.setDepartment(null);
        else {
            Department department = entityManager.find(Department.class, request.getDepartmentId());
            if (department == null || department.getTenant().getId() != tenantId)
                throw new ResourceNotFoundException("Department not found in this tenant.");
            if (department.getStatus() != Status.ACTIVE) throw new ValidationException("Department must be active.");
            employee.setDepartment(department);
        }
    }

    private Employee find(Long id, long tenantId) {
        return employees.findByIdAndTenantIdAndDeletedFalse(id, tenantId).orElseThrow(() -> new ResourceNotFoundException("Employee profile not found."));
    }

    private EmployeeDTO toDto(Employee e) {
        EmployeeDTO d = new EmployeeDTO();
        d.setId(e.getId());
        d.setTenantId(e.getTenant().getId());
        d.setTenantName(e.getTenant().getTenantName());
        d.setEmployeeNumber(e.getEmployeeNumber());
        d.setEmployeeType(e.getEmployeeType());
        d.setFirstName(e.getFirstName());
        d.setMiddleName(e.getMiddleName());
        d.setLastName(e.getLastName());
        d.setEmail(e.getEmail());
        d.setPhone(e.getPhone());
        d.setDesignation(e.getDesignation());
        d.setHireDate(e.getHireDate());
        d.setStatus(e.getStatus());
        if (e.getDepartment() != null) {
            d.setDepartmentId(e.getDepartment().getId());
            d.setDepartmentName(e.getDepartment().getName());
        }
        return d;
    }

    private void diff(EmployeeDTO d, String field, Object before, Object after, String remarks) {
        changeLogs.logChange("Employee", d.getId(), EntityChangeAction.UPDATED, field, before, after, remarks);
    }

    private void log(Employee e, String field, Object before, Object after, EntityChangeAction action, String remarks) {
        changeLogs.logChange("Employee", e.getId(), action, field, before, after, remarks);
    }

    private String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
