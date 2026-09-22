<%@ include file="fragments/header.jspf" %>
<%--<c:set var="pageEyebrow" value="Access administration"/>--%>
<%--<c:set var="pageTitle" value="Menu templates"/>--%>
<%--<c:set var="pageDescription" value="Assign active menus to user types globally or per tenant."/>--%>
<%--<c:set var="pageActionUrl" value="/web/menu-templates/add"/>--%>
<%--<c:set var="pageActionText" value="Add menu template"/>--%>
<%--<%@ include file="fragments/page-header.jspf" %>--%>
<section class="filter-panel">
    <form class="filter-grid tenant-filter-grid" action="<c:url value='/web/menu-templates'/>" method="get">
        <label>User type
            <select name="userType">
                <option value="">All user types</option>
                <c:forEach items="${userTypes}" var="type">
                    <option value="${type}" ${type == selectedUserType ? 'selected' : ''}>${type}</option>
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
        <c:set var="filterActionUrl" value="/web/menu-templates"/>
        <%@ include file="fragments/search-filter.jspf" %>
    </form>
</section>
<section class="panel">
    <div class="table-wrap">
        <table class="data-table">
            <thead><tr><th>Template</th><th>User type</th><th>Menus</th><th>Status</th><th>Action</th></tr></thead>
            <tbody><c:forEach items="${page.objects}" var="template">
                <tr>
                    <td><c:out value="${template.name}"/></td>
                    <td><c:out value="${template.userType}"/></td>
                    <td><c:out value="${template.menuName}"/></td>
                    <td><c:out value="${template.status}"/></td>
                    <td class="actions-cell">
                        <button class="action-button secondary menu-template-status-trigger" type="button"
                                data-action="<c:url value='/web/menu-templates/${template.id}/status'/>"
                                data-status="${template.status}"
                                data-name="<c:out value='${template.name}'/>"
                                title="Change menu template status" aria-label="Change menu template status">
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
<dialog class="status-dialog" id="menuTemplateStatusDialog">
    <form id="menuTemplateStatusForm" method="post">
        <div class="dialog-header">
            <h2>Change menu template status</h2>
            <button class="icon-button dialog-close" type="button" aria-label="Close dialog">
                <i class="fa-solid fa-xmark" aria-hidden="true"></i>
            </button>
        </div>
        <p class="muted" id="menuTemplateStatusName"></p>
        <label>Status
            <select name="status" id="menuTemplateStatusSelect" required>
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
        var dialog = document.getElementById('menuTemplateStatusDialog');
        var form = document.getElementById('menuTemplateStatusForm');
        var statusSelect = document.getElementById('menuTemplateStatusSelect');
        var templateName = document.getElementById('menuTemplateStatusName');

        document.querySelectorAll('.menu-template-status-trigger').forEach(function (button) {
            button.addEventListener('click', function () {
                form.action = button.dataset.action;
                statusSelect.value = button.dataset.status;
                templateName.textContent = button.dataset.name || '';
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
