package com.college.erp.collegemanagementsystem.service.impl;

import com.college.erp.collegemanagementsystem.entity.EntityChangeLog;
import com.college.erp.collegemanagementsystem.dto.AuditLogDTO;
import com.college.erp.collegemanagementsystem.dto.PagablePage;
import com.college.erp.collegemanagementsystem.enums.EntityChangeAction;
import com.college.erp.collegemanagementsystem.repository.EntityChangeLogRepository;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import com.college.erp.collegemanagementsystem.util.ConvertUtils;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

/**
 * @author grish
 *
 */
@Service
public class EntityChangeLogServiceImpl implements EntityChangeLogService {

    private final EntityChangeLogRepository repository;

    public EntityChangeLogServiceImpl(EntityChangeLogRepository repository) {
        this.repository = repository;
    }
    @Override
    public boolean hasChanged(Object oldValue, Object newValue) {
        return !Objects.equals(stringify(oldValue), stringify(newValue));
    }
    @Override
    public void logChange(String entityName, Long entityId, EntityChangeAction action, String fieldName, Object oldValue, Object newValue, String remarks) {
        String previousValue = stringify(oldValue);
        String currentValue = stringify(newValue);
        if (!hasChanged(previousValue, currentValue)) {
            return;
        }
        EntityChangeLog log = new EntityChangeLog();
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setFieldName(fieldName);
        log.setOldValue(previousValue);
        log.setNewValue(currentValue);
        log.setRemarks(ConvertUtils.normalizeText(remarks));
        repository.save(log);
    }

    @Override
    public List<EntityChangeLog> getRecentChanges(String entityName, Long entityId) {
        if (entityName == null || entityId == null) {
            return Collections.emptyList();
        }
        return repository.findTop25ByEntityNameAndEntityIdOrderByCreatedAtDesc(entityName, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagablePage<AuditLogDTO> search(String q, String entityName, Long entityId, EntityChangeAction action,
                                           String changedBy, LocalDate fromDate, LocalDate toDate,
                                           Integer page, Integer size) {
        Specification<EntityChangeLog> specification = (root, query, builder) -> {
            var predicate = builder.conjunction();
            if (entityName != null && !entityName.isBlank()) {
                predicate = builder.and(predicate, builder.equal(builder.lower(root.get("entityName")), entityName.trim().toLowerCase(Locale.ROOT)));
            }
            if (entityId != null) predicate = builder.and(predicate, builder.equal(root.get("entityId"), entityId));
            if (action != null) predicate = builder.and(predicate, builder.equal(root.get("action"), action));
            if (changedBy != null && !changedBy.isBlank()) {
                predicate = builder.and(predicate, builder.like(builder.lower(root.get("createdBy")), "%" + changedBy.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (fromDate != null) predicate = builder.and(predicate,
                    builder.greaterThanOrEqualTo(root.<java.time.OffsetDateTime>get("createdAt"), fromDate.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime()));
            if (toDate != null) predicate = builder.and(predicate,
                    builder.lessThan(root.<java.time.OffsetDateTime>get("createdAt"), toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime()));
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                predicate = builder.and(predicate, builder.or(
                        builder.like(builder.lower(root.get("entityName")), term),
                        builder.like(root.get("entityId").as(String.class), term),
                        builder.like(builder.lower(root.get("action").as(String.class)), term),
                        builder.like(builder.lower(root.get("fieldName")), term),
                        builder.like(builder.lower(root.get("oldValue")), term),
                        builder.like(builder.lower(root.get("newValue")), term),
                        builder.like(builder.lower(root.get("createdBy")), term),
                        builder.like(builder.lower(root.get("remarks")), term)));
            }
            return predicate;
        };
        var request = PageRequest.of(PagablePage.normalizePage(page) - 1, PagablePage.normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        return PagablePage.from(repository.findAll(specification, request).map(AuditLogDTO::from));
    }

    private String stringify(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
