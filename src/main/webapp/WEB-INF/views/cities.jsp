<%@ include file="fragments/header.jspf" %>
<%--<c:set var="pageEyebrow" value="Location setup"/>--%>
<%--<c:set var="pageTitle" value="Cities"/>--%>
<%--<c:set var="pageDescription" value="Manage city master data and state mapping."/>--%>
<%--<c:set var="pageActionUrl" value="/web/cities/add"/>--%>
<%--<c:set var="pageActionText" value="Add city"/>--%>
<%--<%@ include file="fragments/page-header.jspf" %>--%>
<section class="filter-panel">
    <form class="filter-grid tenant-filter-grid" action="<c:url value='/web/cities'/>" method="get">
        <label>State
            <select name="stateId">
                <option value="">All states</option>
                <c:forEach items="${states}" var="state">
                    <option value="${state.id}" ${state.id == stateId ? 'selected' : ''}><c:out value="${state.name}"/> - <c:out value="${state.countryName}"/></option>
                </c:forEach>
            </select>
        </label>
        <c:set var="filterActionUrl" value="/web/cities"/>
        <%@ include file="fragments/search-filter.jspf" %>
    </form>
</section>
<section class="panel">
    <div class="table-wrap">
        <table class="data-table">
            <thead><tr><th>ID</th><th>Name</th><th>State</th><th>Country</th><th>Action</th></tr></thead>
            <tbody><c:forEach items="${page.objects}" var="city">
                <tr>
                    <td>${city.id}</td>
                    <td><c:out value="${city.name}"/></td>
                    <td><c:out value="${city.stateName}"/></td>
                    <td><c:out value="${city.countryName}"/></td>
                    <td class="actions-cell">
                        <form class="action-form" action="<c:url value='/web/cities/${city.id}/delete'/>" method="post">
                            <button class="action-button danger" type="submit" title="Delete city" aria-label="Delete city">
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
