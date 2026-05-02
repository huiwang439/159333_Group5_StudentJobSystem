document.addEventListener("DOMContentLoaded", () => {
    if (!requireRole("admin")) return;

    const roleFilter = document.getElementById("roleFilter");
    const loadUsersBtn = document.getElementById("loadUsersBtn");
    const userTableBody = document.getElementById("userTableBody");
    const messageBox = document.getElementById("messageBox");
    const searchInput = document.getElementById("searchInput");
    const searchBtn = document.getElementById("searchBtn");
    const resetBtn = document.getElementById("resetBtn");

    function showMessage(message, isError = false) {
        messageBox.textContent = message || "";
        messageBox.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function buildPath() {
        const params = new URLSearchParams();
        if (roleFilter.value) params.append("role", roleFilter.value);
        if (searchInput.value.trim()) params.append("keyword", searchInput.value.trim());
        return params.toString() ? `/admin/users?${params}` : "/admin/users";
    }

    function statusClass(status) {
        return status === "active" ? "status-active" : "status-banned";
    }

    function renderUsers(users) {
        userTableBody.innerHTML = "";

        if (!users || users.length === 0) {
            userTableBody.innerHTML = `<tr><td colspan="9">No users found</td></tr>`;
            return;
        }

        users.forEach(user => {
            const isActive = user.accountStatus === "active";
            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td>${user.userId ?? "-"}</td>
                <td>${user.fullName ?? "-"}</td>
                <td>${user.email ?? "-"}</td>
                <td>${user.role ?? "-"}</td>
                <td>${user.phone ?? "-"}</td>
                <td>${user.companyName ?? "-"}</td>
                <td><span class="${statusClass(user.accountStatus)}">${user.accountStatus ?? "-"}</span></td>
                <td>${user.createdAt ?? "-"}</td>
                <td>
                    <button class="action-btn" data-id="${user.userId}" data-status="banned" ${!isActive ? "disabled" : ""}>Ban</button>
                    <button class="action-btn" data-id="${user.userId}" data-status="active" ${isActive ? "disabled" : ""}>Restore</button>
                </td>
            `;

            userTableBody.appendChild(tr);
        });

        userTableBody.querySelectorAll(".action-btn").forEach(btn => {
            btn.addEventListener("click", async () => {
                await updateUserStatus(btn.dataset.id, btn.dataset.status);
            });
        });
    }

    async function loadUsers() {
        try {
            showMessage("Loading users...");
            const users = await apiGet(buildPath());
            renderUsers(users);
            showMessage("Users loaded successfully.");
        } catch (error) {
            renderUsers([]);
            showMessage(error.message, true);
        }
    }

    async function updateUserStatus(userId, status) {
        try {
            showMessage(`Updating user ${userId}...`);
            await apiPatch(`/admin/users/${userId}/status`, { status });
            showMessage(`User ${userId} status updated to ${status}.`);
            await loadUsers();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    function applyRoleFromQuery() {
        const params = new URLSearchParams(window.location.search);
        const role = params.get("role");
        if (role === "student" || role === "employer") roleFilter.value = role;
    }

    function resetFilters() {
        roleFilter.value = "";
        searchInput.value = "";
        window.history.replaceState({}, "", "/admin-users.html");
        loadUsers();
    }

    loadUsersBtn.addEventListener("click", loadUsers);
    searchBtn.addEventListener("click", loadUsers);
    resetBtn.addEventListener("click", resetFilters);
    searchInput.addEventListener("keydown", e => {
        if (e.key === "Enter") loadUsers();
    });

    applyRoleFromQuery();
    loadUsers();
});