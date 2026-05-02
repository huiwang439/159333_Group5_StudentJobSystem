document.addEventListener("DOMContentLoaded", () => {
    if (!requireRole("admin")) return;

    const daysInput = document.getElementById("daysInput");
    const loadAnalyticsBtn = document.getElementById("loadAnalyticsBtn");

    const jobGrowthTrendBox = document.getElementById("jobGrowthTrendBox");
    const positionDistributionBox = document.getElementById("positionDistributionBox");
    const applicationTrendBox = document.getElementById("applicationTrendBox");
    const pendingReviewCountBox = document.getElementById("pendingReviewCountBox");
    const reportStatisticsBox = document.getElementById("reportStatisticsBox");
    const conversionRateBox = document.getElementById("conversionRateBox");
    const analyticsMessage = document.getElementById("analyticsMessage");

    function showMessage(message, isError = false) {
        analyticsMessage.textContent = message || "";
        analyticsMessage.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function showJson(element, data) {
        element.textContent = JSON.stringify(data, null, 2);
    }

    async function loadAnalytics() {
        try {
            const days = daysInput.value || 7;
            showMessage("Loading analytics...");

            const data = await apiGet(`/admin/analytics/overview?days=${days}`);

            showJson(jobGrowthTrendBox, data.jobGrowthTrend || []);
            showJson(positionDistributionBox, data.positionDistribution || []);
            showJson(applicationTrendBox, data.applicationTrend || []);
            showJson(pendingReviewCountBox, data.pendingReviewCount || {});
            showJson(reportStatisticsBox, data.reportStatistics || []);
            showJson(conversionRateBox, data.conversionRate || {});

            showMessage("Analytics loaded.");
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    loadAnalyticsBtn.addEventListener("click", loadAnalytics);
    loadAnalytics();
});