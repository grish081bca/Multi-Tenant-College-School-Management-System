<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="fragments/header.jspf" %>
<section class="page-heading">
    <div><p class="eyebrow">Admissions</p>
        <h1><c:out value="${application.applicationNumber}"/></h1>
        <p class="muted"><c:out value="${application.status}"/> · <c:out value="${application.tenantName}"/></p></div>
    <a class="button secondary" href="<c:url value='/web/admissions'/>">Back to list</a></section>
<section class="panel detail-grid">
    <div><span>Applicant</span><strong><c:out value="${application.firstName}"/> <c:out
            value="${application.middleName}"/> <c:out value="${application.lastName}"/></strong></div>
    <div><span>Email / phone</span><strong><c:out value="${application.email}"/> · <c:out
            value="${application.phone}"/></strong></div>
    <div><span>Date of birth</span><strong><c:out value="${application.dateOfBirth}"/></strong></div>
    <div><span>Application date</span><strong><c:out value="${application.applicationDate}"/></strong></div>
    <div><span>Program</span><strong><c:out value="${application.programName}"/></strong></div>
    <div><span>Academic year / semester</span><strong><c:out value="${application.academicYearName}"/> / <c:out
            value="${application.semesterName}"/></strong></div>
    <div><span>Section</span><strong><c:out value="${application.sectionName}"/></strong></div>
    <div><span>Address</span><strong><c:out value="${application.address}"/></strong></div>
    <c:if test="${not empty application.admittedStudentId}">
        <div><span>Student registration</span><strong><c:out
                value="${application.admittedRegistrationNumber}"/></strong></div>
    </c:if>
    <div><span>Remarks</span><strong><c:out value="${application.remarks}"/></strong></div>
</section>
<c:if test="${canWrite}">
    <section class="panel"><h2>Workflow actions</h2>
        <div class="form-actions"><c:if test="${application.status == 'DRAFT'}">
            <form method="post" action="<c:url value='/web/admissions/${application.id}/status'/>"><input type="hidden"
                                                                                                          name="status"
                                                                                                          value="SUBMITTED">
                <button class="primary">Submit for review</button>
            </form>
        </c:if><c:if test="${application.status == 'SUBMITTED'}">
            <form method="post" action="<c:url value='/web/admissions/${application.id}/status'/>"><input type="hidden"
                                                                                                          name="status"
                                                                                                          value="UNDER_REVIEW">
                <button class="primary">Start review</button>
            </form>
        </c:if><c:if test="${application.status == 'UNDER_REVIEW'}">
            <form method="post" action="<c:url value='/web/admissions/${application.id}/status'/>"><input type="hidden"
                                                                                                          name="status"
                                                                                                          value="ACCEPTED">
                <button class="primary"
                        onclick="return confirm('Accept this applicant and create a student enrollment?')">Accept and
                    enroll
                </button>
            </form>
            <form method="post" action="<c:url value='/web/admissions/${application.id}/status'/>"><input type="hidden"
                                                                                                          name="status"
                                                                                                          value="REJECTED">
                <button class="secondary">Reject</button>
            </form>
        </c:if><c:if
                test="${application.status == 'DRAFT' or application.status == 'SUBMITTED' or application.status == 'UNDER_REVIEW'}">
            <form method="post" action="<c:url value='/web/admissions/${application.id}/status'/>"><input type="hidden"
                                                                                                          name="status"
                                                                                                          value="WITHDRAWN">
                <button class="secondary">Withdraw</button>
            </form>
            <form method="post" action="<c:url value='/web/admissions/${application.id}/delete'/>">
                <button class="secondary" onclick="return confirm('Delete this application?')">Delete</button>
            </form>
        </c:if><c:if test="${application.status == 'DRAFT'}"><a class="button secondary"
                                                                href="<c:url value='/web/admissions/${application.id}/edit'><c:if test='${systemAdmin}'><c:param name='tenantId' value='${tenantId}'/></c:if></c:url>">Edit
            draft</a></c:if></div>
    </section>
</c:if>
<section class="panel"><h2>Change history</h2>
    <div class="table-wrap">
        <table class="data-table">
            <thead>
            <tr>
                <th>Date</th>
                <th>Action</th>
                <th>Field</th>
                <th>Change</th>
                <th>By</th>
            </tr>
            </thead>
            <tbody><c:forEach items="${changeLogs}" var="log">
                <tr>
                    <td><c:out value="${log.createdAt}"/></td>
                    <td><c:out value="${log.action}"/></td>
                    <td><c:out value="${log.fieldName}"/></td>
                    <td><c:out value="${log.oldValue}"/> → <c:out value="${log.newValue}"/></td>
                    <td><c:out value="${log.createdBy}"/></td>
                </tr>
            </c:forEach></tbody>
        </table>
    </div>
</section>
<c:if test="${systemAdmin}">
    <script>document.querySelectorAll('form[method="post"]').forEach(function (form) {
        var input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'tenantId';
        input.value = '${tenantId}';
        form.appendChild(input);
    });</script>
</c:if>
<%@ include file="fragments/footer.jspf" %>
