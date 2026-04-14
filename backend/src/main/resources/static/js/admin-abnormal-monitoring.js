const menuToggleBtn = document.getElementById("menuToggleBtn");
const sidebar = document.getElementById("sidebar");
const levelFilter = document.getElementById("levelFilter");
const loadBtn = document.getElementById("loadBtn");
const abnormalTableBody = document.getElementById("abnormalTableBody");
const messageBox = document.getElementById("messageBox");

const mockAbnormalData = [
    { id: 1, object: "Future Tech", type: "Repeated Posts", description: "Same job posted 5 times in 1 hour", riskLevel: "warning" },
    { id: 2, object: "User 15529561667", type: "Frequent Login", description: "20 login attempts in 10 minutes", riskLevel: "high" },
    { id: 3, object: "Green Company", type: "Normal Activity", description: "No abnormal behavior detected", riskLevel: "normal" }
];

function showMessage(message) {
    messageBox.textContent = message;
}

function getStatusClass(level) {
    if (level === "high") return "status-high";
    if (level === "warning") return "status-warning";
    return "status-normal";
}

function getFilteredData() {
    const level = levelFilter.value;
    if (!level) return mockAbnormalData;
    return mockAbnormalData.filter(item => item.riskLevel === level);
}

function renderAbnormalData(data) {
    abnormalTableBody.innerHTML = "";

    if (!data.length) {
        abnormalTableBody.innerHTML = `<tr><td colspan="5">No abnormal records found</td></tr>`;
        return;
    }

    data.forEach(item => {
        const tr = document.createElement("tr");
        const statusClass = getStatusClass(item.riskLevel);

        tr.innerHTML = `
            <td>${item.id}</td>
            <td>${item.object}</td>
            <td>${item.type}</td>
            <td>${item.description}</td>
            <td><span class="${statusClass}">${item.riskLevel}</span></td>
        `;

        abnormalTableBody.appendChild(tr);
    });
}

function loadData() {
    renderAbnormalData(getFilteredData());
    showMessage("Abnormal monitoring data loaded.");
}

loadBtn.addEventListener("click", loadData);

loadData();