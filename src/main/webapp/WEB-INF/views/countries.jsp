<%@ include file="fragments/header.jspf" %>
<%--<c:set var="pageEyebrow" value="Location setup"/>--%>
<%--<c:set var="pageTitle" value="Countries"/>--%>
<%--<c:set var="pageDescription" value="Manage country master data used by tenants and addresses."/>--%>
<%--<c:set var="pageActionUrl" value="/web/countries/add"/>--%>
<%--<c:set var="pageActionText" value="Add country"/>--%>
<%--<%@ include file="fragments/page-header.jspf" %>--%>
<section class="filter-panel">
    <form class="filter-grid tenant-filter-grid" action="<c:url value='/web/countries'/>" method="get">
        <c:set var="filterActionUrl" value="/web/countries"/>
        <%@ include file="fragments/search-filter.jspf" %>
    </form>
</section>
<section class="panel">
    <div class="table-wrap">
        <table class="data-table">
            <thead><tr><th>ID</th><th>Name</th><th>ISO code</th><th>Action</th></tr></thead>
            <tbody><c:forEach items="${page.objects}" var="country">
                <tr>
                    <td>${country.id}</td>
                    <td><c:out value="${country.name}"/></td>
                    <td><c:out value="${country.isoCode}"/></td>
                    <td class="actions-cell">
                        <form class="action-form" action="<c:url value='/web/countries/${country.id}/delete'/>" method="post">
                            <button class="action-button danger" type="submit" title="Delete country" aria-label="Delete country">
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
