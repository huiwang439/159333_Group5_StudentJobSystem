document.addEventListener("DOMContentLoaded", () => {
    if (!requireRole("admin")) return;

    const levelFilter = document.getElementById("levelFilter");
    const loadBtn = document.getElementById("loadBtn");
    const abnormalTableBody = document.getElementById("abnormalTableBody");
    const messageBox = document.getElementById("messageBox");

    const abnormalListView = document.getElementById("abnormalListView");
    const abnormalDetailView = document.getElementById("abnormalDetailView");
    const abnormalDetailCard = document.getElementById("abnormalDetailCard");
    const backToAbnormalBtn = document.getElementById("backToAbnormalBtn");

    function showMessage(message, isError = false) {
        messageBox.textContent = message || "";
        messageBox.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function escapeHtml(value) {
        if (value === null || value === undefined || value === "") return "-";
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function showListView() {
        abnormalDetailView.classList.add("hidden");
        abnormalListView.classList.remove("hidden");
        showMessage("");
    }

    function showDetailView() {
        abnormalListView.classList.add("hidden");
        abnormalDetailView.classList.remove("hidden");
        showMessage("");
    }

    function renderData(data) {
        abnormalTableBody.innerHTML = "";

        if (!data || data.length === 0) {
            abnormalTableBody.innerHTML = `<tr><td colspan="6">No abnormal records found</td></tr>`;
            return;
        }

        data.forEach(item => {
            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td>${escapeHtml(item.userId)}</td>
                <td>${escapeHtml(item.fullName)}</td>
                <td>${escapeHtml(item.role)}</td>
                <td>
                    Actions Today: ${escapeHtml(item.actionCountToday ?? 0)}<br>
                    Reports Today: ${escapeHtml(item.reportCountToday ?? 0)}<br>
                    Reasons: ${escapeHtml((item.reasons || []).join("; ") || "-")}
                </td>
                <td>${escapeHtml(item.riskLevel)}</td>
                <td>
                    <button class="detail-btn" data-id="${escapeHtml(item.userId)}">View Detail</button>
                </td>
            `;

            abnormalTableBody.appendChild(tr);
        });

        abnormalTableBody.querySelectorAll(".detail-btn").forEach(btn => {
            btn.addEventListener("click", () => loadDetail(btn.dataset.id));
        });
    }

    function renderDetail(detail) {
        const logs = detail.logs || [];

        abnormalDetailCard.innerHTML = `
            <div class="detail-grid">
                <div class="detail-item">
                    <span class="detail-label">User ID</span>
                    <span class="detail-value">${escapeHtml(detail.userId)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Name</span>
                    <span class="detail-value">${escapeHtml(detail.fullName)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Role</span>
                    <span class="detail-value">${escapeHtml(detail.role)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Risk Level</span>
                    <span class="detail-value">${escapeHtml(detail.riskLevel)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Actions Today</span>
                    <span class="detail-value">${escapeHtml(detail.actionCountToday ?? 0)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Reports Today</span>
                    <span class="detail-value">${escapeHtml(detail.reportCountToday ?? 0)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Reports Made Today</span>
                    <span class="detail-value">${escapeHtml(detail.reportsMadeToday ?? 0)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Log Count</span>
                    <span class="detail-value">${escapeHtml(logs.length)}</span>
                </div>

                <div class="detail-item full-width">
                    <span class="detail-label">Reasons</span>
                    <span class="detail-value">${escapeHtml((detail.reasons || []).join("; ") || "-")}</span>
                </div>

                <div class="detail-item full-width">
                    <span class="detail-label">Recent Logs</span>
                    <span class="detail-value">${escapeHtml(
                        logs.length
                            ? logs.map(log =>
                                `${log.actionTime || "-"} | ${log.actionType || "-"} | ${log.description || "-"}`
                            ).join("\n")
                            : "-"
                    )}</span>
                </div>
            </div>
        `;
    }

    async function loadData() {
        try {
            const risk = levelFilter.value;
            const path = risk ? `/admin/abnormal?riskLevel=${risk}` : "/admin/abnormal";
            const data = await apiGet(path);
            renderData(data);
            showMessage("Abnormal monitoring data loaded.");
        } catch (error) {
            renderData([]);
            showMessage(error.message, true);
        }
    }

    async function loadDetail(userId) {
        try {
            const detail = await apiGet(`/admin/abnormal/${userId}`);
            renderDetail(detail);
            showDetailView();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    loadBtn.addEventListener("click", loadData);
    backToAbnormalBtn.addEventListener("click", showListView);

    loadData();
});