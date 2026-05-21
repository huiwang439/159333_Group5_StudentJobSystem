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
            abnormalTableBody.innerHTML = `<tr><td colspan="6">No abnormal records found</td></tr>`;
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
                <td>
                    <button class="detail-btn" data-id="${item.userId}">View Detail</button>
                </td>
            `;

            abnormalTableBody.appendChild(tr);
        });

        abnormalTableBody.querySelectorAll(".detail-btn").forEach(btn => {
            btn.addEventListener("click", () => loadDetail(btn.dataset.id));
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

    async function loadDetail(userId) {
        try {
            const detail = await apiGet(`/admin/abnormal/${userId}`);

            const logs = detail.logs || [];

            alert(
                `User ID: ${detail.userId ?? "-"}\n` +
                `Name: ${detail.fullName ?? "-"}\n` +
                `Role: ${detail.role ?? "-"}\n` +
                `Risk Level: ${detail.riskLevel ?? "-"}\n` +
                `Actions Today: ${detail.actionCountToday ?? 0}\n` +
                `Reports Today: ${detail.reportCountToday ?? 0}\n` +
                `Reports Made Today: ${detail.reportsMadeToday ?? 0}\n` +
                `Reasons: ${(detail.reasons || []).join("; ") || "-"}\n\n` +
                `Log Count: ${logs.length}`
            );
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    loadBtn.addEventListener("click", loadData);
    loadData();
});