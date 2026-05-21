document.addEventListener("DOMContentLoaded", () => {
    if (!requireRole("admin")) return;

    const roleFilter = document.getElementById("roleFilter");
    const loadUsersBtn = document.getElementById("loadUsersBtn");
    const userTableBody = document.getElementById("userTableBody");
    const messageBox = document.getElementById("messageBox");
    const searchInput = document.getElementById("searchInput");
    const searchBtn = document.getElementById("searchBtn");
    const resetBtn = document.getElementById("resetBtn");

    const staffFullName = document.getElementById("staffFullName");
    const staffEmail = document.getElementById("staffEmail");
    const staffPassword = document.getElementById("staffPassword");
    const staffPhone = document.getElementById("staffPhone");
    const createStaffBtn = document.getElementById("createStaffBtn");

    const userListView = document.getElementById("userListView");
    const userDetailView = document.getElementById("userDetailView");
    const userDetailCard = document.getElementById("userDetailCard");
    const backToUsersBtn = document.getElementById("backToUsersBtn");

    function showMessage(message, isError = false) {
        messageBox.textContent = message || "";
        messageBox.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function formatValue(value) {
        return value === null || value === undefined || value === "" ? "-" : value;
    }

    function showListView() {
        userDetailView.classList.add("hidden");
        userListView.classList.remove("hidden");
        showMessage("");
    }

    function showDetailView() {
        userListView.classList.add("hidden");
        userDetailView.classList.remove("hidden");
        showMessage("");
    }

    function buildPath() {
        const params = new URLSearchParams();

        if (roleFilter.value) {
            params.append("role", roleFilter.value);
        }

        if (searchInput.value.trim()) {
            params.append("keyword", searchInput.value.trim());
        }

        return params.toString() ? `/admin/users?${params}` : "/admin/users";
    }

    function statusClass(status) {
        if (status === "active") return "status-active";
        return "status-banned";
    }

    function renderUsers(users) {
        userTableBody.innerHTML = "";

        if (!users || users.length === 0) {
            userTableBody.innerHTML = `<tr><td colspan="9">No users found</td></tr>`;
            return;
        }

        users.forEach(user => {
            const isActive = user.accountStatus === "active";
            const isStaff = user.role === "staff";
            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td>${formatValue(user.userId)}</td>
                <td>${formatValue(user.fullName)}</td>
                <td>${formatValue(user.email)}</td>
                <td>${formatValue(user.role)}</td>
                <td>${formatValue(user.phone)}</td>
                <td>${formatValue(user.companyName)}</td>
                <td><span class="${statusClass(user.accountStatus)}">${formatValue(user.accountStatus)}</span></td>
                <td>${formatValue(user.createdAt)}</td>
                <td>
                    <button class="view-user-btn" data-id="${user.userId}">View</button>
                    ${
                        isStaff
                            ? `<button class="edit-staff-btn" data-id="${user.userId}" data-name="${user.fullName ?? ""}" data-phone="${user.phone ?? ""}" data-status="${user.accountStatus ?? "active"}">Edit Staff</button>`
                            : ""
                    }
                    <button class="action-btn" data-id="${user.userId}" data-status="banned" ${!isActive ? "disabled" : ""}>Ban</button>
                    <button class="action-btn" data-id="${user.userId}" data-status="active" ${isActive ? "disabled" : ""}>Restore</button>
                </td>
            `;

            userTableBody.appendChild(tr);
        });

        userTableBody.querySelectorAll(".view-user-btn").forEach(btn => {
            btn.addEventListener("click", () => {
                loadUserDetail(btn.dataset.id);
            });
        });

        userTableBody.querySelectorAll(".action-btn").forEach(btn => {
            btn.addEventListener("click", async () => {
                await updateUserStatus(btn.dataset.id, btn.dataset.status);
            });
        });

        userTableBody.querySelectorAll(".edit-staff-btn").forEach(btn => {
            btn.addEventListener("click", async () => {
                await editStaff(
                    btn.dataset.id,
                    btn.dataset.name,
                    btn.dataset.phone,
                    btn.dataset.status
                );
            });
        });
    }

    function renderUserDetail(user) {
        userDetailCard.innerHTML = `
            <div class="detail-grid">
                <div class="detail-item">
                    <span class="detail-label">User ID</span>
                    <span class="detail-value">${formatValue(user.userId)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Full Name</span>
                    <span class="detail-value">${formatValue(user.fullName)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Email</span>
                    <span class="detail-value">${formatValue(user.email)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Role</span>
                    <span class="detail-value">${formatValue(user.role)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Phone</span>
                    <span class="detail-value">${formatValue(user.phone)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Company</span>
                    <span class="detail-value">${formatValue(user.companyName)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Account Status</span>
                    <span class="detail-value">${formatValue(user.accountStatus)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Created At</span>
                    <span class="detail-value">${formatValue(user.createdAt)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Updated At</span>
                    <span class="detail-value">${formatValue(user.updatedAt)}</span>
                </div>
            </div>
        `;
    }

    async function loadUsers() {
        try {
            showMessage("Loading users...");
            const users = await apiGet(buildPath());
            renderUsers(users);
            showListView();
            showMessage("Users loaded successfully.");
        } catch (error) {
            renderUsers([]);
            showMessage(error.message, true);
        }
    }

    async function loadUserDetail(userId) {
        try {
            showMessage("Loading user details...");
            const user = await apiGet(`/admin/users/${userId}`);
            renderUserDetail(user);
            showDetailView();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    async function updateUserStatus(userId, status) {
        try {
            await apiPatch(`/admin/users/${userId}/status`, { status });
            showMessage(`User ${userId} status updated to ${status}.`);
            await loadUsers();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    async function createStaff() {
        try {
            if (!staffFullName.value.trim()) throw new Error("Full name is required.");
            if (!staffEmail.value.trim()) throw new Error("Email is required.");
            if (!staffPassword.value.trim()) throw new Error("Password is required.");

            await apiPost("/admin/staff", {
                fullName: staffFullName.value.trim(),
                email: staffEmail.value.trim(),
                password: staffPassword.value,
                phone: staffPhone.value.trim()
            });

            staffFullName.value = "";
            staffEmail.value = "";
            staffPassword.value = "";
            staffPhone.value = "";

            roleFilter.value = "staff";
            showMessage("Staff created successfully.");
            await loadUsers();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    async function editStaff(staffId, currentName, currentPhone, currentStatus) {
        const fullName = prompt("Enter staff full name:", currentName || "");
        if (fullName === null) return;

        const phone = prompt("Enter staff phone:", currentPhone || "");
        if (phone === null) return;

        const status = prompt("Enter status: active, banned, or disabled", currentStatus || "active");
        if (status === null) return;

        try {
            await apiPut(`/admin/staff/${staffId}`, {
                fullName: fullName.trim(),
                phone: phone.trim(),
                status: status.trim()
            });

            showMessage("Staff updated successfully.");
            await loadUsers();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    function applyRoleFromQuery() {
        const params = new URLSearchParams(window.location.search);
        const role = params.get("role");

        if (["student", "employer", "staff"].includes(role)) {
            roleFilter.value = role;
        }
    }

    function resetFilters() {
        roleFilter.value = "";
        searchInput.value = "";
        window.history.replaceState({}, "", "/admin/admin-users.html");
        loadUsers();
    }

    createStaffBtn.addEventListener("click", createStaff);
    loadUsersBtn.addEventListener("click", loadUsers);
    searchBtn.addEventListener("click", loadUsers);
    resetBtn.addEventListener("click", resetFilters);
    backToUsersBtn.addEventListener("click", showListView);

    searchInput.addEventListener("keydown", e => {
        if (e.key === "Enter") loadUsers();
    });

    applyRoleFromQuery();
    loadUsers();
});