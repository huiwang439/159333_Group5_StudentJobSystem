const tokenInput = document.getElementById("tokenInput");
const roleFilter = document.getElementById("roleFilter");
const loadUsersBtn = document.getElementById("loadUsersBtn");
const saveTokenBtn = document.getElementById("saveTokenBtn");
const userTableBody = document.getElementById("userTableBody");
const messageBox = document.getElementById("messageBox");
const searchInput = document.getElementById("searchInput");
const searchBtn = document.getElementById("searchBtn");
const resetBtn = document.getElementById("resetBtn");
const menuToggleBtn = document.getElementById("menuToggleBtn");
const sidebar = document.getElementById("sidebar");

let mockUsers = [
    {
        id: 1,
        fullName: "Alice Zhang",
        email: "alice@example.com",
        role: "student",
        phone: "0211111111",
        companyName: "-",
        accountStatus: "active",
        createdAt: "2026-03-20 10:00:00"
    },
    {
        id: 2,
        fullName: "Bob Chen",
        email: "bob@example.com",
        role: "student",
        phone: "0222222222",
        companyName: "-",
        accountStatus: "banned",
        createdAt: "2026-03-21 11:30:00"
    },
    {
        id: 3,
        fullName: "Future Tech HR",
        email: "hr@futuretech.com",
        role: "employer",
        phone: "0333333333",
        companyName: "Future Tech",
        accountStatus: "active",
        createdAt: "2026-03-22 09:15:00"
    },
    {
        id: 4,
        fullName: "Green Company",
        email: "contact@greencompany.com",
        role: "employer",
        phone: "0444444444",
        companyName: "Green Company",
        accountStatus: "active",
        createdAt: "2026-03-23 14:20:00"
    }
];

function showMessage(message) {
    messageBox.textContent = message;
}

function getToken() {
    return localStorage.getItem("adminToken") || "";
}

function saveToken() {
    const token = tokenInput.value.trim();
    localStorage.setItem("adminToken", token);
    showMessage("Token saved.");
}

function initToken() {
    tokenInput.value = getToken();
}

function formatTime(time) {
    if (!time) return "";
    return time;
}

function toggleSidebar() {
    sidebar.classList.toggle("hidden");
    menuToggleBtn.textContent = sidebar.classList.contains("hidden") ? "▶" : "◀";
}

function getFilteredUsers() {
    const role = roleFilter.value;
    const keyword = searchInput.value.trim().toLowerCase();

    let filteredUsers = mockUsers;

    if (role) {
        filteredUsers = filteredUsers.filter(user => user.role === role);
    }

    if (keyword) {
        filteredUsers = filteredUsers.filter(user => {
            const fullName = (user.fullName || "").toLowerCase();
            const email = (user.email || "").toLowerCase();
            return fullName.includes(keyword) || email.includes(keyword);
        });
    }

    return filteredUsers;
}

function renderUsers(users) {
    userTableBody.innerHTML = "";

    if (!users || users.length === 0) {
        userTableBody.innerHTML = `<tr><td colspan="9">No users found</td></tr>`;
        return;
    }

    users.forEach(user => {
        const tr = document.createElement("tr");

        const isBanned = user.accountStatus === "banned";
        const statusClass = isBanned ? "status-banned" : "status-active";

        tr.innerHTML = `
            <td>${user.id ?? ""}</td>
            <td>${user.fullName ?? ""}</td>
            <td>${user.email ?? ""}</td>
            <td>${user.role ?? ""}</td>
            <td>${user.phone ?? ""}</td>
            <td>${user.companyName ?? "-"}</td>
            <td>
                <span class="${statusClass}">
                    ${user.accountStatus ?? ""}
                </span>
            </td>
            <td>${formatTime(user.createdAt)}</td>
            <td>
                <button
                    class="action-btn"
                    onclick="updateUserStatus(${user.id}, 'banned')"
                    ${isBanned ? "disabled" : ""}
                >
                    Ban
                </button>
                <button
                    class="action-btn"
                    onclick="updateUserStatus(${user.id}, 'active')"
                    ${!isBanned ? "disabled" : ""}
                >
                    Restore
                </button>
            </td>
        `;

        userTableBody.appendChild(tr);
    });
}

function loadUsers() {
    const filteredUsers = getFilteredUsers();
    renderUsers(filteredUsers);
    showMessage("Users loaded successfully.");
}

function updateUserStatus(userId, accountStatus) {
    mockUsers = mockUsers.map(user => {
        if (user.id === userId) {
            return {
                ...user,
                accountStatus: accountStatus
            };
        }
        return user;
    });

    loadUsers();
    showMessage(`User ${userId} status updated to ${accountStatus}.`);
}

function resetFilters() {
    roleFilter.value = "";
    searchInput.value = "";
    loadUsers();
    showMessage("Filters reset.");
}

saveTokenBtn.addEventListener("click", saveToken);
loadUsersBtn.addEventListener("click", loadUsers);
searchBtn.addEventListener("click", loadUsers);
resetBtn.addEventListener("click", resetFilters);
menuToggleBtn.addEventListener("click", toggleSidebar);

searchInput.addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
        loadUsers();
    }
});

initToken();
loadUsers();