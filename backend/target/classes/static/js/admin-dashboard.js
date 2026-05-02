document.addEventListener("DOMContentLoaded", async () => {
    if (!requireRole("admin")) {
        return;
    }

    const totalUsers = document.getElementById("totalUsers");
    const totalStudents = document.getElementById("totalStudents");
    const totalEmployers = document.getElementById("totalEmployers");
    const totalAdmins = document.getElementById("totalAdmins");

    const totalJobs = document.getElementById("totalJobs");
    const pendingJobs = document.getElementById("pendingJobs");
    const approvedJobs = document.getElementById("approvedJobs");
    const rejectedJobs = document.getElementById("rejectedJobs");

    const totalApplications = document.getElementById("totalApplications");

    function setSafeText(element, value) {
        if (!element) return;
        element.textContent = value ?? 0;
    }

    async function loadDashboard() {
        try {
            showText("dashboardMessage", "Loading dashboard...");

            const [
                summaryData,
                jobsData,
                usersData
            ] = await Promise.all([
                apiGet("/admin/dashboard"),
                apiGet("/admin/dashboard/jobs"),
                apiGet("/admin/dashboard/users")
            ]);

            setSafeText(totalUsers, summaryData.totalUsers);
            setSafeText(totalStudents, summaryData.totalStudents);
            setSafeText(totalEmployers, summaryData.totalEmployers);
            setSafeText(totalAdmins, usersData.adminUsers);

            setSafeText(totalJobs, summaryData.totalJobs);
            setSafeText(totalApplications, summaryData.totalApplications);

            setSafeText(pendingJobs, jobsData.pendingJobs);
            setSafeText(approvedJobs, jobsData.approvedJobs);
            setSafeText(rejectedJobs, jobsData.rejectedJobs);

            showText("dashboardMessage", "Dashboard loaded.");
        } catch (error) {
            showText("dashboardMessage", error.message, true);
        }
    }

    loadDashboard();
});