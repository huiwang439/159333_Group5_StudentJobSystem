const menuToggleBtn = document.getElementById("menuToggleBtn");
const sidebar = document.getElementById("sidebar");
const searchInput = document.getElementById("companysearchInput");
const searchBtn = document.getElementById("companysearchBtn");
const resetBtn = document.getElementById("companyresetBtn");
const industryFilter = document.getElementById("industryFilter");
const companyInfoBody = document.getElementById("companyInfoBody");
const messageBox = document.getElementById("messageBox");

const mockCompanyInfo = [
    { id: 1, name: "Future Tech", address: "Auckland", industry: "Technology", status: "approved" },
    { id: 2, name: "Green Company", address: "Wellington", industry: "Environment", status: "approved" },
    { id: 3, name: "Vision Studio", address: "Christchurch", industry: "Design", status: "pending" },
    { id: 4, name: "Blue Sky Ltd", address: "Hamilton", industry: "Consulting", status: "rejected" }
];

function showMessage(message) {
    messageBox.textContent = message;
}

function getStatusClass(status) {
    if (status === "approved") return "status-approved";
    if (status === "rejected") return "status-rejected";
    return "status-pending";
}

function getFilteredCompanyInfo() {
    const keyword = searchInput.value.trim().toLowerCase();
    const industry = industryFilter.value;

    let filteredCompanies = mockCompanyInfo;

    if (industry) {
        filteredCompanies = filteredCompanies.filter(company => company.industry === industry);
    }

    if (keyword) {
        filteredCompanies = filteredCompanies.filter(company =>
            company.name.toLowerCase().includes(keyword)
        );
    }

    return filteredCompanies;
}

function renderCompanyInfo(companies) {
    companyInfoBody.innerHTML = "";

    if (!companies.length) {
        companyInfoBody.innerHTML = `<tr><td colspan="5">No companies found</td></tr>`;
        return;
    }

    companies.forEach(company => {
        const tr = document.createElement("tr");
        const statusClass = getStatusClass(company.status);

        tr.innerHTML = `
            <td>${company.id}</td>
            <td>${company.name}</td>
            <td>${company.address}</td>
            <td>${company.industry}</td>
            <td><span class="${statusClass}">${company.status}</span></td>
        `;

        companyInfoBody.appendChild(tr);
    });
}

function loadCompanyInfo() {
    renderCompanyInfo(getFilteredCompanyInfo());
    showMessage("Company information loaded.");
}

function resetFilters() {
    searchInput.value = "";
    industryFilter.value = "";
    loadCompanyInfo();
    showMessage("Filters reset.");
}

searchBtn.addEventListener("click", loadCompanyInfo);
resetBtn.addEventListener("click", resetFilters);

searchInput.addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
        loadCompanyInfo();
    }
});

loadCompanyInfo();