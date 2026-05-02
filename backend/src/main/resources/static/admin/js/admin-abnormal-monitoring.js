document.addEventListener("DOMContentLoaded", () => {
    if (!requireRole("admin")) return;

    const levelFilter = document.getElementById("levelFilter");
    const loadBtn = document.getElementById("loadBtn");
    const abnormalTableBody = document.getElementById("abnormalTableBody");
    const messageBox = document.getElementById("messageBox");

    function showMessage(message, isError = false) {
        messageBox.textContent = message || "";
        messageBox.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function renderData(data) {
        abnormalTableBody.innerHTML = "";

        if (!data || data.length === 0) {
            abnormalTableBody.innerHTML = `<tr><td colspan="5">No abnormal records found</td></tr>`;
            return;
        }

        data.forEach(item => {
            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td>${item.userId ?? "-"}</td>
                <td>${item.fullName ?? "-"}</td>
                <td>${item.role ?? "-"}</td>
                <td>
                    Actions Today: ${item.actionCountToday ?? 0}<br>
                    Reports Today: ${item.reportCountToday ?? 0}<br>
                    Reasons: ${(item.reasons || []).join("; ") || "-"}
                </td>
                <td>${item.riskLevel ?? "-"}</td>
            `;

            abnormalTableBody.appendChild(tr);
        });
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

    loadBtn.addEventListener("click", loadData);
    loadData();
});