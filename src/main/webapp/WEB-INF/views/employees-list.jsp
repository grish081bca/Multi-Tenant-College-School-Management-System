<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="fragments/header.jspf" %>
<section class="page-heading">
    <div><p class="eyebrow">People</p>
        <h1>Teacher and Staff Management</h1>
        <p class="muted">Maintain tenant scoped teacher and staff profiles.</p></div>
    <c:if test="${canWrite}"><c:url var="addUrl" value="/web/employees/add"><c:if
            test="${systemAdmin and not empty selectedTenantId}"><c:param name="tenantId" value="${selectedTenantId}"/></c:if></c:url><a
            class="button primary" href="${addUrl}">Add employee</a></c:if></section>
<section class="filter-panel">
    <form class="filter-grid" method="get" action="<c:url value='/web/employees'/>"><label>Search<input name="q"
                                                                                                        value="<c:out value='${q}'/>"
                                                                                                        placeholder="Employee, name, email or role"></label><label>Type<select
            name="type">
        <option value="">Teachers and staff</option>
        <c:forEach items="${employeeTypes}" var="t">
            <option value="${t}" ${t == selectedType ? 'selected' : ''}>${t}</option>
        </c:forEach></select></label><c:if test="${systemAdmin}"><label>Tenant<select name="tenantId">
        <option value="">All tenants</option>
        <c:forEach items="${tenants}" var="t">
            <option value="${t.id}" ${t.id == selectedTenantId ? 'selected' : ''}><c:out
                    value="${t.tenantName}"/></option>
        </c:forEach></select></label></c:if><label>Rows<select name="size">
        <option value="10">10</option>
        <option value="25">25</option>
        <option value="50">50</option>
    </select></label>
        <div class="filter-actions">
            <button class="primary">Apply</button>
            <a class="button secondary" href="<c:url value='/web/employees'/>">Reset</a></div>
    </form>
</section>
<section class="panel">
    <div class="table-wrap">
        <table class="data-table">
            <thead>
            <tr><c:if test="${systemAdmin}">
                <th>Tenant</th>
            </c:if>
                <th>Employee no.</th>
                <th>Name</th>
                <th>Type / designation</th>
                <th>Department</th>
                <th>Contact</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody><c:forEach items="${page.objects}" var="e">
                <tr><c:if test="${systemAdmin}">
                    <td><c:out value="${e.tenantName}"/></td>
                </c:if>
                    <td><c:out value="${e.employeeNumber}"/></td>
                    <td><c:out value="${e.firstName}"/> <c:out value="${e.lastName}"/></td>
                    <td><c:out value="${e.employeeType}"/><br><span class="muted"><c:out
                            value="${e.designation}"/></span></td>
                    <td><c:out value="${e.departmentName}"/></td>
                    <td><c:out value="${e.email}"/><br><c:out value="${e.phone}"/></td>
                    <td><span class="status-pill"><c:out value="${e.status}"/></span></td>
                    <td class="actions-cell"><c:url var="viewUrl" value="/web/employees/${e.id}"><c:if
                            test="${systemAdmin}"><c:param name="tenantId" value="${e.tenantId}"/></c:if></c:url><a
                            class="action-button secondary" href="${viewUrl}">View</a><c:if test="${canWrite}"><c:url
                            var="editUrl" value="/web/employees/${e.id}/edit"><c:if test="${systemAdmin}"><c:param
                            name="tenantId" value="${e.tenantId}"/></c:if></c:url><a class="action-button secondary"
                                                                                     href="${editUrl}">Edit</a></c:if>
                    </td>
                </tr>
            </c:forEach></tbody>
        </table>
    </div>
    <%@ include file="fragments/data-table-empty.jspf" %>
    <%@ include file="fragments/pagination.jspf" %>
</section>
<%@ include file="fragments/footer.jspf" %>
