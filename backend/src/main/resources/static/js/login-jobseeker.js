document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("jobseekerLoginForm");
    const emailInput = document.getElementById("jobseeker-email");
    const passwordInput = document.getElementById("jobseeker-password");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        showText("authMessage", "Logging in...");

        try {
            const data = await apiPost("/auth/login", {
                email: emailInput.value.trim(),
                password: passwordInput.value
            });

            if (data.role !== "student") {
                throw new Error("This account is not a job seeker account.");
            }

            saveAuth(data);
            showText("authMessage", "Login successful.");

            window.location.href = "/jobseeker-dashboard.html";
        } catch (error) {
            clearAuth();
            showText("authMessage", error.message, true);
        }
    });
});