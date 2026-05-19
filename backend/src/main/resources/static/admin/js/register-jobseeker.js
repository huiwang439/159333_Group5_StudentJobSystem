document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("jobseekerRegisterForm");

    const roleInput = document.getElementById("jobseeker-role");
    const fullNameInput = document.getElementById("jobseeker-full-name");
    const emailInput = document.getElementById("jobseeker-register-email");
    const phoneInput = document.getElementById("jobseeker-phone");
    const passwordInput = document.getElementById("jobseeker-register-password");
    const confirmPasswordInput = document.getElementById("jobseeker-confirm-password");

    const universityInput = document.getElementById("jobseeker-university");
    const majorInput = document.getElementById("jobseeker-major");
    const degreeLevelInput = document.getElementById("jobseeker-degree-level");
    const graduationYearInput = document.getElementById("jobseeker-graduation-year");
    const studentTypeInput = document.getElementById("jobseeker-student-type");
    const preferredJobTypeInput = document.getElementById("jobseeker-preferred-job-type");
    const preferredLocationInput = document.getElementById("jobseeker-preferred-location");
    const skillsInput = document.getElementById("jobseeker-skills");
    const bioInput = document.getElementById("jobseeker-bio");

    function normalizeGraduationYear(value) {
        const trimmed = value.trim();
        if (!trimmed) {
            return null;
        }

        const year = Number(trimmed);
        if (!Number.isInteger(year) || year < 1900 || year > 2100) {
            throw new Error("Graduation year must be a valid year between 1900 and 2100.");
        }

        return year;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (passwordInput.value !== confirmPasswordInput.value) {
            showText("authMessage", "Passwords do not match.", true);
            return;
        }

        if (!degreeLevelInput.value) {
            showText("authMessage", "Please select your degree level.", true);
            return;
        }

        if (!studentTypeInput.value) {
            showText("authMessage", "Please select your student type.", true);
            return;
        }

        showText("authMessage", "Registering...");

        try {
            const graduationYear = normalizeGraduationYear(graduationYearInput.value);

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

            await apiPut("/students/profile", {
                university: universityInput.value.trim(),
                major: majorInput.value.trim(),
                degreeLevel: degreeLevelInput.value,
                graduationYear: graduationYear,
                studentType: studentTypeInput.value,
                preferredJobType: preferredJobTypeInput.value.trim(),
                preferredLocation: preferredLocationInput.value.trim(),
                bio: bioInput.value.trim(),
                skills: skillsInput.value.trim()
            });

            clearAuth();
            showText("authMessage", "Registration successful. Redirecting to login...");
            window.location.href = "./login-jobseeker.html";
        } catch (error) {
            clearAuth();
            showText("authMessage", error.message, true);
        }
    });
});