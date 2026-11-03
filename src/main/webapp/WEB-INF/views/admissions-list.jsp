<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="fragments/header.jspf" %>
<section class="page-heading">
    <div><p class="eyebrow">Admissions</p>
        <h1>Student Admissions</h1>
        <p class="muted">Review applications and admit successful applicants into the selected section.</p></div>
    <c:if test="${canWrite}"><c:url var="addUrl" value="/web/admissions/add"><c:if
            test="${systemAdmin and not empty selectedTenantId}"><c:param name="tenantId" value="${selectedTenantId}"/></c:if></c:url><a
            class="button primary" href="${addUrl}">New application</a></c:if></section>
<section class="filter-panel">
    <form class="filter-grid" method="get" action="<c:url value='/web/admissions'/>">
        <label>Search<input name="q" value="<c:out value='${q}'/>"
                            placeholder="Application, applicant, email or program"></label>
        <label>Status<select name="status">
            <option value="">All statuses</option>
            <c:forEach items="${statuses}" var="s">
                <option value="${s}" ${s == selectedStatus ? 'selected' : ''}>${s}</option>
            </c:forEach></select></label>
        <c:if test="${systemAdmin}"><label>Tenant<select name="tenantId">
            <option value="">All tenants</option>
            <c:forEach items="${tenants}" var="t">
                <option value="${t.id}" ${t.id == selectedTenantId ? 'selected' : ''}><c:out
                        value="${t.tenantName}"/></option>
            </c:forEach></select></label></c:if>
        <label>Rows<select name="size">
            <option value="10">10</option>
            <option value="25">25</option>
            <option value="50">50</option>
        </select></label>
        <div class="filter-actions">
            <button class="primary">Apply</button>
            <a class="button secondary" href="<c:url value='/web/admissions'/>">Reset</a></div>
    </form>
</section>
<section class="panel">
    <div class="table-wrap">
        <table class="data-table">
            <thead>
            <tr><c:if test="${systemAdmin}">
                <th>Tenant</th>
            </c:if>
                <th>Application</th>
                <th>Applicant</th>
                <th>Program / section</th>
                <th>Date</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${page.objects}" var="a">
                <tr><c:if test="${systemAdmin}">
                    <td><c:out value="${a.tenantName}"/></td>
                </c:if>
                    <td><c:out value="${a.applicationNumber}"/></td>
                    <td><c:out value="${a.firstName}"/> <c:out value="${a.lastName}"/><br><span class="muted"><c:out
                            value="${a.email}"/></span></td>
                    <td><c:out value="${a.programName}"/> / <c:out value="${a.sectionName}"/><br><span
                            class="muted"><c:out value="${a.academicYearName}"/> · <c:out
                            value="${a.semesterName}"/></span></td>
                    <td><c:out value="${a.applicationDate}"/></td>
                    <td><span class="status-pill status-${a.status}"><c:out value="${a.status}"/></span></td>
                    <td class="actions-cell"><c:url var="viewUrl" value="/web/admissions/${a.id}"><c:if
                            test="${systemAdmin}"><c:param name="tenantId" value="${a.tenantId}"/></c:if></c:url><a
                            class="action-button secondary" href="${viewUrl}">View</a><c:if
                            test="${canWrite and a.status == 'DRAFT'}"><c:url var="editUrl"
                                                                              value="/web/admissions/${a.id}/edit"><c:if
                            test="${systemAdmin}"><c:param name="tenantId" value="${a.tenantId}"/></c:if></c:url><a
                            class="action-button secondary" href="${editUrl}">Edit</a></c:if></td>
                </tr>
            </c:forEach></tbody>
        </table>
    </div>
    <%@ include file="fragments/data-table-empty.jspf" %>
    <%@ include file="fragments/pagination.jspf" %>
</section>
<%@ include file="fragments/footer.jspf" %>
