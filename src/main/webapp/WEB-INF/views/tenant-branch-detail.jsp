<%@ include file="fragments/header.jspf" %>
<section class="tenant-detail-hero">
    <div class="tenant-identity">
        <span class="tenant-avatar"><i class="fa-solid fa-code-branch" aria-hidden="true"></i></span>
        <div>
            <span class="eyebrow">Tenant branch profile</span>
            <h1><c:out value="${tenant.tenantName}"/></h1>
            <div class="tenant-meta">
                <span><i class="fa-solid fa-building" aria-hidden="true"></i><c:out value="${tenant.parentTenantName}"/></span>
                <span><i class="fa-solid fa-hashtag" aria-hidden="true"></i><c:out value="${tenant.tenantCode}"/></span>
                <span><i class="fa-solid fa-location-dot" aria-hidden="true"></i><c:out
                        value="${tenant.cityName}"/><c:if test="${not empty tenant.stateName}">, <c:out
                        value="${tenant.stateName}"/></c:if></span>
            </div>
        </div>
    </div>
    <div class="tenant-hero-actions">
        <span class="status-pill status-${tenant.status}"><c:out value="${tenant.status}"/></span>
        <a class="button secondary" href="<c:url value='/web/tenant-branches'/>"><i class="fa-solid fa-arrow-left"
                                                                                    aria-hidden="true"></i>Back</a>
        <a class="button primary" href="<c:url value='/web/tenant-branches/${tenant.id}/edit'/>"><i
                class="fa-solid fa-pen-to-square" aria-hidden="true"></i>Edit</a>
    </div>
</section>

<section class="tenant-overview-grid">
    <article class="tenant-info-panel">
        <div class="tenant-section-heading">
            <span><i class="fa-solid fa-address-card" aria-hidden="true"></i></span>
            <div>
                <h2>Contact</h2>
                <p>Primary and backup communication details</p>
            </div>
        </div>
        <div class="tenant-detail-list">
            <div><span>Main tenant</span><strong><c:out value="${tenant.parentTenantName}"/> - <c:out
                    value="${tenant.parentTenantCode}"/></strong></div>
            <div><span>Email</span><strong><c:out value="${tenant.contactEmail}"/></strong></div>
            <div><span>Secondary email</span><strong><c:out
                    value="${empty tenant.contactEmailSecondary ? 'Not provided' : tenant.contactEmailSecondary}"/></strong>
            </div>
            <div><span>Phone</span><strong><c:out value="${tenant.contactPhone}"/></strong></div>
            <div><span>Secondary phone</span><strong><c:out
                    value="${empty tenant.contactPhoneSecondary ? 'Not provided' : tenant.contactPhoneSecondary}"/></strong>
            </div>
        </div>
    </article>

    <article class="tenant-info-panel">
        <div class="tenant-section-heading">
            <span><i class="fa-solid fa-map-location-dot" aria-hidden="true"></i></span>
            <div>
                <h2>Location</h2>
                <p>Registered address and region</p>
            </div>
        </div>
        <div class="tenant-detail-list">
            <div><span>Address line 1</span><strong><c:out
                    value="${empty tenant.addressLine1 ? 'Not provided' : tenant.addressLine1}"/></strong></div>
            <div><span>Address line 2</span><strong><c:out
                    value="${empty tenant.addressLine2 ? 'Not provided' : tenant.addressLine2}"/></strong></div>
            <div><span>Country</span><strong><c:out value="${tenant.countryName}"/></strong></div>
            <div><span>State</span><strong><c:out value="${tenant.stateName}"/></strong></div>
            <div><span>City</span><strong><c:out value="${tenant.cityName}"/></strong></div>
            <div><span>Postal code</span><strong><c:out value="${tenant.postalCode}"/></strong></div>
        </div>
    </article>
</section>

<section class="tenant-info-panel tenant-audit-panel">
    <div class="tenant-section-heading">
        <span><i class="fa-solid fa-clock-rotate-left" aria-hidden="true"></i></span>
        <div>
            <h2>Entity Change Logs</h2>
            <p>Recent updates recorded for this branch</p>
        </div>
    </div>
    <div class="table-wrap">
        <table class="data-table tenant-log-table">
            <thead>
            <tr>
                <th>When</th>
                <th>Action</th>
                <th>Field</th>
                <th>Old value</th>
                <th>New value</th>
                <th>Remarks</th>
                <th>By</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${changeLogs}" var="log">
                <tr>
                    <td><c:out value="${log.createdAtDisplay}"/></td>
                    <td><span class="log-action"><c:out value="${log.action}"/></span></td>
                    <td><c:out value="${empty log.fieldName ? '-' : log.fieldName}"/></td>
                    <td><c:out value="${empty log.oldValue ? '-' : log.oldValue}"/></td>
                    <td><c:out value="${empty log.newValue ? '-' : log.newValue}"/></td>
                    <td><c:out value="${empty log.remarks ? '-' : log.remarks}"/></td>
                    <td><c:out value="${empty log.createdBy ? 'System' : log.createdBy}"/></td>
                </tr>
            </c:forEach>
            <c:if test="${empty changeLogs}">
                <tr>
                    <td class="empty-cell" colspan="7">No entity change logs found for this branch.</td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
