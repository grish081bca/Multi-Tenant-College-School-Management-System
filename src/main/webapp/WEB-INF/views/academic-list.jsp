<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="fragments/header.jspf" %>
<section class="page-heading">
    <div><p class="eyebrow">Academic administration</p><h1>${moduleTitle}</h1><p class="muted">Manage ${moduleTitle.toLowerCase()} records for your college.</p></div>
    <a class="button primary" href="<c:url value='/web/${module}/add'/>"><i class="fa-solid fa-plus"></i> Add ${moduleTitle}</a>
</section>
<section class="filter-panel">
    <form class="filter-grid" action="<c:url value='/web/${module}'/>" method="get">
        <label>Search<input name="q" value="<c:out value='${q}'/>" placeholder="Search ${moduleTitle.toLowerCase()} records"></label>
        <label>Status<select name="status"><option value="">All statuses</option><c:forEach items="${statuses}" var="value"><option value="${value}" ${value == selectedStatus ? 'selected' : ''}>${value}</option></c:forEach></select></label>
        <label>Rows<select name="size"><option value="10" ${pageSize == 10 ? 'selected' : ''}>10</option><option value="25" ${pageSize == 25 ? 'selected' : ''}>25</option><option value="50" ${pageSize == 50 ? 'selected' : ''}>50</option><option value="100" ${pageSize == 100 ? 'selected' : ''}>100</option></select></label>
        <div class="filter-actions"><button class="primary" type="submit">Apply</button><a class="button secondary" href="<c:url value='/web/${module}'/>">Reset</a></div>
    </form>
</section>
<section class="panel"><div class="table-wrap"><table class="data-table">
    <thead><tr><th>Code / Registration</th><th>Name</th><th>Academic details</th><th>Status</th><th>Action</th></tr></thead>
    <tbody><c:forEach items="${page.objects}" var="record"><tr>
        <td><c:out value="${record.code}"/><c:if test="${module == 'students'}"><c:out value="${record.registrationNumber}"/></c:if></td>
        <td><c:choose><c:when test="${module == 'students'}"><c:out value="${record.firstName}"/> <c:out value="${record.lastName}"/></c:when><c:otherwise><c:out value="${record.name}"/></c:otherwise></c:choose></td>
        <td><c:choose>
            <c:when test="${module == 'departments'}">Faculty: <c:out value="${record.facultyName}"/></c:when>
            <c:when test="${module == 'programs'}"><c:out value="${record.facultyName}"/> · <c:out value="${record.departmentName}"/></c:when>
            <c:when test="${module == 'sections'}"><c:out value="${record.programName}"/> · <c:out value="${record.academicYearName}"/> · <c:out value="${record.semesterName}"/></c:when>
            <c:when test="${module == 'program-subjects'}"><c:out value="${record.programName}"/> · <c:out value="${record.subjectName}"/> · <c:out value="${record.semesterName}"/></c:when>
            <c:when test="${module == 'student-enrollments'}"><c:out value="${record.studentName}"/> · <c:out value="${record.programName}"/> · <c:out value="${record.sectionName}"/> · <c:out value="${record.academicYearName}"/> / <c:out value="${record.semesterName}"/></c:when>
            <c:when test="${module == 'students'}"><c:out value="${record.email}"/><c:if test="${not empty record.phone}"> · <c:out value="${record.phone}"/></c:if></c:when>
            <c:when test="${module == 'semesters'}">Sequence <c:out value="${record.sequenceNumber}"/></c:when>
            <c:when test="${module == 'subjects'}">Credit hours <c:out value="${record.creditHours}"/></c:when>
            <c:otherwise><c:out value="${record.description}"/></c:otherwise>
        </c:choose></td>
        <td><span class="status-pill status-${record.status}"><c:out value="${record.status}"/></span></td>
        <td class="actions-cell"><a class="action-button secondary" href="<c:url value='/web/${module}/${record.id}'/>"><i class="fa-solid fa-eye"></i><span>View</span></a><a class="action-button secondary" href="<c:url value='/web/${module}/${record.id}/edit'/>"><i class="fa-solid fa-pen-to-square"></i><span>Edit</span></a>
            <button class="action-button secondary academic-status-trigger" type="button" data-action="<c:url value='/web/${module}/${record.id}/status'/>" data-status="${record.status}" data-name="<c:out value='${record.name}'/>"><i class="fa-solid fa-toggle-on" aria-hidden="true"></i><span>Change status</span></button>
            <form class="action-form" method="post" action="<c:url value='/web/${module}/${record.id}/delete'/>"><button class="action-button secondary" type="submit" onclick="return confirm('Delete this ${moduleTitle.toLowerCase()} record?')"><i class="fa-solid fa-trash"></i><span>Delete</span></button></form>
        </td>
    </tr></c:forEach></tbody>
</table></div><%@ include file="fragments/data-table-empty.jspf" %><%@ include file="fragments/pagination.jspf" %></section>
<dialog class="status-dialog" id="academicStatusDialog"><form id="academicStatusForm" method="post">
    <div class="dialog-header"><h2>Change ${moduleTitle.toLowerCase()} status</h2><button class="icon-button academic-dialog-close" type="button" aria-label="Close dialog"><i class="fa-solid fa-xmark" aria-hidden="true"></i></button></div>
    <p class="muted" id="academicStatusName"></p>
    <label>Status<select name="status" id="academicStatusSelect" required><c:forEach items="${statuses}" var="value"><option value="${value}">${value}</option></c:forEach></select></label>
    <label>Remarks (optional)<textarea name="remarks" rows="3"></textarea></label>
    <div class="form-actions dialog-actions"><button class="primary" type="submit">Update status</button><button class="secondary academic-dialog-close" type="button">Cancel</button></div>
</form></dialog>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        var dialog = document.getElementById('academicStatusDialog');
        var form = document.getElementById('academicStatusForm');
        var select = document.getElementById('academicStatusSelect');
        var name = document.getElementById('academicStatusName');
        document.querySelectorAll('.academic-status-trigger').forEach(function (button) {
            button.addEventListener('click', function () {
                form.action = button.dataset.action;
                select.value = button.dataset.status;
                name.textContent = button.dataset.name || '';
                if (dialog.showModal) dialog.showModal(); else dialog.setAttribute('open', 'open');
            });
        });
        document.querySelectorAll('.academic-dialog-close').forEach(function (button) { button.addEventListener('click', function () { dialog.close(); }); });
    });
</script>
<%@ include file="fragments/footer.jspf" %>
