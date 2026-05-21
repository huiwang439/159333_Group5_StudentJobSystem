(function () {
    const menuToggleBtn = document.getElementById("menuToggleBtn");
    const sidebar = document.getElementById("sidebar");
    if (!menuToggleBtn || !sidebar) return;

    menuToggleBtn.addEventListener("click", function () {
        sidebar.classList.toggle("hidden");
        menuToggleBtn.textContent = sidebar.classList.contains("hidden") ? "▶" : "◀";
    });

    const groups = document.querySelectorAll(".menu-group");

    groups.forEach(group => {
        const parent = group.querySelector(".menu-item.parent");
        const subMenu = group.querySelector(".sub-menu");
        if (!parent || !subMenu) return;

        const hasActiveChild = !!group.querySelector(".sub-menu-item.active");

        if (hasActiveChild) {
            group.classList.add("open");
            subMenu.hidden = false;
        } else {
            subMenu.hidden = false;
            group.classList.add("open");
        }

        if (parent.dataset.href) {
            parent.addEventListener("click", function (event) {
                if (event.target.closest(".toggle-only")) return;
                window.location.href = parent.dataset.href;
            });
        }
    });

    function findMenuItemByText(text) {
        const items = document.querySelectorAll(".menu-item, .sub-menu-item");

        return Array.from(items).find(item => {
            return item.textContent.trim().replace(/\s+/g, " ").includes(text);
        });
    }

    function setBadge(menuText, count) {
        const item = findMenuItemByText(menuText);
        if (!item) return;

        const oldBadge = item.querySelector(".sidebar-badge");
        if (oldBadge) oldBadge.remove();

        if (!count || count <= 0) return;

        const badge = document.createElement("span");
        badge.className = "sidebar-badge";
        badge.textContent = count > 99 ? "99+" : String(count);

        item.appendChild(badge);
    }

    async function loadSidebarBadges() {
        if (typeof apiGet !== "function") return;
        if (typeof getRole === "function" && getRole() !== "admin") return;

        try {
            const [
                verificationResult,
                reportResult,
                abnormalResult
            ] = await Promise.allSettled([
                apiGet("/verification/admin?reviewStatus=pending"),
                apiGet("/reports/admin?status=pending"),
                apiGet("/admin/abnormal")
            ]);

            const pendingVerificationCount =
                verificationResult.status === "fulfilled" && Array.isArray(verificationResult.value)
                    ? verificationResult.value.length
                    : 0;

            const pendingReportCount =
                reportResult.status === "fulfilled" && Array.isArray(reportResult.value)
                    ? reportResult.value.length
                    : 0;

            const abnormalCount =
                abnormalResult.status === "fulfilled" && Array.isArray(abnormalResult.value)
                    ? abnormalResult.value.length
                    : 0;

            setBadge("Company Verification", pendingVerificationCount);
            setBadge("Report Handling", pendingReportCount);
            setBadge("Abnormal Data / Behavior", abnormalCount);
        } catch (error) {
            console.warn("Failed to load sidebar badges:", error.message);
        }
    }

    loadSidebarBadges();
})();