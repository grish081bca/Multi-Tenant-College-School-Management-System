<%@ include file="fragments/header.jspf" %>
<%--<c:set var="pageEyebrow" value="Access administration"/>--%>
<%--<c:set var="pageTitle" value="User templates"/>--%>
<%--<c:set var="pageDescription" value="Control which user types can be created for each tenant."/>--%>
<%--<c:set var="pageActionUrl" value="/web/user-templates/add"/>--%>
<%--<c:set var="pageActionText" value="Add user template"/>--%>
<%--<%@ include file="fragments/page-header.jspf" %>--%>
<section class="filter-panel">
    <form class="filter-grid tenant-filter-grid" action="<c:url value='/web/user-templates'/>" method="get">
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
        <c:set var="filterActionUrl" value="/web/user-templates"/>
        <%@ include file="fragments/search-filter.jspf" %>
    </form>
</section>
<section class="panel">
    <div class="table-wrap">
        <table class="data-table">
            <thead><tr><th>User type</th><th>Menu template</th><th>Status</th><th>Action</th></tr></thead>
            <tbody><c:forEach items="${page.objects}" var="template">
                <tr>
                    <td><c:out value="${template.userType}"/></td>
                    <td><c:out value="${empty template.menuTemplateName ? 'Default' : template.menuTemplateName}"/></td>
                    <td><c:out value="${template.status}"/></td>
                    <td class="actions-cell">
                        <button class="action-button secondary user-template-status-trigger" type="button"
                                data-action="<c:url value='/web/user-templates/${template.id}/status'/>"
                                data-status="${template.status}"
                                data-name="<c:out value='${template.userType}'/>"
                                title="Change user template status" aria-label="Change user template status">
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
<dialog class="status-dialog" id="userTemplateStatusDialog">
    <form id="userTemplateStatusForm" method="post">
        <div class="dialog-header">
            <h2>Change user template status</h2>
            <button class="icon-button dialog-close" type="button" aria-label="Close dialog">
                <i class="fa-solid fa-xmark" aria-hidden="true"></i>
            </button>
        </div>
        <p class="muted" id="userTemplateStatusName"></p>
        <label>Status
            <select name="status" id="userTemplateStatusSelect" required>
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
        var dialog = document.getElementById('userTemplateStatusDialog');
        var form = document.getElementById('userTemplateStatusForm');
        var statusSelect = document.getElementById('userTemplateStatusSelect');
        var templateName = document.getElementById('userTemplateStatusName');

        document.querySelectorAll('.user-template-status-trigger').forEach(function (button) {
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
