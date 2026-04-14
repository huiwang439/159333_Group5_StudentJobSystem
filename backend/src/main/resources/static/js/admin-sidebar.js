(function () {
    const menuToggleBtn = document.getElementById('menuToggleBtn');
    const sidebar = document.getElementById('sidebar');
    if (!menuToggleBtn || !sidebar) return;

    menuToggleBtn.addEventListener('click', function () {
        sidebar.classList.toggle('hidden');
        menuToggleBtn.textContent = sidebar.classList.contains('hidden') ? '▶' : '◀';
    });

    const groups = document.querySelectorAll('.menu-group');
    groups.forEach(group => {
        const parent = group.querySelector('.menu-item.parent');
        const subMenu = group.querySelector('.sub-menu');
        if (!parent || !subMenu) return;

        const hasActiveChild = !!group.querySelector('.sub-menu-item.active');
        if (hasActiveChild) {
            group.classList.add('open');
            subMenu.hidden = false;
        } else {
            subMenu.hidden = false;
            group.classList.add('open');
        }

        if (parent.dataset.href) {
            parent.addEventListener('click', function (event) {
                if (event.target.closest('.toggle-only')) return;
                window.location.href = parent.dataset.href;
            });
        }
    });
})();