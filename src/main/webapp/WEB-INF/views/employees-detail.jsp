<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="fragments/header.jspf" %>
<c:url var="employeeListUrl" value="/web/employees"><c:if test="${systemAdmin}"><c:param
        name="tenantId" value="${employee.tenantId}"/></c:if></c:url>

<section class="tenant-detail-hero employee-detail-hero">
    <div class="tenant-identity">
        <span class="tenant-avatar employee-avatar"><i class="fa-solid fa-user-tie" aria-hidden="true"></i></span>
        <div>
            <span class="eyebrow">Employee profile</span>
            <h1><c:out value="${employee.firstName}"/><c:if test="${not empty employee.middleName}"> <c:out
                    value="${employee.middleName}"/></c:if> <c:out value="${employee.lastName}"/></h1>
            <div class="tenant-meta">
                <span><i class="fa-solid fa-id-badge" aria-hidden="true"></i><c:out
                        value="${employee.employeeNumber}"/></span>
                <span><i class="fa-solid fa-briefcase" aria-hidden="true"></i><c:out
                        value="${employee.employeeType}"/></span>
                <c:if test="${systemAdmin}">
                    <span><i class="fa-solid fa-building" aria-hidden="true"></i><c:out
                            value="${employee.tenantName}"/></span>
                </c:if>
            </div>
        </div>
    </div>
    <div class="tenant-hero-actions employee-hero-actions">
        <span class="status-pill status-${employee.status}"><c:out value="${employee.status}"/></span>
        <a class="button secondary" href="${employeeListUrl}"><i class="fa-solid fa-arrow-left"
                                                                    aria-hidden="true"></i>Back</a>
    </div>
</section>

<section class="tenant-overview-grid employee-overview-grid">
    <article class="tenant-info-panel">
        <div class="tenant-section-heading">
            <span><i class="fa-solid fa-address-card" aria-hidden="true"></i></span>
            <div>
                <h2>Contact information</h2>
                <p>Employee communication details</p>
            </div>
        </div>
        <div class="tenant-detail-list">
            <div><span>Email</span><strong><c:out
                    value="${empty employee.email ? 'Not provided' : employee.email}"/></strong></div>
            <div><span>Phone</span><strong><c:out
                    value="${empty employee.phone ? 'Not provided' : employee.phone}"/></strong></div>
        </div>
    </article>

    <article class="tenant-info-panel">
        <div class="tenant-section-heading">
            <span><i class="fa-solid fa-briefcase" aria-hidden="true"></i></span>
            <div>
                <h2>Employment details</h2>
                <p>Role and department information</p>
            </div>
        </div>
        <div class="tenant-detail-list">
            <div><span>Designation</span><strong><c:out
                    value="${empty employee.designation ? 'Not provided' : employee.designation}"/></strong></div>
            <div><span>Department</span><strong><c:out
                    value="${empty employee.departmentName ? 'Not assigned' : employee.departmentName}"/></strong></div>
            <div><span>Employee type</span><strong><c:out value="${employee.employeeType}"/></strong></div>
            <div><span>Hire date</span><strong><c:out
                    value="${empty employee.hireDate ? 'Not provided' : employee.hireDate}"/></strong></div>
            <c:if test="${systemAdmin}">
                <div><span>Tenant</span><strong><c:out value="${employee.tenantName}"/></strong></div>
            </c:if>
        </div>
    </article>
</section>

<section class="tenant-info-panel tenant-audit-panel">
    <div class="tenant-section-heading">
        <span><i class="fa-solid fa-clock-rotate-left" aria-hidden="true"></i></span>
        <div>
            <h2>Change history</h2>
            <p>Recent updates recorded for this employee</p>
        </div>
    </div>
    <div class="table-wrap">
        <table class="data-table tenant-log-table employee-log-table">
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
                    <td class="empty-cell" colspan="7">No employee change history found.</td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>
</section>

<c:if test="${canWrite}">
    <section class="employee-delete-actions">
        <form method="post" action="${employeeDeleteUrl}">
            <button class="secondary" type="submit"
                    onclick="return confirm('Delete this employee profile?')">
                <i class="fa-solid fa-trash" aria-hidden="true"></i>Delete profile
            </button>
        </form>
    </section>
</c:if>
<%@ include file="fragments/footer.jspf" %>
