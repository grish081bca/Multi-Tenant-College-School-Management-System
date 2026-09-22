<%@ include file="fragments/header.jspf" %>
<%--<c:set var="pageEyebrow" value="Location setup"/>--%>
<%--<c:set var="pageTitle" value="States"/>--%>
<%--<c:set var="pageDescription" value="Manage states and provinces under configured countries."/>--%>
<%--<c:set var="pageActionUrl" value="/web/states/add"/>--%>
<%--<c:set var="pageActionText" value="Add state"/>--%>
<%--<%@ include file="fragments/page-header.jspf" %>--%>
<section class="filter-panel">
    <form class="filter-grid tenant-filter-grid" action="<c:url value='/web/states'/>" method="get">
        <label>Country
            <select name="countryId">
                <option value="">All countries</option>
                <c:forEach items="${countries}" var="country">
                    <option value="${country.id}" ${country.id == countryId ? 'selected' : ''}><c:out value="${country.name}"/></option>
                </c:forEach>
            </select>
        </label>
        <c:set var="filterActionUrl" value="/web/states"/>
        <%@ include file="fragments/search-filter.jspf" %>
    </form>
</section>
<section class="panel">
    <div class="table-wrap">
        <table class="data-table">
            <thead><tr><th>ID</th><th>Name</th><th>Country</th><th>Action</th></tr></thead>
            <tbody><c:forEach items="${page.objects}" var="state">
                <tr>
                    <td>${state.id}</td>
                    <td><c:out value="${state.name}"/></td>
                    <td><c:out value="${state.countryName}"/></td>
                    <td class="actions-cell">
                        <form class="action-form" action="<c:url value='/web/states/${state.id}/delete'/>" method="post">
                            <button class="action-button danger" type="submit" title="Delete state" aria-label="Delete state">
                                <i class="fa-solid fa-trash" aria-hidden="true"></i>
                                <span>Delete</span>
                            </button>
                        </form>
                    </td>
                </tr>
            </c:forEach></tbody>
        </table>
    </div>
    <%@ include file="fragments/data-table-empty.jspf" %>
    <%@ include file="fragments/pagination.jspf" %>
</section>
<%@ include file="fragments/footer.jspf" %>
