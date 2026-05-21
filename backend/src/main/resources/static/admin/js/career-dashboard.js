document.addEventListener("DOMContentLoaded", async () => {
    if (!requireCareerStaff()) return;

    function setText(id, value) {
        const el = document.getElementById(id);
        if (el) el.textContent = value ?? 0;
    }

    try {
        showText("dashboardMessage", "Loading dashboard...");

        const summary = await apiGet("/admin/dashboard");
        const jobs = await apiGet("/admin/dashboard/jobs");

        setText("activeStudents", summary.totalStudents);
        setText("participatingEmployers", summary.totalEmployers);
        setText("totalJobs", summary.totalJobs);
        setText("totalApplications", summary.totalApplications);
        setText("pendingJobs", jobs.pendingJobs);
        setText("approvedJobs", jobs.approvedJobs);

        showText("dashboardMessage", "Dashboard loaded.");
    } catch (error) {
        showText("dashboardMessage", error.message, true);
    }
});