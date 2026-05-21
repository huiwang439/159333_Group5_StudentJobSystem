document.addEventListener("DOMContentLoaded", async () => {
    if (!requireRole("admin")) return;

    function setText(id, value) {
        const el = document.getElementById(id);
        if (el) el.textContent = value ?? 0;
    }

    async function loadDashboard() {
        try {
            showText("dashboardMessage", "Loading dashboard...");

            const [
                summaryData,
                jobsData,
                usersData,
                applicationsData
            ] = await Promise.all([
                apiGet("/admin/dashboard"),
                apiGet("/admin/dashboard/jobs"),
                apiGet("/admin/dashboard/users"),
                apiGet("/admin/dashboard/applications")
            ]);

            setText("totalUsers", summaryData.totalUsers);
            setText("totalStudents", summaryData.totalStudents);
            setText("totalEmployers", summaryData.totalEmployers);
            setText("totalJobs", summaryData.totalJobs);
            setText("totalApplications", summaryData.totalApplications);

            setText("totalAdmins", usersData.adminUsers);
            setText("totalStaff", usersData.staffUsers);

            setText("pendingJobs", jobsData.pendingJobs);
            setText("approvedJobs", jobsData.approvedJobs);
            setText("rejectedJobs", jobsData.rejectedJobs);

            setText("submittedApplications", applicationsData.submittedApplications);

            showText("dashboardMessage", "Dashboard loaded.");
        } catch (error) {
            showText("dashboardMessage", error.message, true);
        }
    }

    loadDashboard();
});