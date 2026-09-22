<%@ include file="fragments/header.jspf" %>
<section class="filter-panel">
    <form class="filter-grid tenant-filter-grid" action="<c:url value='/web/users'/>" method="get">
        <label>Master Search
            <input name="q" value="<c:out value='${search}'/>" placeholder="Search all columns">
        </label>
        <label>Username
            <select name="username">
                <option value="">All usernames</option>
                <c:forEach items="${usernames}" var="name">
                    <option value="${name}" ${name == selectedUsername ? 'selected' : ''}><c:out value="${name}"/></option>
                </c:forEach>
            </select>
        </label>
        <label>Full name
            <input name="fullName" value="<c:out value='${fullName}'/>" placeholder="Full name">
        </label>
        <label>Email
            <input name="email" value="<c:out value='${email}'/>" placeholder="Email">
        </label>
        <label>Phone number
            <input name="phoneNumber" value="<c:out value='${phoneNumber}'/>" placeholder="Phone number">
        </label>
        <label>Tenant
            <select name="tenantId">
                <option value="">All tenants</option>
                <c:forEach items="${tenants}" var="tenant">
                    <option value="${tenant.id}" ${tenant.id == tenantId ? 'selected' : ''}>
                        <c:out value="${tenant.tenantName}"/>
                    </option>
                </c:forEach>
            </select>
        </label>
        <label>User type
            <select name="userType">
                <option value="">All user types</option>
                <c:forEach items="${userTypes}" var="type">
                    <option value="${type}" ${type == selectedUserType ? 'selected' : ''}>${type}</option>
                </c:forEach>
            </select>
        </label>
        <label>Enabled
            <select name="enabled">
                <option value="">All</option>
                <option value="true" ${enabled == true ? 'selected' : ''}>Enabled</option>
                <option value="false" ${enabled == false ? 'selected' : ''}>Disabled</option>
            </select>
        </label>
        <label>From date
            <input type="date" name="fromDate" value="<c:out value='${fromDate}'/>">
        </label>
        <label>To date
            <input type="date" name="toDate" value="<c:out value='${toDate}'/>">
        </label>
        <label>Status
            <select name="status">
                <option value="">All statuses</option>
                <c:forEach items="${statuses}" var="status">
                    <option value="${status}" ${status == selectedStatus ? 'selected' : ''}>${status}</option>
                </c:forEach>
            </select>
        </label>
        <label>Rows
            <select name="size">
                <option value="10" ${pageSize == 10 ? 'selected' : ''}>10</option>
                <option value="25" ${pageSize == 25 ? 'selected' : ''}>25</option>
                <option value="50" ${pageSize == 50 ? 'selected' : ''}>50</option>
                <option value="100" ${pageSize == 100 ? 'selected' : ''}>100</option>
            </select>
        </label>
        <div class="filter-actions">
            <button class="primary" type="submit">Apply</button>
            <a class="button secondary" href="<c:url value='/web/users'/>">Reset</a>
            <a class="button primary" href="<c:url value='/web/users/add'/>">Add user</a>
        </div>
    </form>
</section>
<section class="panel">
    <div class="table-wrap">
        <table class="data-table">
            <thead>
            <tr>
                <th>Created Date</th>
                <th>Username</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Tenant</th>
                <th>User Type</th>
                <th>User Template</th>
                <th>Enabled</th>
                <th>Status</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody><c:forEach items="${page.objects}" var="user">
                <tr>
                    <td><c:out value="${user.createdDate}"/></td>
                    <td><c:out value="${user.username}"/></td>
                    <td>
                        <c:out value="${user.firstName}"/>
                        <c:if test="${not empty user.middleName}"> <c:out value="${user.middleName}"/></c:if>
                        <c:if test="${not empty user.lastName}"> <c:out value="${user.lastName}"/></c:if>
                    </td>
                    <td><c:out value="${user.email}"/></td>
                    <td><c:out value="${user.phoneNumber}"/></td>
                    <td><c:out value="${empty user.tenantName ? '-' : user.tenantName}"/></td>
                    <td><c:out value="${user.userType}"/></td>
                    <td><c:out value="${empty user.userTemplateName ? '-' : user.userTemplateName}"/></td>
                    <td><span class="status-pill status-${user.enabled ? 'ACTIVE' : 'INACTIVE'}"><c:out value="${user.enabled ? 'Enabled' : 'Disabled'}"/></span></td>
                    <td><span class="status-pill status-${user.status}"><c:out value="${user.status}"/></span></td>
                    <td class="actions-cell">
                        <a class="action-button secondary" href="<c:url value='/web/users/${user.id}/edit'/>" title="Edit user" aria-label="Edit user">
                            <i class="fa-solid fa-pen-to-square" aria-hidden="true"></i>
                            <span>Edit</span>
                        </a>
                        <a class="action-button secondary" href="<c:url value='/web/users/${user.id}'/>" title="View user" aria-label="View user">
                            <i class="fa-solid fa-eye" aria-hidden="true"></i>
                            <span>View</span>
                        </a>
                        <button class="action-button secondary user-status-trigger" type="button"
                                data-action="<c:url value='/web/users/${user.id}/status'/>"
                                data-status="${user.status}"
                                data-name="<c:out value='${user.username}'/>"
                                title="Change user status" aria-label="Change user status">
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
<dialog class="status-dialog" id="userStatusDialog">
    <form id="userStatusForm" method="post">
        <div class="dialog-header">
            <h2>Change user status</h2>
            <button class="icon-button dialog-close" type="button" aria-label="Close dialog">
                <i class="fa-solid fa-xmark" aria-hidden="true"></i>
            </button>
        </div>
        <p class="muted" id="userStatusName"></p>
        <label>Status
            <select name="status" id="userStatusSelect" required>
                <c:forEach items="${statuses}" var="status">
                    <option value="${status}">${status}</option>
                </c:forEach>
            </select>
        </label>
        <label>Remarks (optional)
            <textarea name="remarks" rows="3"></textarea>
        </label>
        <div class="form-actions dialog-actions">
            <button class="primary" type="submit">Update status</button>
            <button class="secondary dialog-close" type="button">Cancel</button>
        </div>
    </form>
</dialog>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        var dialog = document.getElementById('userStatusDialog');
        var form = document.getElementById('userStatusForm');
        var statusSelect = document.getElementById('userStatusSelect');
        var userName = document.getElementById('userStatusName');

        document.querySelectorAll('.user-status-trigger').forEach(function (button) {
            button.addEventListener('click', function () {
                form.action = button.dataset.action;
                statusSelect.value = button.dataset.status;
                userName.textContent = button.dataset.name || '';
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
