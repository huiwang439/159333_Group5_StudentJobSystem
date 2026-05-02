document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("employerLoginForm");
    const emailInput = document.getElementById("employer-email");
    const passwordInput = document.getElementById("employer-password");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        showText("authMessage", "Logging in...");

        try {
            const data = await apiPost("/auth/login", {
                email: emailInput.value.trim(),
                password: passwordInput.value
            });

            if (data.role !== "employer") {
                throw new Error("This account is not a recruiter account.");
            }

            saveAuth(data);
            showText("authMessage", "Login successful.");

            window.location.href = "/employer-dashboard.html";
        } catch (error) {
            clearAuth();
            showText("authMessage", error.message, true);
        }
    });
});