document.addEventListener("DOMContentLoaded", () => {
    if (!requireCareerStaff()) return;

    const daysInput = document.getElementById("daysInput");
    const loadAnalyticsBtn = document.getElementById("loadAnalyticsBtn");
    const analyticsMessage = document.getElementById("analyticsMessage");

    let jobGrowthTrendChart = null;
    let positionDistributionChart = null;
    let applicationTrendChart = null;
    let jobsAwaitingReviewChart = null;
    let applicationSuccessMetricsChart = null;

    function showMessage(message, isError = false) {
        analyticsMessage.textContent = message || "";
        analyticsMessage.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function destroyChart(chart) {
        if (chart) {
            chart.destroy();
        }
    }

    function normalizeList(data) {
        if (!data) return [];
        if (Array.isArray(data)) return data;
        if (Array.isArray(data.data)) return data.data;
        if (Array.isArray(data.items)) return data.items;
        return [];
    }

    function getLabel(item) {
        return item.date || item.name || item.status || item.label || item.type || "-";
    }

    function getValue(item) {
        return item.count ?? item.value ?? item.total ?? 0;
    }

    function createLineChart(canvasId, labels, values, label) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return null;

        return new Chart(canvas, {
            type: "line",
            data: {
                labels: labels,
                datasets: [
                    {
                        label: label,
                        data: values,
                        tension: 0.3,
                        fill: false
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        display: true
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        }
                    }
                }
            }
        });
    }

    function createBarChart(canvasId, labels, values, label) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return null;

        return new Chart(canvas, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [
                    {
                        label: label,
                        data: values
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        display: true
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        }
                    }
                }
            }
        });
    }

    function createPieChart(canvasId, labels, values, label) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return null;

        return new Chart(canvas, {
            type: "pie",
            data: {
                labels: labels,
                datasets: [
                    {
                        label: label,
                        data: values
                    }
                ]
            },
            options: {
                responsive: true
            }
        });
    }

    function clearOldCharts() {
        destroyChart(jobGrowthTrendChart);
        destroyChart(positionDistributionChart);
        destroyChart(applicationTrendChart);
        destroyChart(jobsAwaitingReviewChart);
        destroyChart(applicationSuccessMetricsChart);

        jobGrowthTrendChart = null;
        positionDistributionChart = null;
        applicationTrendChart = null;
        jobsAwaitingReviewChart = null;
        applicationSuccessMetricsChart = null;
    }

    function renderCharts(data) {
        clearOldCharts();

        const jobGrowthTrend = normalizeList(data.jobGrowthTrend);
        jobGrowthTrendChart = createLineChart(
            "jobGrowthTrendChart",
            jobGrowthTrend.map(getLabel),
            jobGrowthTrend.map(getValue),
            "Jobs"
        );

        const positionDistribution = normalizeList(data.positionDistribution);
        positionDistributionChart = createPieChart(
            "positionDistributionChart",
            positionDistribution.map(getLabel),
            positionDistribution.map(getValue),
            "Positions"
        );

        const applicationTrend = normalizeList(data.applicationTrend);
        applicationTrendChart = createLineChart(
            "applicationTrendChart",
            applicationTrend.map(getLabel),
            applicationTrend.map(getValue),
            "Applications"
        );

        const pendingReviewCount = data.pendingReviewCount || {};
        const pendingJobs =
            pendingReviewCount.pendingJobs ??
            pendingReviewCount.jobs ??
            pendingReviewCount.pendingJobCount ??
            0;

        jobsAwaitingReviewChart = createBarChart(
            "jobsAwaitingReviewChart",
            ["Pending Jobs"],
            [pendingJobs],
            "Jobs Awaiting Review"
        );

        const conversionRate = data.conversionRate || {};
        const totalApplications = conversionRate.totalApplications ?? 0;
        const acceptedApplications = conversionRate.acceptedApplications ?? 0;
        const acceptanceRate = conversionRate.acceptanceRate ?? 0;

        applicationSuccessMetricsChart = createBarChart(
            "applicationSuccessMetricsChart",
            ["Accepted Applications", "Total Applications", "Acceptance Rate"],
            [acceptedApplications, totalApplications, acceptanceRate],
            "Application Success Metrics"
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