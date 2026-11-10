<%@ include file="fragments/header.jspf" %>
<%--<c:set var="pageEyebrow" value="Access administration"/>--%>
<%--<c:set var="pageTitle" value="Add user template"/>--%>
<%--<c:set var="pageDescription" value="Assign an allowed user type to a tenant."/>--%>
<%--<c:set var="pageActionUrl" value="/web/user-templates"/>--%>
<%--<c:set var="pageActionText" value="List user templates"/>--%>
<%--<%@ include file="fragments/page-header.jspf" %>--%>
<section class="form-panel">
    <form class="form-grid enterprise-form" action="<c:url value='/web/user-templates'/>" method="post">
        <label>User type<select name="userType" required>
            <c:forEach items="${userTypes}" var="type"><option value="${type}">${type}</option></c:forEach>
        </select></label>
        <label>Menu template<select name="menuTemplateId">
            <option value="">Use default for user type</option>
            <c:forEach items="${menuTemplates}" var="menuTemplate">
                <option value="${menuTemplate.id}">${menuTemplate.name} - ${menuTemplate.userType}</option>
            </c:forEach>
        </select></label>
        <label>Status<select name="status">
            <c:forEach items="${statuses}" var="status"><option value="${status}">${status}</option></c:forEach>
        </select></label>
        <div class="form-actions">
            <button class="primary" type="submit">Add user template</button>
            <a class="button secondary" href="<c:url value='/web/user-templates'/>">Cancel</a>
        </div>
    </form>
</section>
<%@ include file="fragments/footer.jspf" %>
