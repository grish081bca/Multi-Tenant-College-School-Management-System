<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login | College ERP</title>
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            /* -- color system -- */
            --primary: #315BFF;
            --primary-hover: #2448D8;
            --primary-100: #E9EFFF;
            --bg: #F8FAFC;
            --surface: #FFFFFF;
            --text: #111827;
            --text-secondary: #64748B;
            --text-muted: #94A3B8;
            --border: #E2E8F0;
            --success: #10B981;
            --success-bg: #ECFDF5;
            --success-border: #A7F3D0;
            --error: #EF4444;
            --error-bg: #FEF2F2;
            --error-border: #FECACA;
            --navy-950: #0A1226;
            --navy-900: #0F1B33;
            --navy-800: #16244A;

            /* -- spacing (4/8 system) -- */
            --sp-1: 4px;
            --sp-2: 8px;
            --sp-3: 12px;
            --sp-4: 16px;
            --sp-6: 24px;
            --sp-8: 32px;
            --sp-10: 40px;
            --sp-12: 48px;
            --sp-16: 64px;

            --font: "Plus Jakarta Sans", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        }

        * { box-sizing: border-box; }
        html, body { height: 100%; margin: 0; }

        body.auth-page {
            font-family: var(--font);
            color: var(--text);
            display: grid;
            grid-template-columns: 1fr 1fr;
            width: 100vw;
            height: 100vh;
            height: 100dvh;
            background: var(--surface);
        }

        a { font-family: inherit; }

        /* ================= LEFT — branding panel ================= */
        .auth-brand {
            position: relative;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            height: 100%;
            width: 100%;
            padding: var(--sp-12) var(--sp-12) var(--sp-10);
            background: linear-gradient(160deg, var(--navy-950), var(--navy-900) 55%, var(--navy-800));
            color: #EEF1F8;
            overflow: hidden;
        }

        .auth-brand::before {
            content: "";
            position: absolute;
            inset: 0;
            z-index: 0;
            background:
                    radial-gradient(55% 45% at 15% 0%, rgba(49, 91, 255, 0.32), transparent 65%),
                    radial-gradient(45% 40% at 100% 100%, rgba(49, 91, 255, 0.14), transparent 60%);
        }

        .brand-mark {
            position: relative;
            z-index: 1;
            display: flex;
            align-items: center;
            gap: var(--sp-3);
            font-weight: 700;
            font-size: 16px;
            letter-spacing: 0.01em;
        }

        .crest {
            width: 36px;
            height: 36px;
            border-radius: 9px;
            display: flex;
            align-items: center;
            justify-content: center;
            flex: none;
            background: linear-gradient(150deg, var(--primary), #5478ff);
            box-shadow: 0 6px 16px rgba(49, 91, 255, 0.4);
        }

        .crest svg { width: 19px; height: 19px; }

        /* ---- illustration ---- */
        .stage {
            position: relative;
            z-index: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: var(--sp-8) 0;
        }

        .dash-mock {
            width: 100%;
            max-width: 380px;
            border-radius: 16px;
            background: rgba(255,255,255,0.05);
            border: 1px solid rgba(255,255,255,0.12);
            box-shadow: 0 24px 48px -20px rgba(0,0,0,0.5);
            padding: var(--sp-4);
        }

        .dash-mock .dm-head {
            display: flex;
            align-items: center;
            gap: 6px;
            margin-bottom: var(--sp-4);
        }

        .dash-mock .dm-head span {
            width: 7px; height: 7px; border-radius: 50%;
            background: rgba(255,255,255,0.18);
        }

        .dash-mock .dm-head b {
            margin-left: var(--sp-2);
            font-size: 11.5px;
            font-weight: 600;
            color: rgba(255,255,255,0.55);
            letter-spacing: 0.02em;
        }

        .dm-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: var(--sp-2);
            margin-bottom: var(--sp-4);
        }

        .dm-chip {
            background: rgba(255,255,255,0.06);
            border: 1px solid rgba(255,255,255,0.1);
            border-radius: 10px;
            padding: 10px 8px;
        }

        .dm-chip svg {
            width: 15px; height: 15px;
            color: #8fa4ff;
            margin-bottom: 8px;
        }

        .dm-chip strong {
            display: block;
            font-size: 13px;
            font-weight: 700;
            color: #fff;
            line-height: 1.2;
        }

        .dm-chip span {
            display: block;
            font-size: 9.5px;
            color: rgba(255,255,255,0.5);
            margin-top: 2px;
        }

        .dm-chart {
            display: flex;
            align-items: flex-end;
            gap: 6px;
            height: 54px;
            padding: var(--sp-3) var(--sp-2) 0;
            background: rgba(255,255,255,0.04);
            border: 1px solid rgba(255,255,255,0.08);
            border-radius: 10px;
        }

        .dm-chart i {
            flex: 1;
            border-radius: 3px 3px 0 0;
            background: linear-gradient(180deg, #5478ff, rgba(84,120,255,0.25));
        }

        /* ---- copy ---- */
        .brand-copy { position: relative; z-index: 1; }

        .brand-copy .eyebrow {
            display: inline-flex;
            align-items: center;
            gap: var(--sp-2);
            font-size: 11.5px;
            font-weight: 700;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            color: #8fa4ff;
            margin-bottom: var(--sp-3);
        }

        .brand-copy h2 {
            font-size: 30px;
            line-height: 1.28;
            font-weight: 700;
            letter-spacing: -0.01em;
            margin: 0 0 var(--sp-3);
            max-width: 440px;
        }

        .brand-copy p {
            font-size: 14.5px;
            line-height: 1.65;
            color: #B7C0D6;
            margin: 0 0 var(--sp-6);
            max-width: 420px;
        }

        .feature-list {
            list-style: none;
            margin: 0;
            padding: 0;
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: var(--sp-3) var(--sp-4);
            max-width: 440px;
        }

        .feature-list li {
            display: flex;
            align-items: center;
            gap: var(--sp-2);
            font-size: 13px;
            font-weight: 500;
            color: #D7DCEA;
        }

        .feature-list svg {
            width: 16px; height: 16px;
            flex: none;
            color: var(--success);
        }

        .brand-foot {
            position: relative;
            z-index: 1;
            margin-top: var(--sp-6);
            font-size: 12px;
            color: #6D7794;
        }

        /* ================= RIGHT — form panel ================= */
        .auth-panel {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100%;
            width: 100%;
            padding: var(--sp-8);
            overflow-y: auto;
            background: var(--bg);
        }

        .auth-card {
            width: 100%;
            max-width: 420px;
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 16px;
            box-shadow: 0 20px 50px rgba(15, 23, 42, 0.08);
            padding: var(--sp-12);
        }

        .panel-eyebrow {
            display: none;
            align-items: center;
            gap: var(--sp-2);
            font-weight: 700;
            font-size: 14px;
            color: var(--text);
            margin-bottom: var(--sp-8);
        }

        .auth-card h1 {
            margin: 0 0 var(--sp-1);
            font-size: 30px;
            font-weight: 700;
            letter-spacing: -0.01em;
            color: var(--text);
        }

        .auth-card .subtitle {
            margin: 0 0 var(--sp-8);
            color: var(--text-secondary);
            font-size: 14.5px;
        }

        .alert {
            display: flex;
            align-items: flex-start;
            gap: var(--sp-2);
            padding: var(--sp-3) var(--sp-4);
            border-radius: 10px;
            font-size: 13.5px;
            line-height: 1.4;
            margin-bottom: var(--sp-6);
            border: 1px solid transparent;
        }

        .alert svg { flex: none; margin-top: 1px; }
        .alert.error { background: var(--error-bg); color: #B42318; border-color: var(--error-border); }
        .alert.success { background: var(--success-bg); color: #067647; border-color: var(--success-border); }

        form { display: flex; flex-direction: column; gap: var(--sp-6); }

        label.field-label {
            display: flex;
            flex-direction: column;
            gap: var(--sp-2);
            font-size: 12.5px;
            font-weight: 700;
            letter-spacing: 0.04em;
            text-transform: uppercase;
            color: var(--text-secondary);
        }

        .field-wrap { position: relative; display: flex; align-items: center; }

        .field-wrap svg.field-icon {
            position: absolute;
            left: 14px;
            width: 17px; height: 17px;
            color: var(--text-muted);
            pointer-events: none;
            transition: color 0.18s ease;
        }

        .field-wrap:focus-within svg.field-icon { color: var(--primary); }

        input {
            font: inherit;
            font-size: 14.5px;
            font-weight: 500;
            width: 100%;
            height: 48px;
            padding: 0 var(--sp-4) 0 42px;
            border-radius: 10px;
            border: 1.5px solid var(--border);
            background: var(--surface);
            color: var(--text);
            transition: border-color 0.18s ease, box-shadow 0.18s ease;
        }

        input::placeholder { color: var(--text-muted); font-weight: 400; }

        input:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(49, 91, 255, 0.10);
        }

        input:invalid:not(:placeholder-shown) { border-color: var(--error); }

        input[type="password"] { padding-right: 44px; }

        .toggle-visibility {
            position: absolute;
            right: 4px;
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: none;
            border: none;
            cursor: pointer;
            color: var(--text-muted);
            border-radius: 8px;
            transition: color 0.15s ease, background 0.15s ease;
        }

        .toggle-visibility:hover { color: var(--text-secondary); background: #F1F5F9; }
        .toggle-visibility:focus-visible,
        input:focus-visible,
        .forgot-link:focus-visible,
        button.primary:focus-visible {
            outline: 2px solid var(--primary);
            outline-offset: 2px;
        }

        .row-between {
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 13.5px;
        }

        .remember {
            display: flex;
            align-items: center;
            gap: var(--sp-2);
            font-weight: 500;
            color: var(--text-secondary);
            cursor: pointer;
            user-select: none;
        }

        .remember input[type="checkbox"] {
            position: absolute;
            width: 1px; height: 1px;
            opacity: 0;
        }

        .remember .box {
            width: 18px; height: 18px;
            border-radius: 5px;
            border: 1.5px solid var(--border);
            background: var(--surface);
            display: flex;
            align-items: center;
            justify-content: center;
            flex: none;
            transition: background 0.15s ease, border-color 0.15s ease;
        }

        .remember .box svg {
            width: 12px; height: 12px;
            opacity: 0;
            transform: scale(0.6);
            color: #fff;
            transition: opacity 0.12s ease, transform 0.12s ease;
        }

        .remember input[type="checkbox"]:checked ~ .box {
            background: var(--primary);
            border-color: var(--primary);
        }

        .remember input[type="checkbox"]:checked ~ .box svg {
            opacity: 1;
            transform: scale(1);
        }

        .remember input[type="checkbox"]:focus-visible ~ .box {
            outline: 2px solid var(--primary);
            outline-offset: 2px;
        }

        .forgot-link { color: var(--primary); font-weight: 600; text-decoration: none; }
        .forgot-link:hover { text-decoration: underline; }

        button.primary {
            font: inherit;
            font-size: 15px;
            font-weight: 700;
            color: #fff;
            height: 50px;
            border: none;
            border-radius: 10px;
            background: var(--primary);
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: var(--sp-2);
            transition: background 0.18s ease, transform 0.12s ease;
        }

        button.primary:hover { background: var(--primary-hover); }
        button.primary:active { transform: translateY(1px); }
        button.primary svg.arrow { transition: transform 0.18s ease; }
        button.primary:hover svg.arrow { transform: translateX(3px); }

        button.primary:disabled {
            background: #A9B8ED;
            cursor: not-allowed;
            transform: none;
        }

        .spinner {
            width: 16px; height: 16px;
            border-radius: 50%;
            border: 2px solid rgba(255,255,255,0.4);
            border-top-color: #fff;
            animation: spin 0.7s linear infinite;
            display: none;
        }

        button.primary.loading .spinner { display: inline-block; }
        button.primary.loading .btn-label,
        button.primary.loading .arrow { display: none; }

        @keyframes spin { to { transform: rotate(360deg); } }

        .auth-foot {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            margin-top: var(--sp-8);
            font-size: 12.5px;
            color: var(--text-muted);
        }

        .auth-foot svg { width: 13px; height: 13px; flex: none; }

        /* ================= Responsive ================= */

        /* Tablet: narrow the branding panel, tighten spacing */
        @media (max-width: 1180px) {
            body.auth-page { grid-template-columns: 0.9fr 1fr; }
            .auth-brand { padding: var(--sp-8) var(--sp-8) var(--sp-6); }
            .brand-copy h2 { font-size: 25px; }
            .feature-list { grid-template-columns: 1fr; }
            .auth-card { padding: var(--sp-10); }
        }

        /* Mobile: single column, form-first */
        @media (max-width: 860px) {
            body.auth-page { grid-template-columns: 1fr; }
            .auth-brand { display: none; }
            .auth-panel { padding: var(--sp-6) var(--sp-4); }
            .auth-card { border: none; box-shadow: none; padding: var(--sp-2); }
            .panel-eyebrow { display: flex; }
        }

        @media (max-width: 420px) {
            .auth-card h1 { font-size: 25px; }
        }
    </style>
</head>
<body class="auth-page">

<section class="auth-brand" aria-hidden="true">
    <div class="brand-mark">
        <span class="crest">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 3 2 8l10 5 8-4v6" stroke="#fff" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/><path d="M5 10.5V15c0 1.7 3.2 3.5 7 3.5s7-1.8 7-3.5v-4.5" stroke="#fff" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </span>
        College ERP
    </div>

    <div class="stage">
        <div class="dash-mock">
            <div class="dm-head">
                <span></span><span></span><span></span>
                <b>ACADEMIC OVERVIEW</b>
            </div>
            <div class="dm-grid">
                <div class="dm-chip">
                    <svg viewBox="0 0 16 16" fill="none"><path d="M8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" stroke="currentColor" stroke-width="1.4"/><path d="M2.5 14c.6-2.9 3-4.5 5.5-4.5s4.9 1.6 5.5 4.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
                    <strong>Students</strong>
                    <span>Enrollment</span>
                </div>
                <div class="dm-chip">
                    <svg viewBox="0 0 16 16" fill="none"><path d="M2 12.5 6 8l3 3 5-6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    <strong>Attendance</strong>
                    <span>Daily</span>
                </div>
                <div class="dm-chip">
                    <svg viewBox="0 0 16 16" fill="none"><path d="M4 2h6l3 3v9a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V3a1 1 0 0 1 1-1Z" stroke="currentColor" stroke-width="1.3"/><path d="M5.5 8h5M5.5 10.5h5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>
                    <strong>Exams</strong>
                    <span>Results</span>
                </div>
                <div class="dm-chip">
                    <svg viewBox="0 0 16 16" fill="none"><rect x="2" y="3" width="12" height="10" rx="1.5" stroke="currentColor" stroke-width="1.3"/><path d="M2 6.5h12" stroke="currentColor" stroke-width="1.3"/></svg>
                    <strong>Fees</strong>
                    <span>Finance</span>
                </div>
            </div>
            <div class="dm-chart">
                <i style="height:35%"></i><i style="height:55%"></i><i style="height:40%"></i>
                <i style="height:70%"></i><i style="height:50%"></i><i style="height:85%"></i>
                <i style="height:60%"></i><i style="height:75%"></i>
            </div>
        </div>
    </div>

    <div class="brand-copy">
        <span class="eyebrow">Campus Management, Unified</span>
        <h2>One secure platform for admissions, academics &amp; administration.</h2>
        <p>Manage students, academics, finance, and administration from one centralized platform.</p>

        <ul class="feature-list">
            <li>
                <svg viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="7" fill="currentColor" opacity="0.15"/><path d="M4.8 8.2 7 10.4l4.2-4.8" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
                Student Management
            </li>
            <li>
                <svg viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="7" fill="currentColor" opacity="0.15"/><path d="M4.8 8.2 7 10.4l4.2-4.8" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
                Academic Management
            </li>
            <li>
                <svg viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="7" fill="currentColor" opacity="0.15"/><path d="M4.8 8.2 7 10.4l4.2-4.8" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
                Finance &amp; Administration
            </li>
            <li>
                <svg viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="7" fill="currentColor" opacity="0.15"/><path d="M4.8 8.2 7 10.4l4.2-4.8" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
                Campus Operations
            </li>
        </ul>
    </div>

    <div class="brand-foot">© <%= java.time.Year.now() %> College ERP. All rights reserved.</div>
</section>

<section class="auth-panel">
    <div class="auth-card">
        <div class="panel-eyebrow">
            <span class="crest" style="width:30px;height:30px;border-radius:8px;">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 3 2 8l10 5 8-4v6" stroke="#fff" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/><path d="M5 10.5V15c0 1.7 3.2 3.5 7 3.5s7-1.8 7-3.5v-4.5" stroke="#fff" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </span>
            College ERP
        </div>

        <h1>Welcome back</h1>
        <p class="subtitle">Sign in to your account</p>

        <c:if test="${not empty param.error}">
            <div class="alert error" role="alert">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M8 1.5 15 14H1L8 1.5Z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/><path d="M8 6v3.5M8 11.5v.01" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>
                <span>Invalid username or password.</span>
            </div>
        </c:if>
        <c:if test="${not empty param.logout}">
            <div class="alert success" role="status">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M13.5 4.5 6.5 11.5 2.5 7.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                <span>You have been signed out.</span>
            </div>
        </c:if>

        <form action="<c:url value='/login'/>" method="post" id="loginForm">
            <label class="field-label" for="username">Username
                <div class="field-wrap">
                    <svg class="field-icon" viewBox="0 0 16 16" fill="none"><path d="M8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" stroke="currentColor" stroke-width="1.3"/><path d="M2.5 14c.6-2.9 3-4.5 5.5-4.5s4.9 1.6 5.5 4.5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>
                    <input id="username" name="username" autocomplete="username" placeholder="Enter your username" required>
                </div>
            </label>

            <label class="field-label" for="password">Password
                <div class="field-wrap">
                    <svg class="field-icon" viewBox="0 0 16 16" fill="none"><rect x="3" y="7" width="10" height="7" rx="1.5" stroke="currentColor" stroke-width="1.3"/><path d="M5.5 7V5a2.5 2.5 0 0 1 5 0v2" stroke="currentColor" stroke-width="1.3"/></svg>
                    <input id="password" type="password" name="password" autocomplete="current-password" placeholder="Enter your password" required>
                    <button type="button" class="toggle-visibility" id="togglePassword" aria-label="Show password" aria-pressed="false">
                        <svg id="eyeIcon" width="18" height="18" viewBox="0 0 16 16" fill="none"><path d="M1 8s2.5-4.5 7-4.5S15 8 15 8s-2.5 4.5-7 4.5S1 8 1 8Z" stroke="currentColor" stroke-width="1.3"/><circle cx="8" cy="8" r="2" stroke="currentColor" stroke-width="1.3"/></svg>
                    </button>
                </div>
            </label>

            <div class="row-between">
                <label class="remember" for="remember">
                    <input type="checkbox" id="remember" name="remember">
                    <span class="box">
                        <svg viewBox="0 0 16 16" fill="none"><path d="M3.5 8.2 6.5 11l6-6.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    </span>
                    Remember me
                </label>
                <a class="forgot-link" href="<c:url value='/forgot-password'/>">Forgot password?</a>
            </div>

            <button class="primary" type="submit" id="loginBtn">
                <span class="spinner" aria-hidden="true"></span>
                <span class="btn-label">Sign in</span>
                <svg class="arrow" width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 8h10M9 4l4 4-4 4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
        </form>

        <div class="auth-foot">
            <svg viewBox="0 0 16 16" fill="none"><rect x="3" y="7" width="10" height="7" rx="1.5" stroke="currentColor" stroke-width="1.3"/><path d="M5.5 7V5a2.5 2.5 0 0 1 5 0v2" stroke="currentColor" stroke-width="1.3"/></svg>
            Secure institutional access
        </div>
    </div>
</section>

<script>
    (function () {
        var toggle = document.getElementById('togglePassword');
        var pwd = document.getElementById('password');
        var eye = document.getElementById('eyeIcon');
        if (toggle && pwd) {
            toggle.addEventListener('click', function () {
                var show = pwd.type === 'password';
                pwd.type = show ? 'text' : 'password';
                toggle.setAttribute('aria-pressed', String(show));
                toggle.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
                eye.style.opacity = show ? '0.6' : '1';
            });
        }

        var form = document.getElementById('loginForm');
        var btn = document.getElementById('loginBtn');
        if (form && btn) {
            form.addEventListener('submit', function () {
                // Purely visual loading state; does not intercept or alter the existing submission.
                btn.classList.add('loading');
                btn.disabled = true;
            });
        }
    })();
</script>

</body>
</html>
