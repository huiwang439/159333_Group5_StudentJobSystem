document.addEventListener("DOMContentLoaded", () => {
    if (!requireRole("admin")) return;

    const statusFilter = document.getElementById("statusFilter");
    const loadBtn = document.getElementById("loadBtn");
    const reportTableBody = document.getElementById("reportTableBody");
    const messageBox = document.getElementById("messageBox");

    function showMessage(message, isError = false) {
        messageBox.textContent = message || "";
        messageBox.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function renderReports(reports) {
        reportTableBody.innerHTML = "";

        if (!reports || reports.length === 0) {
            reportTableBody.innerHTML = `<tr><td colspan="6">No reports found</td></tr>`;
            return;
        }

        reports.forEach(report => {
            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td>${report.reportId}</td>
                <td>
                    ${report.reportedUserName ?? `User #${report.reportedUserId}`}<br>
                    <small>Reporter: ${report.reporterName ?? `User #${report.reporterUserId}`}</small>
                </td>
                <td>${report.reportReason ?? "-"}</td>
                <td>${report.relatedJobTitle ?? (report.relatedJobId ? `Job #${report.relatedJobId}` : "-")}</td>
                <td>${report.reportStatus ?? "-"}</td>
                <td>
                    <button class="resolve-btn" data-id="${report.reportId}">Resolve</button>
                    <button class="reject-btn" data-id="${report.reportId}">Reject</button>
                </td>
            `;

            reportTableBody.appendChild(tr);
        });

        reportTableBody.querySelectorAll(".resolve-btn").forEach(btn => {
            btn.addEventListener("click", () => handleReport(btn.dataset.id, "resolved"));
        });

        reportTableBody.querySelectorAll(".reject-btn").forEach(btn => {
            btn.addEventListener("click", () => handleReport(btn.dataset.id, "rejected"));
        });
    }

    async function loadReports() {
        try {
            const status = statusFilter.value;
            const path = status ? `/reports/admin?status=${encodeURIComponent(status)}` : "/reports/admin";
            const data = await apiGet(path);
            renderReports(data);
            showMessage("Reports loaded.");
        } catch (error) {
            renderReports([]);
            showMessage(error.message, true);
        }
    }

    async function handleReport(id, status) {
        try {
            await apiPatchForm(`/reports/admin/${id}/handle`, { status: status });
            showMessage(`Report ${id} updated to ${status}.`);
            await loadReports();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    loadBtn.addEventListener("click", loadReports);
    loadReports();
});