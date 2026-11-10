<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="fragments/header.jspf" %>
<section class="form-panel"><c:choose><c:when test="${isEdit}"><c:url var="formAction"
                                                                      value="/web/employees/${id}"/></c:when><c:otherwise><c:url
        var="formAction" value="/web/employees"/></c:otherwise></c:choose>
    <form class="form-grid enterprise-form" action="${formAction}" method="post">
        <c:if test="${systemAdmin}"><c:choose><c:when test="${isEdit}"><input type="hidden" name="tenantId"
                                                                              value="${tenantId}"></c:when><c:otherwise><label>Tenant<select
                id="tenantSelect" name="tenantId" required>
            <option value="">Select tenant</option>
            <c:forEach items="${tenants}" var="t">
                <option value="${t.id}" ${t.id == tenantId ? 'selected' : ''}><c:out value="${t.tenantName}"/></option>
            </c:forEach></select></label></c:otherwise></c:choose></c:if>
        <label>Employee number<input name="employeeNumber" required maxlength="50"
                                     value="<c:out value='${employee.employeeNumber}'/>"/></label><label>Category<select
            name="employeeType" required>
        <option value="">Select category</option>
        <c:forEach items="${employeeTypes}" var="t">
            <option value="${t}" ${t == employee.employeeType ? 'selected' : ''}>${t}</option>
        </c:forEach></select></label><label>First name<input name="firstName" required maxlength="100"
                                                             value="<c:out value='${employee.firstName}'/>"/></label><label>Middle
        name<input name="middleName" maxlength="100" value="<c:out value='${employee.middleName}'/>"/></label><label>Last
        name<input name="lastName" required maxlength="100"
                   value="<c:out value='${employee.lastName}'/>"/></label><label>Email<input type="email" name="email"
                                                                                             maxlength="200"
                                                                                             value="<c:out value='${employee.email}'/>"/></label><label>Phone<input
            name="phone" maxlength="30" value="<c:out value='${employee.phone}'/>"/></label><label>Designation<input
            name="designation" required maxlength="160"
            value="<c:out value='${employee.designation}'/>"/></label><label>Department<select name="departmentId">
        <option value="">No department</option>
        <c:forEach items="${departments}" var="d">
            <option value="${d.id}" ${d.id == employee.departmentId ? 'selected' : ''}><c:out
                    value="${d.name}"/></option>
        </c:forEach></select></label><label>Hire date<input type="date" name="hireDate"
                                                            value="<c:out value='${employee.hireDate}'/>"/></label><label>Status<select
            name="status" required><c:forEach items="${statuses}" var="s">
        <option value="${s}" ${s == employee.status ? 'selected' : ''}>${s}</option>
    </c:forEach></select></label><c:if test="${isEdit}"><label class="wide-field">Remarks<textarea name="remarks"
                                                                                                   rows="2"></textarea></label></c:if>
        <div class="form-actions">
            <button class="primary">${isEdit ? 'Save changes' : 'Create employee'}</button>
            <a class="button secondary" href="<c:url value='/web/employees'/>">Cancel</a></div>
    </form>
</section>
<c:if test="${systemAdmin and not isEdit}">
    <script>document.getElementById('tenantSelect').addEventListener('change', function () {
        if (this.value) {
            window.location.assign('<c:url value="/web/employees/add"/>?tenantId=' + encodeURIComponent(this.value));
        }
    });</script>
</c:if>
<%@ include file="fragments/footer.jspf" %>
