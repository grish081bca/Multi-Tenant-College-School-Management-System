document.querySelectorAll('form[action*="/delete"]').forEach(form => form.addEventListener('submit', event => {
    if (!confirm('Delete this record?')) {
        event.preventDefault();
    }
}));

const dateTime = document.getElementById('currentDateTime');
if (dateTime) {
    const updateDateTime = () => {
        dateTime.textContent = new Date().toLocaleString();
    };
    updateDateTime();
    setInterval(updateDateTime, 1000);
}

const lastLogin = document.getElementById('currentLastLoginAt');
if (lastLogin?.dataset.utc) {
    const lastLoginDate = new Date(lastLogin.dataset.utc);
    if (!Number.isNaN(lastLoginDate.getTime())) {
        lastLogin.textContent = lastLoginDate.toLocaleString();
    }
}

document.querySelectorAll('.profile-trigger').forEach(trigger => {
    trigger.addEventListener('click', event => {
        event.stopPropagation();
        const menu = trigger.closest('.profile-menu');
        const isOpen = menu.classList.toggle('open');
        trigger.setAttribute('aria-expanded', String(isOpen));
    });
});

document.querySelectorAll('.menu-toggle').forEach(trigger => {
    trigger.addEventListener('click', (e) => {
        const group = trigger.closest('.menu-group');
        if (!group) return;
        const isCollapsed = appShell && appShell.classList.contains('sidebar-collapsed');
        if (isCollapsed) {
            appShell.classList.remove('sidebar-collapsed');
            localStorage.setItem('erp.sidebarCollapsed', 'false');
            document.querySelectorAll('.sidebar-toggle').forEach(t => t.setAttribute('aria-expanded', 'true'));
        }
        group.classList.toggle('open');
        saveOpenGroups();
        if (e.target && e.target.tagName === 'BUTTON') e.preventDefault();
    });
});

function getGroupKey(group) {
    return group.dataset.menuCode || group.dataset.menuId || null;
}
function getOpenGroups() {
    return Array.from(document.querySelectorAll('.menu-group.open'))
        .map(g => getGroupKey(g))
        .filter(Boolean);
}
function saveOpenGroups() {
    try { localStorage.setItem('erp.openMenuGroups', JSON.stringify(getOpenGroups())); } catch(e) {}
}
function restoreOpenGroups() {
    try {
        const raw = localStorage.getItem('erp.openMenuGroups');
        if (!raw) return;
        const arr = JSON.parse(raw);
        if (!Array.isArray(arr)) return;
        arr.forEach(code => {
            const group = Array.from(document.querySelectorAll('.menu-group'))
                .find(gg => (gg.dataset.menuCode || gg.dataset.menuId) == code);
            if (group) group.classList.add('open');
        });
    } catch(e) {}
}

function normalizePath(path) {
    if (!path) return '';
    return path.replace(/\/+$/, '') || '/';
}

function findCurrentMenuLink() {
    const currentPath = normalizePath(window.location.pathname);
    return Array.from(document.querySelectorAll('.sidebar-nav a[href]')).find(link => {
        try {
            const linkPath = normalizePath(new URL(link.href, window.location.origin).pathname);
            return linkPath === currentPath;
        } catch(e) {
            return false;
        }
    });
}

function applyCurrentMenuState() {
    const activeLink = findCurrentMenuLink();
    if (!activeLink) return;
    activeLink.classList.add('active');
    activeLink.setAttribute('aria-current', 'page');
    const activeGroup = activeLink.closest('.menu-group');
    if (activeGroup) {
        activeGroup.classList.add('open');
        saveOpenGroups();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.menu-group.open').forEach(g => g.classList.remove('open'));
    const appShell = document.getElementById('appShell');
    if (appShell && !appShell.classList.contains('sidebar-collapsed')) {
        restoreOpenGroups();
    }
    applyCurrentMenuState();
});

const appShell = document.getElementById('appShell');
const sidebarToggle = document.querySelector('.sidebar-toggle');
if (appShell && sidebarToggle) {
    const storedState = localStorage.getItem('erp.sidebarCollapsed');
    if (storedState === 'true') {
        localStorage.setItem('erp.sidebarCollapsed', 'false');
        sidebarToggle.setAttribute('aria-expanded', 'true');
    }
    sidebarToggle.addEventListener('click', event => {
        event.stopPropagation();
        const collapsed = appShell.classList.toggle('sidebar-collapsed');
        if (collapsed) {
            saveOpenGroups();
            document.querySelectorAll('.menu-group.open').forEach(g => g.classList.remove('open'));
        } else {
            restoreOpenGroups();
            applyCurrentMenuState();
        }
        localStorage.setItem('erp.sidebarCollapsed', String(collapsed));
        sidebarToggle.setAttribute('aria-expanded', String(!collapsed));
    });


    document.querySelectorAll('.super-menu').forEach(el => {
        el.addEventListener('click', function(e) {
            if (appShell.classList.contains('sidebar-collapsed')) {
                if (e.target && (e.target.tagName === 'A' || e.target.closest('a'))) {
                    e.preventDefault();
                }
                appShell.classList.remove('sidebar-collapsed');
                localStorage.setItem('erp.sidebarCollapsed', 'false');
                document.querySelectorAll('.sidebar-toggle').forEach(t => t.setAttribute('aria-expanded', 'true'));
                const group = el.closest('.menu-group');
                if (group) {
                    document.querySelectorAll('.menu-group.open').forEach(g => g.classList.remove('open'));
                    group.classList.add('open');
                    saveOpenGroups();
                }
            }
        });
    });
}


document.addEventListener('click', () => {
    document.querySelectorAll('.profile-menu.open').forEach(menu => {
        menu.classList.remove('open');
        const trigger = menu.querySelector('.profile-trigger');
        if (trigger) {
            trigger.setAttribute('aria-expanded', 'false');
        }
    });
});
