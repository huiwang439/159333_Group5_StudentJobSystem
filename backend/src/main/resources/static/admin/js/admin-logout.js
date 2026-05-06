document.addEventListener("DOMContentLoaded", () => {
    const logoutBtn = document.getElementById("adminLogoutBtn");

    if (!logoutBtn) return;

    logoutBtn.addEventListener("click", () => {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        localStorage.removeItem("userId");
        localStorage.removeItem("fullName");

        window.location.href = "/admin/login.html";
    });
});