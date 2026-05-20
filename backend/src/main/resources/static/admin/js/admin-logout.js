document.addEventListener("DOMContentLoaded", () => {
    const logoutBtn = document.getElementById("adminLogoutBtn");

    if (!logoutBtn) return;

    logoutBtn.addEventListener("click", async () => {
        try {
            await apiPost("/auth/logout", {});
        } catch (error) {
            console.warn("Logout request failed:", error.message);
        } finally {
            clearAuth();
            window.location.href = "/index/index.html";
        }
    });
});