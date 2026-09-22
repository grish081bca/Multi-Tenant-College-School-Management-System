<%@ include file="fragments/header.jspf" %>
<section class="tenant-detail-hero">
    <div class="tenant-identity">
        <span class="tenant-avatar"><i class="fa-solid fa-user" aria-hidden="true"></i></span>
        <div>
            <span class="eyebrow">User profile</span>
            <h1>
                <c:out value="${user.firstName}"/>
                <c:if test="${not empty user.middleName}"> <c:out value="${user.middleName}"/></c:if>
                <c:if test="${not empty user.lastName}"> <c:out value="${user.lastName}"/></c:if>
            </h1>
            <div class="tenant-meta">
                <span><i class="fa-solid fa-at" aria-hidden="true"></i><c:out value="${user.username}"/></span>
                <span><i class="fa-solid fa-envelope" aria-hidden="true"></i><c:out value="${user.email}"/></span>
            </div>
        </div>
    </div>
    <div class="tenant-hero-actions">
        <span class="status-pill status-${user.status}"><c:out value="${user.status}"/></span>
        <a class="button secondary" href="<c:url value='/web/users'/>"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i>Back</a>
        <a class="button primary" href="<c:url value='/web/users/${user.id}/edit'/>"><i class="fa-solid fa-pen-to-square" aria-hidden="true"></i>Edit</a>
    </div>
</section>

<section class="tenant-overview-grid">
    <article class="tenant-info-panel">
        <div class="tenant-section-heading">
            <span><i class="fa-solid fa-address-card" aria-hidden="true"></i></span>
            <div>
                <h2>Contact</h2>
                <p>Identity and communication details</p>
            </div>
        </div>
        <div class="tenant-detail-list">
            <div><span>Username</span><strong><c:out value="${user.username}"/></strong></div>
            <div><span>Email</span><strong><c:out value="${user.email}"/></strong></div>
            <div><span>Phone</span><strong><c:out value="${empty user.phoneNumber ? 'Not provided' : user.phoneNumber}"/></strong></div>
            <div><span>Full name</span><strong><c:out value="${user.firstName}"/> <c:if test="${not empty user.middleName}"><c:out value="${user.middleName}"/> </c:if><c:out value="${user.lastName}"/></strong></div>
        </div>
    </article>

    <article class="tenant-info-panel">
        <div class="tenant-section-heading">
            <span><i class="fa-solid fa-shield-halved" aria-hidden="true"></i></span>
            <div>
                <h2>Access</h2>
                <p>Tenant, template and account status</p>
            </div>
        </div>
        <div class="tenant-detail-list">
            <div><span>Tenant</span><strong><c:out value="${empty user.tenantName ? 'No tenant' : user.tenantName}"/></strong></div>
            <div><span>User type</span><strong><c:out value="${user.userType}"/></strong></div>
            <div><span>User template</span><strong><c:out value="${empty user.userTemplateName ? 'None' : user.userTemplateName}"/></strong></div>
            <div><span>Enabled</span><strong><c:out value="${user.enabled ? 'Enabled' : 'Disabled'}"/></strong></div>
            <div><span>Created date</span><strong><c:out value="${empty user.createdDate ? 'Not available' : user.createdDate}"/></strong></div>
            <div><span>Updated date</span><strong><c:out value="${empty user.updatedDate ? 'Not available' : user.updatedDate}"/></strong></div>
        </div>
    </article>
</section>
<%@ include file="fragments/footer.jspf" %>
