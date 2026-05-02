const menuToggleBtn = document.getElementById("menuToggleBtn");
const sidebar = document.getElementById("sidebar");
const statusFilter = document.getElementById("statusFilter");
const loadBtn = document.getElementById("loadBtn");
const companyVerificationBody = document.getElementById("companyVerificationBody");
const messageBox = document.getElementById("messageBox");

let mockVerificationCompanies = [
    { id: 1, name: "Future Tech", address: "Auckland", code: "FT001", size: "100-499", status: "pending" },
    { id: 2, name: "Green Company", address: "Wellington", code: "GC002", size: "50-99", status: "approved" },
    { id: 3, name: "Vision Studio", address: "Christchurch", code: "VS003", size: "10-49", status: "pending" },
    { id: 4, name: "Blue Sky Ltd", address: "Hamilton", code: "BS004", size: "500+", status: "rejected" }
];

function showMessage(message) {
    messageBox.textContent = message;
}

function getStatusClass(status) {
    if (status === "approved") return "status-approved";
    if (status === "rejected") return "status-rejected";
    return "status-pending";
}

function getFilteredCompanies() {
    const status = statusFilter.value;
    if (!status) return mockVerificationCompanies;
    return mockVerificationCompanies.filter(company => company.status === status);
}

function renderCompanies(companies) {
    companyVerificationBody.innerHTML = "";

    if (!companies.length) {
        companyVerificationBody.innerHTML = `<tr><td colspan="7">No companies found</td></tr>`;
        return;
    }

    companies.forEach(company => {
        const tr = document.createElement("tr");
        const statusClass = getStatusClass(company.status);

        const isApproved = company.status === "approved";
        const isRejected = company.status === "rejected";

        tr.innerHTML = `
            <td>${company.id}</td>
            <td>${company.name}</td>
            <td>${company.address}</td>
            <td>${company.code}</td>
            <td>${company.size}</td>
            <td><span class="${statusClass}">${company.status}</span></td>
            <td>
                <button class="action-btn" onclick="updateCompanyStatus(${company.id}, 'approved')" ${isApproved ? "disabled" : ""}>Approve</button>
                <button class="action-btn" onclick="updateCompanyStatus(${company.id}, 'rejected')" ${isRejected ? "disabled" : ""}>Reject</button>
            </td>
        `;

        companyVerificationBody.appendChild(tr);
    });
}

function loadCompanies() {
    renderCompanies(getFilteredCompanies());
    showMessage("Company verification list loaded.");
}

function updateCompanyStatus(companyId, newStatus) {
    mockVerificationCompanies = mockVerificationCompanies.map(company => {
        if (company.id === companyId) {
            return { ...company, status: newStatus };
        }
        return company;
    });
    loadCompanies();
    showMessage(`Company ${companyId} status updated to ${newStatus}.`);
}

loadBtn.addEventListener("click", loadCompanies);

loadCompanies();