document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("staffLoginForm");
    const emailInput = document.getElementById("staff-email");
    const passwordInput = document.getElementById("staff-password");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        showText("authMessage", "Logging in...");

        try {
            const data = await apiPost("/auth/login", {
                email: emailInput.value.trim(),
                password: passwordInput.value
            });

            if (data.role !== "staff") {
                throw new Error("This account is not a staff account.");
            }

            saveAuth(data);
            showText("authMessage", "Login successful.");
            window.location.href = "/admin/career-dashboard.html";
        } catch (error) {
            clearAuth();
            showText("authMessage", error.message, true);
        }
    });
});