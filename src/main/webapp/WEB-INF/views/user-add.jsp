<%@ include file="fragments/header.jspf" %>
<section class="form-panel">
    <c:choose>
        <c:when test="${not empty user.id}">
            <c:url var="userFormAction" value="/web/users/${user.id}"/>
        </c:when>
        <c:otherwise>
            <c:url var="userFormAction" value="/web/users"/>
        </c:otherwise>
    </c:choose>
    <form class="form-grid enterprise-form" action="${userFormAction}" method="post">
        <input type="hidden" name="id" value="<c:out value='${user.id}'/>">
        <label>Tenant
            <select name="tenantId">
                <option value="">No tenant</option>
                <c:forEach items="${tenants}" var="tenant">
                    <option value="${tenant.id}" ${tenant.id == user.tenantId ? 'selected' : ''}>
                        <c:out value="${tenant.tenantName}"/>
                    </option>
                </c:forEach>
            </select>
        </label>
        <label>Username<input name="username" value="<c:out value='${user.username}'/>" required></label>
        <label>Email<input type="email" name="email" value="<c:out value='${user.email}'/>" required></label>
        <label>First name<input name="firstName" value="<c:out value='${user.firstName}'/>" required></label>
        <label>Middle name (optional)<input name="middleName" value="<c:out value='${user.middleName}'/>"></label>
        <label>Last name<input name="lastName" value="<c:out value='${user.lastName}'/>" required></label>
        <label>Phone<input name="phoneNumber" value="<c:out value='${user.phoneNumber}'/>"></label>
        <label>Password
            <c:choose>
                <c:when test="${not empty user.id}">
                    <input name="password" type="password" placeholder="Leave blank to keep current password">
                </c:when>
                <c:otherwise>
                    <input name="password" type="password" placeholder="Defaults to changeme if blank">
                </c:otherwise>
            </c:choose>
        </label>
        <label>User type
            <select name="userType" required>
                <option value="">Select user type</option>
                <c:forEach items="${userTypes}" var="type">
                    <option value="${type}" ${type == user.userType ? 'selected' : ''}>${type}</option>
                </c:forEach>
            </select>
        </label>
        <label>User template
            <select name="userTemplateId">
                <option value="">None</option>
                <c:forEach items="${userTemplates}" var="template">
                    <option value="${template.id}" ${user.userTemplateId != null and user.userTemplateId == template.id ? 'selected' : ''}>
                        <c:out value="${template.userType}"/><c:if test="${not empty template.menuTemplateName}"> - <c:out value="${template.menuTemplateName}"/></c:if>
                    </option>
                </c:forEach>
            </select>
        </label>
        <label>Status
            <select name="status" required>
                <option value="">Select status</option>
                <c:forEach items="${statuses}" var="status">
                    <option value="${status}" ${status == user.status ? 'selected' : ''}>${status}</option>
                </c:forEach>
            </select>
        </label>
        <label>Enabled
            <select name="enabled">
                <option value="true" ${user.enabled ? 'selected' : ''}>Enabled</option>
                <option value="false" ${!user.enabled ? 'selected' : ''}>Disabled</option>
            </select>
        </label>
        <div class="form-actions">
            <button class="primary" type="submit">${not empty user.id ? 'Update user' : 'Add user'}</button>
            <a class="button secondary" href="<c:url value='/web/users'/>">Cancel</a>
        </div>
    </form>
</section>
<%@ include file="fragments/footer.jspf" %>
