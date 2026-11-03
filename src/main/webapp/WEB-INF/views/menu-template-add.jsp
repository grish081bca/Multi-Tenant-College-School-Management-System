<%@ include file="fragments/header.jspf" %>
<%--<c:set var="pageEyebrow" value="Access administration"/>--%>
<%--<c:set var="pageTitle" value="Add menu template"/>--%>
<%--<c:set var="pageDescription" value="Assign a menu to a user type and tenant scope."/>--%>
<%--<c:set var="pageActionUrl" value="/web/menu-templates"/>--%>
<%--<c:set var="pageActionText" value="List menu templates"/>--%>
<%--<%@ include file="fragments/page-header.jspf" %>--%>
<section class="form-panel">
    <c:choose>
        <c:when test="${editMode}"><c:url var="templateFormAction" value="/web/menu-templates/${templateRequest.id}"/></c:when>
        <c:otherwise><c:url var="templateFormAction" value="/web/menu-templates"/></c:otherwise>
    </c:choose>
    <form class="form-grid enterprise-form" action="${templateFormAction}" method="post">
        <label>Template name<input name="templateName" value="<c:out value='${templateRequest.templateName}'/>" required maxlength="150" placeholder="College admin menus"></label>
        <label>User type<select name="userType" required>
            <c:forEach items="${userTypes}" var="type"><option value="${type}" ${type == selectedUserType ? 'selected' : ''}>${type}</option></c:forEach>
        </select></label>
        <label>Status<select name="status">
            <c:forEach items="${statuses}" var="status"><option value="${status}" ${status == selectedStatus ? 'selected' : ''}>${status}</option></c:forEach>
        </select></label>
        <fieldset class="checkbox-field">
            <legend>Menus</legend>
            <div class="checkbox-grid">
                <c:forEach items="${menus}" var="menu">
                    <label class="checkbox-option">
                        <input type="checkbox" name="menuIds" value="${menu.id}" <c:if test="${templateRequest.menuIds.contains(menu.id)}">checked</c:if>>
                        <span>
                            <c:choose>
                                <c:when test="${empty menu.parentMenuName}"><c:out value="${menu.name}"/></c:when>
                                <c:otherwise><c:out value="${menu.parentMenuName}"/> / <c:out value="${menu.name}"/></c:otherwise>
                            </c:choose>
                        </span>
                    </label>
                </c:forEach>
            </div>
        </fieldset>
        <div class="form-actions">
            <button class="primary" type="submit"><c:choose><c:when test="${editMode}">Update menu template</c:when><c:otherwise>Add menu template</c:otherwise></c:choose></button>
            <a class="button secondary" href="<c:url value='/web/menu-templates'/>">Cancel</a>
        </div>
    </form>
</section>
<%@ include file="fragments/footer.jspf" %>
