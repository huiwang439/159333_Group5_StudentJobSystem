document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("employerRegisterForm");

    const roleInput = document.getElementById("employer-role");
    const fullNameInput = document.getElementById("employer-full-name");
    const passwordInput = document.getElementById("employer-register-password");
    const confirmPasswordInput = document.getElementById("employer-confirm-password");
    const emailInput = document.getElementById("employer-register-email");
    const phoneInput = document.getElementById("employer-phone");
    const companyNameInput = document.getElementById("employer-company-name");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (passwordInput.value !== confirmPasswordInput.value) {
            showText("authMessage", "Passwords do not match.", true);
            return;
        }

        showText("authMessage", "Registering...");

        try {
            await apiPost("/auth/register", {
                fullName: fullNameInput.value.trim(),
                email: emailInput.value.trim(),
                password: passwordInput.value,
                role: roleInput.value,
                phone: phoneInput.value.trim()
            });

            const loginData = await apiPost("/auth/login", {
                email: emailInput.value.trim(),
                password: passwordInput.value
            });

            saveAuth(loginData);

            await apiPut("/employers/profile", {
                companyName: companyNameInput.value.trim(),
                industry: "",
                companySize: "",
                website: "",
                location: "",
                companyDescription: "",
                contactPerson: fullNameInput.value.trim(),
                contactEmail: emailInput.value.trim()
            });

            showText("authMessage", "Registration successful.");
            window.location.href = "/login-employer.html";
        } catch (error) {
            clearAuth();
            showText("authMessage", error.message, true);
        }
    });
});