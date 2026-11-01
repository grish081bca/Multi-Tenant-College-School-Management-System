<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="fragments/header.jspf" %>
<section class="page-heading">
    <div><p class="eyebrow">Admissions</p>
        <h1>${isEdit ? 'Edit' : 'New'} application</h1></div>
    <a class="button secondary" href="<c:url value='/web/admissions'/>">Back to list</a></section>
<section class="form-panel"><c:choose><c:when test="${isEdit}"><c:url var="formAction"
                                                                      value="/web/admissions/${id}"/></c:when><c:otherwise><c:url
        var="formAction" value="/web/admissions"/></c:otherwise></c:choose>
    <form class="form-grid enterprise-form" action="${formAction}" method="post">
        <c:if test="${systemAdmin}"><c:choose><c:when test="${isEdit}"><input type="hidden" name="tenantId"
                                                                              value="${tenantId}"></c:when><c:otherwise><label>Tenant<select
                id="tenantSelect" name="tenantId" required>
            <option value="">Select tenant</option>
            <c:forEach items="${tenants}" var="t">
                <option value="${t.id}" ${t.id == tenantId ? 'selected' : ''}><c:out value="${t.tenantName}"/></option>
            </c:forEach></select></label></c:otherwise></c:choose></c:if>
        <label>First name<input name="firstName" required maxlength="100"
                                value="<c:out value='${application.firstName}'/>"/></label><label>Middle name<input
            name="middleName" maxlength="100" value="<c:out value='${application.middleName}'/>"/></label><label>Last
        name<input name="lastName" required maxlength="100" value="<c:out value='${application.lastName}'/>"/></label>
        <label>Email<input type="email" name="email" maxlength="200"
                           value="<c:out value='${application.email}'/>"/></label><label>Phone<input name="phone"
                                                                                                     maxlength="30"
                                                                                                     value="<c:out value='${application.phone}'/>"/></label><label>Date
        of birth<input type="date" name="dateOfBirth" max="${today}"
                       value="<c:out value='${application.dateOfBirth}'/>"/></label><label>Application date<input
            type="date" name="applicationDate" required max="${today}"
            value="<c:out value='${application.applicationDate != null ? application.applicationDate : today}'/>"/></label>
        <label>Program<select name="programId" required>
            <option value="">Select program</option>
            <c:forEach items="${programs}" var="x">
                <option value="${x.id}" ${x.id == application.programId ? 'selected' : ''}><c:out value="${x.code}"/> ·
                    <c:out value="${x.name}"/></option>
            </c:forEach></select></label><label>Academic year<select name="academicYearId" required>
        <option value="">Select year</option>
        <c:forEach items="${academicYears}" var="x">
            <option value="${x.id}" ${x.id == application.academicYearId ? 'selected' : ''}><c:out
                    value="${x.name}"/></option>
        </c:forEach></select></label>
        <label>Semester<select name="semesterId" required>
            <option value="">Select semester</option>
            <c:forEach items="${semesters}" var="x">
                <option value="${x.id}" ${x.id == application.semesterId ? 'selected' : ''}><c:out
                        value="${x.name}"/></option>
            </c:forEach></select></label><label>Section<select name="sectionId" required>
        <option value="">Select section</option>
        <c:forEach items="${sections}" var="x">
            <option value="${x.id}" ${x.id == application.sectionId ? 'selected' : ''}><c:out value="${x.code}"/> ·
                <c:out value="${x.name}"/></option>
        </c:forEach></select></label>
        <label class="wide-field">Address<input name="address" maxlength="500"
                                                value="<c:out value='${application.address}'/>"/></label><label
            class="wide-field">Remarks<textarea name="remarks" rows="3" maxlength="1000"><c:out
            value="${application.remarks}"/></textarea></label>
        <div class="form-actions">
            <button class="primary">${isEdit ? 'Save changes' : 'Create draft'}</button>
            <a class="button secondary" href="<c:url value='/web/admissions'/>">Cancel</a></div>
    </form>
</section>
<c:if test="${systemAdmin and not isEdit}">
    <script>document.getElementById('tenantSelect').addEventListener('change', function () {
        if (this.value) {
            window.location.assign('<c:url value="/web/admissions/add"/>?tenantId=' + encodeURIComponent(this.value));
        }
    });</script>
</c:if>
<%@ include file="fragments/footer.jspf" %>
