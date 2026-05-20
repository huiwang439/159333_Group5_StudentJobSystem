document.addEventListener("DOMContentLoaded", async () => {
    if (!requireCareerStaff()) return;

    const activeStudents = document.getElementById("activeStudents");
    const participatingEmployers = document.getElementById("participatingEmployers");
    const totalJobs = document.getElementById("totalJobs");
    const pendingJobs = document.getElementById("pendingJobs");
    const approvedJobs = document.getElementById("approvedJobs");
    const totalApplications = document.getElementById("totalApplications");

    function setSafeText(element, value) {
        if (!element) return;
        element.textContent = value ?? 0;
    }

    async function loadDashboard() {
        try {
            showText("dashboardMessage", "Loading dashboard...");

            const [summaryData, jobsData] = await Promise.all([
                apiGet("/admin/dashboard"),
                apiGet("/admin/dashboard/jobs")
            ]);

            setSafeText(activeStudents, summaryData.totalStudents);
            setSafeText(participatingEmployers, summaryData.totalEmployers);
            setSafeText(totalJobs, summaryData.totalJobs);
            setSafeText(totalApplications, summaryData.totalApplications);
            setSafeText(pendingJobs, jobsData.pendingJobs);
            setSafeText(approvedJobs, jobsData.approvedJobs);

            showText("dashboardMessage", "Dashboard loaded.");
        } catch (error) {
            showText("dashboardMessage", error.message, true);
        }
    }

    loadDashboard();
});