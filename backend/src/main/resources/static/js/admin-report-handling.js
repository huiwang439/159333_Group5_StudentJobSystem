const menuToggleBtn = document.getElementById("menuToggleBtn");
const sidebar = document.getElementById("sidebar");
const statusFilter = document.getElementById("statusFilter");
const loadBtn = document.getElementById("loadBtn");
const reportTableBody = document.getElementById("reportTableBody");
const messageBox = document.getElementById("messageBox");

let mockReports = [
    { id: 1, reportedUser: "Future Tech HR", reason: "Spam posting", relatedJob: "Frontend Intern", status: "pending" },
    { id: 2, reportedUser: "Blue Sky Ltd", reason: "Fake company info", relatedJob: "Data Analyst", status: "resolved" },
    { id: 3, reportedUser: "Vision Studio", reason: "Misleading salary", relatedJob: "UI Designer", status: "pending" }
];

function showMessage(message) {
    messageBox.textContent = message;
}

function getStatusClass(status) {
    return status === "resolved" ? "status-resolved" : "status-pending";
}

function getFilteredReports() {
    const status = statusFilter.value;
    if (!status) return mockReports;
    return mockReports.filter(report => report.status === status);
}

function renderReports(reports) {
    reportTableBody.innerHTML = "";

    if (!reports.length) {
        reportTableBody.innerHTML = `<tr><td colspan="6">No reports found</td></tr>`;
        return;
    }

    reports.forEach(report => {
        const tr = document.createElement("tr");
        const statusClass = getStatusClass(report.status);
        const isResolved = report.status === "resolved";

        tr.innerHTML = `
            <td>${report.id}</td>
            <td>${report.reportedUser}</td>
            <td>${report.reason}</td>
            <td>${report.relatedJob}</td>
            <td><span class="${statusClass}">${report.status}</span></td>
            <td>
                <button class="action-btn" onclick="handleReport(${report.id})" ${isResolved ? "disabled" : ""}>Resolve</button>
            </td>
        `;

        reportTableBody.appendChild(tr);
    });
}

function loadReports() {
    renderReports(getFilteredReports());
    showMessage("Report list loaded.");
}

function handleReport(reportId) {
    mockReports = mockReports.map(report => {
        if (report.id === reportId) {
            return { ...report, status: "resolved" };
        }
        return report;
    });
    loadReports();
    showMessage(`Report ${reportId} resolved.`);
}

loadBtn.addEventListener("click", loadReports);

loadReports();