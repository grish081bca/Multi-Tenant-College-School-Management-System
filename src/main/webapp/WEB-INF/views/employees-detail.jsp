<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="fragments/header.jspf" %>
<section class="page-heading">
    <div><p class="eyebrow">People</p>
        <h1><c:out value="${employee.firstName}"/> <c:out value="${employee.lastName}"/></h1>
        <p class="muted"><c:out value="${employee.employeeNumber}"/> · <c:out value="${employee.employeeType}"/> ·
            <c:out value="${employee.tenantName}"/></p></div>
    <a class="button secondary" href="<c:url value='/web/employees'/>">Back to list</a></section>
<section class="panel detail-grid">
    <div><span>Designation</span><strong><c:out value="${employee.designation}"/></strong></div>
    <div><span>Department</span><strong><c:out value="${employee.departmentName}"/></strong></div>
    <div><span>Email / phone</span><strong><c:out value="${employee.email}"/> · <c:out
            value="${employee.phone}"/></strong></div>
    <div><span>Hire date</span><strong><c:out value="${employee.hireDate}"/></strong></div>
    <div><span>Status</span><strong><c:out value="${employee.status}"/></strong></div>
</section>
<c:if test="${canWrite}">
    <section class="panel">
        <div class="form-actions"><c:url var="editUrl" value="/web/employees/${employee.id}/edit"><c:if
                test="${systemAdmin}"><c:param name="tenantId" value="${employee.tenantId}"/></c:if></c:url><a
                class="button secondary" href="${editUrl}">Edit profile</a><c:url var="deleteUrl"
                                                                                  value="/web/employees/${employee.id}/delete"><c:if
                test="${systemAdmin}"><c:param name="tenantId" value="${employee.tenantId}"/></c:if></c:url>
            <form method="post" action="${deleteUrl}">
                <button class="secondary" onclick="return confirm('Delete this employee profile?')">Delete profile
                </button>
            </form>
            <c:url var="statusUrl" value="/web/employees/${employee.id}/status"><c:if test="${systemAdmin}"><c:param
                    name="tenantId" value="${employee.tenantId}"/></c:if></c:url>
            <form method="post" action="${statusUrl}"><select name="status"><c:forEach items="${statuses}" var="s">
                <option value="${s}" ${s == employee.status ? 'selected' : ''}>${s}</option>
            </c:forEach></select>
                <button class="secondary">Update status</button>
            </form>
        </div>
    </section>
</c:if>
<section class="panel"><h2>Change history</h2>
    <div class="table-wrap">
        <table class="data-table">
            <thead>
            <tr>
                <th>Date</th>
                <th>Action</th>
                <th>Field</th>
                <th>Change</th>
                <th>By</th>
            </tr>
            </thead>
            <tbody><c:forEach items="${changeLogs}" var="log">
                <tr>
                    <td><c:out value="${log.createdAtDisplay}"/></td>
                    <td><c:out value="${log.action}"/></td>
                    <td><c:out value="${log.fieldName}"/></td>
                    <td><c:out value="${log.oldValue}"/> → <c:out value="${log.newValue}"/></td>
                    <td><c:out value="${log.createdBy}"/></td>
                </tr>
            </c:forEach></tbody>
        </table>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
