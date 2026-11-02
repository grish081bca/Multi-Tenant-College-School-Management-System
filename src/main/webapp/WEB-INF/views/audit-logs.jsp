<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="fragments/header.jspf" %>
<section class="filter-panel">
    <form class="filter-grid" action="<c:url value='/web/audit-logs'/>" method="get">
        <label>Search<input name="q" value="<c:out value='${q}'/>"
                            placeholder="Entity, user, field, or remarks"></label>
        <label>Entity<input name="entityName" value="<c:out value='${entityName}'/>" placeholder="For example, Student"></label>
        <label>Entity ID<input type="number" min="1" name="entityId" value="<c:out value='${entityId}'/>"></label>
        <label>Action<select name="action">
            <option value="">All actions</option>
            <c:forEach items="${actions}" var="value">
                <option value="${value}" ${value == selectedAction ? 'selected' : ''}><c:out value="${value}"/></option>
            </c:forEach></select></label>
        <label>Changed by<input name="changedBy" value="<c:out value='${changedBy}'/>" placeholder="Username"></label>
        <label>From date<input type="date" name="fromDate" value="<c:out value='${fromDate}'/>"></label>
        <label>To date<input type="date" name="toDate" value="<c:out value='${toDate}'/>"></label>
        <label>Rows<select name="size">
            <option value="10" ${pageSize == 10 ? 'selected' : ''}>10</option>
            <option value="25" ${pageSize == 25 ? 'selected' : ''}>25</option>
            <option value="50" ${pageSize == 50 ? 'selected' : ''}>50</option>
            <option value="100" ${pageSize == 100 ? 'selected' : ''}>100</option>
        </select></label>
        <div class="filter-actions">
            <button class="primary" type="submit">Apply</button>
            <a class="button secondary" href="<c:url value='/web/audit-logs'/>">Reset</a></div>
    </form>
</section>
<c:if test="${empty error and not empty page}">
    <section class="panel">
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th>Date and time</th>
                    <th>Actor</th>
                    <th>Action</th>
                    <th>Entity</th>
                    <th>Entity ID</th>
                    <th>Field</th>
                    <th>Previous value</th>
                    <th>New value</th>
                    <th>Remarks</th>
                </tr>
                </thead>
                <tbody><c:forEach items="${page.objects}" var="log">
                    <tr>
                        <td><c:out value="${log.createdAtDisplay}"/></td>
                        <td><c:out value="${log.createdBy}"/></td>
                        <td><c:out value="${log.action}"/></td>
                        <td><c:out value="${log.entityName}"/></td>
                        <td><c:out value="${log.entityId}"/></td>
                        <td><c:out value="${log.fieldName}"/></td>
                        <td><c:out value="${log.oldValue}"/></td>
                        <td><c:out value="${log.newValue}"/></td>
                        <td><c:out value="${log.remarks}"/></td>
                    </tr>
                </c:forEach></tbody>
            </table>
        </div>
        <c:if test="${empty page.objects}"><p class="empty-state">No audit logs match these filters.</p></c:if>
        <%@ include file="fragments/pagination.jspf" %>
    </section>
</c:if>
<c:if test="${not empty error}">
    <section class="panel"><p class="alert error"><c:out value="${error}"/></p></section>
</c:if>
<%@ include file="fragments/footer.jspf" %>
