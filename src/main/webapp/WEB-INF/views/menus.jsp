<%@ include file="fragments/header.jspf" %>
<%--<c:set var="pageEyebrow" value="Access administration"/>--%>
<%--<c:set var="pageTitle" value="Menus"/>--%>
<%--<c:set var="pageDescription" value="Manage dynamic super menus and sub-menus shown in the sidebar."/>--%>
<%--<c:set var="pageActionUrl" value="/web/menus/add"/>--%>
<%--<c:set var="pageActionText" value="Add menu"/>--%>
<%--<%@ include file="fragments/page-header.jspf" %>--%>
<section class="filter-panel">
    <form class="filter-grid tenant-filter-grid" action="<c:url value='/web/menus'/>" method="get">
        <label>Type
            <select name="menuType">
                <option value="">All types</option>
                <c:forEach items="${menuTypes}" var="type">
                    <option value="${type}" ${type == selectedMenuType ? 'selected' : ''}>${type}</option>
                </c:forEach>
            </select>
        </label>
        <label>Status
            <select name="status">
                <option value="">All statuses</option>
                <c:forEach items="${statuses}" var="status">
                    <option value="${status}" ${status == selectedStatus ? 'selected' : ''}>${status}</option>
                </c:forEach>
            </select>
        </label>
        <c:set var="filterActionUrl" value="/web/menus"/>
        <%@ include file="fragments/search-filter.jspf" %>
    </form>
</section>
<section class="panel">
    <div class="table-wrap">
        <table class="data-table">
            <thead><tr><th>Code</th><th>Name</th><th>URL</th><th>Parent</th><th>Type</th><th>Status</th><th>Action</th></tr></thead>
            <tbody><c:forEach items="${page.objects}" var="menu">
                <tr>
                    <td><c:out value="${menu.menuCode}"/></td>
                    <td><c:out value="${menu.name}"/></td>
                    <td><c:out value="${menu.menuUrl}"/></td>
                    <td><c:out value="${menu.parentMenuName}"/></td>
                    <td><c:out value="${menu.menuType}"/></td>
                    <td><c:out value="${menu.status}"/></td>
                    <td class="actions-cell">
                        <button class="action-button secondary menu-status-trigger" type="button"
                                data-action="<c:url value='/web/menus/${menu.id}/status'/>"
                                data-status="${menu.status}"
                                data-name="<c:out value='${menu.name}'/>"
                                title="Change menu status" aria-label="Change menu status">
                            <i class="fa-solid fa-toggle-on" aria-hidden="true"></i>
                            <span>Change status</span>
                        </button>
                    </td>
                </tr>
            </c:forEach></tbody>
        </table>
    </div>
    <%@ include file="fragments/data-table-empty.jspf" %>
    <%@ include file="fragments/pagination.jspf" %>
</section>
<dialog class="status-dialog" id="menuStatusDialog">
    <form id="menuStatusForm" method="post">
        <div class="dialog-header">
            <h2>Change menu status</h2>
            <button class="icon-button dialog-close" type="button" aria-label="Close dialog">
                <i class="fa-solid fa-xmark" aria-hidden="true"></i>
            </button>
        </div>
        <p class="muted" id="menuStatusName"></p>
        <label>Status
            <select name="status" id="menuStatusSelect" required>
                <c:forEach items="${statuses}" var="status">
                    <option value="${status}">${status}</option>
                </c:forEach>
            </select>
        </label>
        <div class="form-actions dialog-actions">
            <button class="primary" type="submit">Update status</button>
            <button class="secondary dialog-close" type="button">Cancel</button>
        </div>
    </form>
</dialog>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        var dialog = document.getElementById('menuStatusDialog');
        var form = document.getElementById('menuStatusForm');
        var statusSelect = document.getElementById('menuStatusSelect');
        var menuName = document.getElementById('menuStatusName');

        document.querySelectorAll('.menu-status-trigger').forEach(function (button) {
            button.addEventListener('click', function () {
                form.action = button.dataset.action;
                statusSelect.value = button.dataset.status;
                menuName.textContent = button.dataset.name || '';
                if (dialog.showModal) {
                    dialog.showModal();
                } else {
                    dialog.setAttribute('open', 'open');
                }
            });
        });

        document.querySelectorAll('.dialog-close').forEach(function (button) {
            button.addEventListener('click', function () {
                dialog.close();
            });
        });
    });
</script>
<%@ include file="fragments/footer.jspf" %>
