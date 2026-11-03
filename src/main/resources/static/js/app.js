document.querySelectorAll('form[action*="/delete"]').forEach(form => form.addEventListener('submit', event => {
    if (!confirm('Delete this record?')) {
        event.preventDefault();
    }
}));

const dateTime = document.getElementById('currentDateTime');
const formatDisplayDateTime = value => {
    const date = value instanceof Date ? value : new Date(value);
    if (Number.isNaN(date.getTime())) return '';
    const twoDigits = part => String(part).padStart(2, '0');
    return `${date.getFullYear()}-${twoDigits(date.getMonth() + 1)}-${twoDigits(date.getDate())} ${twoDigits(date.getHours())}:${twoDigits(date.getMinutes())}`;
};

if (dateTime) {
    const updateDateTime = () => {
        dateTime.textContent = formatDisplayDateTime(new Date());
    };
    updateDateTime();
    setInterval(updateDateTime, 60000);
}

const lastLogin = document.getElementById('currentLastLoginAt');
if (lastLogin?.dataset.utc) {
    const lastLoginDate = new Date(lastLogin.dataset.utc);
    if (!Number.isNaN(lastLoginDate.getTime())) {
        lastLogin.textContent = formatDisplayDateTime(lastLoginDate);
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

const menuSearchInput = document.getElementById('sidebarMenuSearch');

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
        if (menuSearchInput?.value.trim()) {
            group.classList.add('open');
        } else {
            group.classList.toggle('open');
            saveOpenGroups();
        }
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
    try {
        localStorage.setItem('erp.openMenuGroups', JSON.stringify(getOpenGroups()));
    } catch (e) {
    }
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
    } catch (e) {
    }
}

function applyMenuSearch() {
    const query = menuSearchInput?.value.trim().toLocaleLowerCase() || '';
    const groups = document.querySelectorAll('.sidebar-nav .menu-group');

    if (!query) {
        groups.forEach(group => {
            group.hidden = false;
            group.querySelectorAll('.sub-menu').forEach(link => {
                link.hidden = false;
            });
            group.classList.remove('open');
        });
        restoreOpenGroups();
        applyCurrentMenuState();
        return;
    }

    groups.forEach(group => {
        const menuText = group.querySelector('.super-menu .menu-text')?.textContent || '';
        const menuMatches = menuText.trim().toLocaleLowerCase().includes(query);
        const subMenus = Array.from(group.querySelectorAll('.sub-menu'));
        let hasMatchingSubMenu = false;

        subMenus.forEach(link => {
            const matches = link.textContent.trim().toLocaleLowerCase().includes(query);
            link.hidden = !menuMatches && !matches;
            hasMatchingSubMenu ||= matches;
        });

        group.hidden = !menuMatches && !hasMatchingSubMenu;
        if (group.hidden) {
            group.classList.remove('open');
        } else if (subMenus.some(link => !link.hidden)) {
            group.classList.add('open');
        }
    });
}

menuSearchInput?.addEventListener('input', applyMenuSearch);

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
        } catch (e) {
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

const sidebarScrollStorageKey = 'erp.sidebarMenuScrollTop';

function restoreSavedSidebarScrollPosition() {
    const menuList = document.querySelector('.sidebar-nav');
    if (!menuList) return;
    try {
        const savedPosition = Number(sessionStorage.getItem(sidebarScrollStorageKey));
        if (Number.isFinite(savedPosition) && savedPosition >= 0) {
            menuList.scrollTop = savedPosition;
        }
    } catch (e) {
    }
}

function keepSidebarItemsVisible(sidebar, items, priorityItem) {
    if (!sidebar || !items.length) return;
    const sidebarRect = sidebar.getBoundingClientRect();
    const visibleTop = sidebarRect.top + sidebar.clientTop;
    const visibleBottom = visibleTop + sidebar.clientHeight;
    const itemRects = items.filter(Boolean).map(item => item.getBoundingClientRect());
    if (!itemRects.length) return;
    const top = Math.min(...itemRects.map(rect => rect.top));
    const bottom = Math.max(...itemRects.map(rect => rect.bottom));

    if (bottom - top <= sidebar.clientHeight) {
        if (top < visibleTop) {
            sidebar.scrollTop -= visibleTop - top;
        } else if (bottom > visibleBottom) {
            sidebar.scrollTop += bottom - visibleBottom;
        }
        return;
    }

    const priorityRect = priorityItem?.getBoundingClientRect();
    if (priorityRect?.top < visibleTop) sidebar.scrollTop -= visibleTop - priorityRect.top;
    else if (priorityRect?.bottom > visibleBottom) sidebar.scrollTop += priorityRect.bottom - visibleBottom;
}

function restoreSidebarScroll() {
    const menuList = document.querySelector('.sidebar-nav');
    if (!menuList) return;

    restoreSavedSidebarScrollPosition();

    const activeLink = document.querySelector('.sidebar-nav a[aria-current="page"]');
    if (!activeLink) return;

    const activeGroup = activeLink.closest('.menu-group');
    const parentMenu = activeGroup?.querySelector('.super-menu');
    keepSidebarItemsVisible(menuList, [parentMenu, activeLink], activeLink);

    try {
        sessionStorage.setItem(sidebarScrollStorageKey, String(menuList.scrollTop));
    } catch (e) {
    }
}

restoreSavedSidebarScrollPosition();

document.querySelector('.sidebar-nav')?.addEventListener('click', event => {
    const link = event.target.closest('a[href]');
    if (!link || event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;

    const menuList = document.querySelector('.sidebar-nav');
    if (!menuList) return;
    try {
        sessionStorage.setItem(sidebarScrollStorageKey, String(menuList.scrollTop));
    } catch (e) {
    }
});

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.menu-group.open').forEach(g => g.classList.remove('open'));
    const appShell = document.getElementById('appShell');
    if (appShell && !appShell.classList.contains('sidebar-collapsed')) {
        restoreOpenGroups();
    }
    applyCurrentMenuState();
    restoreSidebarScroll();
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
        el.addEventListener('click', function (e) {
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
