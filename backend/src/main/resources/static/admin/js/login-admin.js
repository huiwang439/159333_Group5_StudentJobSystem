document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("adminLoginForm");
    const emailInput = document.getElementById("admin-email");
    const passwordInput = document.getElementById("admin-password");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        showText("authMessage", "Logging in...");

        try {
            const data = await apiPost("/auth/login", {
                email: emailInput.value.trim(),
                password: passwordInput.value
            });

            if (data.role !== "admin") {
                throw new Error("This account is not an admin account.");
            }

            saveAuth(data);
            showText("authMessage", "Login successful.");
            window.location.href = "/admin/admin-dashboard.html";
        } catch (error) {
            clearAuth();
            showText("authMessage", error.message, true);
        }
    });
});