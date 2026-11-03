<%@ include file="fragments/header.jspf" %>
<section class="filter-panel">
    <form class="filter-grid tenant-filter-grid" action="<c:url value='/web/tenant-branches'/>" method="get">
        <label>Master Search
            <input name="q" value="<c:out value='${search}'/>" placeholder="Search all columns">
        </label>
        <label>Main tenant
            <select name="parentTenantId">
                <option value="">All main tenants</option>
                <c:forEach items="${mainTenants}" var="mainTenant">
                    <option value="${mainTenant.id}" ${mainTenant.id == parentTenantId ? 'selected' : ''}>
                        <c:out value="${mainTenant.tenantName}"/> - <c:out value="${mainTenant.tenantCode}"/>
                    </option>
                </c:forEach>
            </select>
        </label>
        <label>Branch name
            <select name="tenantName">
                <option value="">All branches</option>
                <c:forEach items="${tenantNames}" var="name">
                    <option value="${name}" ${name == selectedTenantName ? 'selected' : ''}><c:out value="${name}"/></option>
                </c:forEach>
            </select>
        </label>
        <label>Branch code
            <input name="tenantCode" value="<c:out value='${tenantCode}'/>" placeholder="Branch code">
        </label>
        <label>Phone number
            <input name="contactPhone" value="<c:out value='${contactPhone}'/>" placeholder="Phone number">
        </label>
        <label>Country
            <input name="country" value="<c:out value='${country}'/>" placeholder="Country">
        </label>
        <label>State
            <input name="state" value="<c:out value='${state}'/>" placeholder="State">
        </label>
        <label>City
            <input name="city" value="<c:out value='${city}'/>" placeholder="City">
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
            <a class="button secondary" href="<c:url value='/web/tenant-branches'/>">Reset</a>
        </div>
    </form>
</section>
<section class="panel">
    <div class="table-wrap">
        <table class="data-table">
            <thead>
            <tr>
                <th>Created Date</th>
                <th>Main Tenant</th>
                <th>Branch Code</th>
                <th>Branch Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Country</th>
                <th>State</th>
                <th>City</th>
                <th>Postal Code</th>
                <th>Status</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody><c:forEach items="${page.objects}" var="tenant">
                <tr>
                    <td><c:out value="${tenant.createdDate}"/></td>
                    <td><c:out value="${tenant.parentTenantName}"/></td>
                    <td><c:out value="${tenant.tenantCode}"/></td>
                    <td><c:out value="${tenant.tenantName}"/></td>
                    <td><c:out value="${tenant.contactEmail}"/></td>
                    <td><c:out value="${tenant.contactPhone}"/></td>
                    <td><c:out value="${tenant.countryName}"/></td>
                    <td><c:out value="${tenant.stateName}"/></td>
                    <td><c:out value="${tenant.cityName}"/></td>
                    <td><c:out value="${tenant.postalCode}"/></td>
                    <td><span class="status-pill status-${tenant.status}"><c:out value="${tenant.status}"/></span></td>
                    <td class="actions-cell">
                        <a class="action-button secondary" href="<c:url value='/web/tenant-branches/${tenant.id}/edit'/>" title="Edit branch" aria-label="Edit branch">
                            <i class="fa-solid fa-pen-to-square" aria-hidden="true"></i>
                            <span>Edit</span>
                        </a>
                        <a class="action-button secondary" href="<c:url value='/web/tenant-branches/${tenant.id}'/>" title="View branch" aria-label="View branch">
                            <i class="fa-solid fa-eye" aria-hidden="true"></i>
                            <span>View</span>
                        </a>
                        <button class="action-button secondary tenant-branch-status-trigger" type="button"
                                data-action="<c:url value='/web/tenant-branches/${tenant.id}/status'/>"
                                data-status="${tenant.status}"
                                data-name="<c:out value='${tenant.tenantName}'/>"
                                title="Change branch status" aria-label="Change branch status">
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
<dialog class="status-dialog" id="tenantBranchStatusDialog">
    <form id="tenantBranchStatusForm" method="post">
        <div class="dialog-header">
            <h2>Change branch status</h2>
            <button class="icon-button dialog-close" type="button" aria-label="Close dialog">
                <i class="fa-solid fa-xmark" aria-hidden="true"></i>
            </button>
        </div>
        <p class="muted" id="tenantBranchStatusName"></p>
        <label>Status
            <select name="status" id="tenantBranchStatusSelect" required>
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
        var dialog = document.getElementById('tenantBranchStatusDialog');
        var form = document.getElementById('tenantBranchStatusForm');
        var statusSelect = document.getElementById('tenantBranchStatusSelect');
        var tenantName = document.getElementById('tenantBranchStatusName');

        document.querySelectorAll('.tenant-branch-status-trigger').forEach(function (button) {
            button.addEventListener('click', function () {
                form.action = button.dataset.action;
                statusSelect.value = button.dataset.status;
                tenantName.textContent = button.dataset.name || '';
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
