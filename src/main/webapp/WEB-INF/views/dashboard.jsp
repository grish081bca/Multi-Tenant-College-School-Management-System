<%@ include file="fragments/header.jspf" %>
<section class="dashboard-page">
    <div class="dashboard-hero">
        <div class="dashboard-hero-copy">
            <span class="dashboard-eyebrow"><i class="fa-solid fa-sparkles" aria-hidden="true"></i> <c:out value="${dashboard.roleLabel}"/> workspace</span>
            <h1>Good to see you, <c:out value="${dashboard.greeting}"/></h1>
            <p><c:out value="${dashboard.description}"/></p>
        </div>
        <div class="dashboard-hero-mark" aria-hidden="true"><i class="fa-solid fa-chart-pie"></i></div>
        <div class="dashboard-hero-foot"><span class="live-dot"></span> Current overview <span class="hero-divider">/</span> <c:out value="${dashboard.roleLabel}"/></div>
    </div>

    <section class="dashboard-metrics ${dashboard.cardLayout}" aria-label="Key metrics">
        <c:forEach items="${dashboard.cards}" var="card">
            <c:choose>
                <c:when test="${not empty card.href}"><a class="metric-card ${card.tone}" href="<c:url value='${card.href}'/>"></c:when>
                <c:otherwise><article class="metric-card ${card.tone}"></c:otherwise>
            </c:choose>
                <span class="metric-icon"><i class="${card.icon}" aria-hidden="true"></i></span>
                <span class="metric-label"><c:out value="${card.title}"/></span>
                <strong class="metric-value"><c:out value="${card.value}"/></strong>
                <span class="metric-caption"><c:out value="${card.caption}"/></span>
                <span class="metric-trend"><i class="fa-solid fa-arrow-up-right-from-square" aria-hidden="true"></i></span>
            <c:choose>
                <c:when test="${not empty card.href}"></a></c:when>
                <c:otherwise></article></c:otherwise>
            </c:choose>
        </c:forEach>
    </section>

    <section class="dashboard-charts" aria-label="College analytics">
        <article class="dashboard-panel enrollment-panel">
            <div class="panel-heading">
                <div><span class="panel-kicker">ACADEMIC SNAPSHOT</span><h2>Enrollment status</h2><p>How enrollment records are distributed</p></div>
                <span class="panel-icon"><i class="fa-solid fa-chart-pie" aria-hidden="true"></i></span>
            </div>
            <div class="donut-layout">
                <div class="donut-chart" style="--pie-gradient: ${dashboard.pieGradient};" role="img" aria-label="Enrollment status distribution">
                    <div class="donut-center"><strong><c:out value="${dashboard.pieTotal}"/></strong><span>Total</span></div>
                </div>
                <div class="chart-legend">
                    <c:forEach items="${dashboard.pieItems}" var="item">
                        <div class="legend-row">
                            <span class="legend-dot" style="--legend-color:${item.color}"></span>
                            <span class="legend-name"><c:out value="${item.label}"/></span>
                            <strong><c:out value="${item.value}"/></strong>
                        </div>
                    </c:forEach>
                </div>
            </div>
            <a class="panel-link" href="<c:url value='/web/student-enrollments'/>">Explore enrollments <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
        </article>

        <article class="dashboard-panel activity-panel">
            <div class="panel-heading">
                <div><span class="panel-kicker">WORKSPACE PULSE</span><h2>At a glance</h2><p>Quick comparison of your key areas</p></div>
                <span class="panel-icon warm"><i class="fa-solid fa-chart-column" aria-hidden="true"></i></span>
            </div>
            <div class="bar-chart">
                <c:forEach items="${dashboard.barItems}" var="item">
                    <div class="bar-row">
                        <span class="bar-label"><c:out value="${item.label}"/></span>
                        <div class="bar-track"><span class="bar-fill" style="--bar-width:${item.percentage}%;--bar-color:${item.color}"></span></div>
                        <strong class="bar-value"><c:out value="${item.value}"/></strong>
                    </div>
                </c:forEach>
            </div>
            <div class="chart-note"><span><i class="fa-solid fa-circle-info" aria-hidden="true"></i></span> Counts reflect the data available to your role.</div>
        </article>
    </section>

    <section class="dashboard-actions">
        <div class="actions-heading"><div><span class="panel-kicker">KEEP MOVING</span><h2>Quick access</h2></div><span class="actions-caption">Your everyday tools</span></div>
        <div class="action-grid">
            <c:forEach items="${dashboard.actions}" var="action">
                <a class="action-card" href="<c:url value='${action.href}'/>">
                    <span class="action-icon"><i class="${action.icon}" aria-hidden="true"></i></span>
                    <span class="action-copy"><strong><c:out value="${action.label}"/></strong><small><c:out value="${action.description}"/></small></span>
                    <span class="action-arrow"><i class="fa-solid fa-arrow-up-right-from-square" aria-hidden="true"></i></span>
                </a>
            </c:forEach>
        </div>
    </section>
</section>
<%@ include file="fragments/footer.jspf" %>
