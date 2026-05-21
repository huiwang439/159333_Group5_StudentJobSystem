document.addEventListener("DOMContentLoaded", () => {
    if (!requireRole("admin")) return;

    const daysInput = document.getElementById("daysInput");
    const loadAnalyticsBtn = document.getElementById("loadAnalyticsBtn");
    const analyticsMessage = document.getElementById("analyticsMessage");

    let jobGrowthTrendChart = null;
    let positionDistributionChart = null;
    let applicationTrendChart = null;
    let pendingReviewCountChart = null;
    let reportStatisticsChart = null;
    let conversionRateChart = null;

    function showMessage(message, isError = false) {
        analyticsMessage.textContent = message || "";
        analyticsMessage.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function destroyChart(chart) {
        if (chart) chart.destroy();
    }

    function createLineChart(canvasId, labels, values, label) {
        const ctx = document.getElementById(canvasId);

        return new Chart(ctx, {
            type: "line",
            data: {
                labels,
                datasets: [{
                    label,
                    data: values,
                    tension: 0.3,
                    fill: false
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: true } }
            }
        });
    }

    function createBarChart(canvasId, labels, values, label) {
        const ctx = document.getElementById(canvasId);

        return new Chart(ctx, {
            type: "bar",
            data: {
                labels,
                datasets: [{
                    label,
                    data: values
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: true } }
            }
        });
    }

    function createPieChart(canvasId, labels, values, label) {
        const ctx = document.getElementById(canvasId);

        return new Chart(ctx, {
            type: "pie",
            data: {
                labels,
                datasets: [{
                    label,
                    data: values
                }]
            },
            options: {
                responsive: true
            }
        });
    }

    function renderCharts(overviewData, trendData, distributionData) {
        destroyChart(jobGrowthTrendChart);
        destroyChart(positionDistributionChart);
        destroyChart(applicationTrendChart);
        destroyChart(pendingReviewCountChart);
        destroyChart(reportStatisticsChart);
        destroyChart(conversionRateChart);

        const jobGrowthTrend = overviewData.jobGrowthTrend || [];

        jobGrowthTrendChart = createLineChart(
            "jobGrowthTrendChart",
            jobGrowthTrend.map(item => item.date),
            jobGrowthTrend.map(item => item.count),
            "Jobs"
        );

        const userRoleDistribution = distributionData.userRoleDistribution || {};
        positionDistributionChart = createPieChart(
            "positionDistributionChart",
            Object.keys(userRoleDistribution),
            Object.values(userRoleDistribution),
            "User Role Distribution"
        );

        applicationTrendChart = createLineChart(
            "applicationTrendChart",
            trendData.map(item => item.date),
            trendData.map(item => item.count),
            "Activity Trend"
        );

        const pendingReviewCount = overviewData.pendingReviewCount || {};
        pendingReviewCountChart = createBarChart(
            "pendingReviewCountChart",
            Object.keys(pendingReviewCount),
            Object.values(pendingReviewCount),
            "Pending Review"
        );

        const reportStatistics = overviewData.reportStatistics || [];
        reportStatisticsChart = createBarChart(
            "reportStatisticsChart",
            reportStatistics.map(item => item.name),
            reportStatistics.map(item => item.value),
            "Reports"
        );

        const jobStatusDistribution = distributionData.jobStatusDistribution || {};
        conversionRateChart = createBarChart(
            "conversionRateChart",
            Object.keys(jobStatusDistribution),
            Object.values(jobStatusDistribution),
            "Job Status Distribution"
        );
    }

    async function loadAnalytics() {
        try {
            const days = daysInput.value || 7;
            showMessage("Loading analytics...");

            const [overviewData, trendData, distributionData] = await Promise.all([
                apiGet(`/admin/analytics/overview?days=${days}`),
                apiGet(`/admin/analytics/trend?days=${days}`),
                apiGet("/admin/analytics/distribution")
            ]);

            renderCharts(overviewData, trendData, distributionData);

            showMessage("Analytics loaded.");
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    loadAnalyticsBtn.addEventListener("click", loadAnalytics);
    loadAnalytics();
});