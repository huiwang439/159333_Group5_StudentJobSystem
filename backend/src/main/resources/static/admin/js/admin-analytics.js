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
        if (chart) {
            chart.destroy();
        }
    }

    function createLineChart(canvasId, labels, values, label) {
        const ctx = document.getElementById(canvasId);

        return new Chart(ctx, {
            type: "line",
            data: {
                labels: labels,
                datasets: [{
                    label: label,
                    data: values,
                    tension: 0.3,
                    fill: false
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        display: true
                    }
                }
            }
        });
    }

    function createBarChart(canvasId, labels, values, label) {
        const ctx = document.getElementById(canvasId);

        return new Chart(ctx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [{
                    label: label,
                    data: values
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        display: true
                    }
                }
            }
        });
    }

    function createPieChart(canvasId, labels, values, label) {
        const ctx = document.getElementById(canvasId);

        return new Chart(ctx, {
            type: "pie",
            data: {
                labels: labels,
                datasets: [{
                    label: label,
                    data: values
                }]
            },
            options: {
                responsive: true
            }
        });
    }

    function renderCharts(data) {
        destroyChart(jobGrowthTrendChart);
        destroyChart(positionDistributionChart);
        destroyChart(applicationTrendChart);
        destroyChart(pendingReviewCountChart);
        destroyChart(reportStatisticsChart);
        destroyChart(conversionRateChart);

        const jobGrowthTrend = data.jobGrowthTrend || [];
        jobGrowthTrendChart = createLineChart(
            "jobGrowthTrendChart",
            jobGrowthTrend.map(item => item.date),
            jobGrowthTrend.map(item => item.count),
            "Jobs"
        );

        const positionDistribution = data.positionDistribution || [];
        positionDistributionChart = createPieChart(
            "positionDistributionChart",
            positionDistribution.map(item => item.name),
            positionDistribution.map(item => item.value),
            "Positions"
        );

        const applicationTrend = data.applicationTrend || [];
        applicationTrendChart = createLineChart(
            "applicationTrendChart",
            applicationTrend.map(item => item.date),
            applicationTrend.map(item => item.count),
            "Applications"
        );

        const pendingReviewCount = data.pendingReviewCount || {};
        pendingReviewCountChart = createBarChart(
            "pendingReviewCountChart",
            Object.keys(pendingReviewCount),
            Object.values(pendingReviewCount),
            "Pending Review"
        );

        const reportStatistics = data.reportStatistics || [];
        reportStatisticsChart = createBarChart(
            "reportStatisticsChart",
            reportStatistics.map(item => item.name),
            reportStatistics.map(item => item.value),
            "Reports"
        );

        const conversionRate = data.conversionRate || {};
        conversionRateChart = createBarChart(
            "conversionRateChart",
            Object.keys(conversionRate),
            Object.values(conversionRate),
            "Conversion Rate"
        );
    }

    async function loadAnalytics() {
        try {
            const days = daysInput.value || 7;
            showMessage("Loading analytics...");

            const data = await apiGet(`/admin/analytics/overview?days=${days}`);

            renderCharts(data);

            showMessage("Analytics loaded.");
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    loadAnalyticsBtn.addEventListener("click", loadAnalytics);
    loadAnalytics();
});