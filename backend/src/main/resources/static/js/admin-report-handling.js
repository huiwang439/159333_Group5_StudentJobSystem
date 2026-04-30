document.addEventListener("DOMContentLoaded", () => {
    if (!requireRole("admin")) {
        return;
    }

    const statusFilter = document.getElementById("statusFilter");
    const loadBtn = document.getElementById("loadBtn");
    const reportTableBody = document.getElementById("reportTableBody");
    const messageBox = document.getElementById("messageBox");

    function showMessage(message, isError = false) {
        messageBox.textContent = message || "";
        messageBox.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function getStatusClass(status) {
        if (status === "resolved") return "status-resolved";
        if (status === "rejected") return "status-rejected";
        if (status === "reviewed") return "status-reviewed";
        return "status-pending";
    }

    function renderReports(reports) {
        reportTableBody.innerHTML = "";

        if (!reports || reports.length === 0) {
            reportTableBody.innerHTML = `<tr><td colspan="6">No reports found</td></tr>`;
            return;
        }

        reports.forEach((report) => {
            const tr = document.createElement("tr");
            const status = report.reportStatus || "pending";
            const statusClass = getStatusClass(status);

            tr.innerHTML = `
                <td>${report.reportId}</td>
                <td>User #${report.reportedUserId}</td>
                <td>${report.reportReason || "-"}</td>
                <td>${report.relatedJobId ? `Job #${report.relatedJobId}` : "-"}</td>
                <td><span class="${statusClass}">${status}</span></td>
                <td>
                    <button class="action-btn" data-id="${report.reportId}" data-status="resolved" ${status === "resolved" ? "disabled" : ""}>Resolve</button>
                </td>
            `;

            reportTableBody.appendChild(tr);
        });

        reportTableBody.querySelectorAll(".action-btn").forEach((button) => {
            button.addEventListener("click", async () => {
                const reportId = button.dataset.id;
                const newStatus = button.dataset.status;

                try {
                    showMessage(`Updating report ${reportId}...`);
                    await apiPutForm(`/reports/admin/${reportId}/handle`, {
                        reportStatus: newStatus
                    });
                    showMessage(`Report ${reportId} updated to ${newStatus}.`);
                    await loadReports();
                } catch (error) {
                    showMessage(error.message, true);
                }
            });
        });
    }

    async function loadReports() {
        try {
            showMessage("Loading reports...");
            const selectedStatus = statusFilter.value.trim();
            const path = selectedStatus
                ? `/reports/admin?reportStatus=${encodeURIComponent(selectedStatus)}`
                : "/reports/admin";

            const data = await apiGet(path);
            renderReports(data);
            showMessage("Reports loaded.");
        } catch (error) {
            renderReports([]);
            showMessage(error.message, true);
        }
    }

    loadBtn.addEventListener("click", loadReports);

    loadReports();
});