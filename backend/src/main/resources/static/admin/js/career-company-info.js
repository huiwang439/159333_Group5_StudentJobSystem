document.addEventListener("DOMContentLoaded", () => {
    if (!requireCareerStaff()) return;

    const searchInput = document.getElementById("companysearchInput");
    const searchBtn = document.getElementById("companysearchBtn");
    const resetBtn = document.getElementById("companyresetBtn");
    const industryFilter = document.getElementById("industryFilter");
    const companyInfoBody = document.getElementById("companyInfoBody");
    const messageBox = document.getElementById("messageBox");

    const companyListView = document.getElementById("companyListView");
    const companyDetailView = document.getElementById("companyDetailView");
    const companyDetailCard = document.getElementById("companyDetailCard");
    const backToCompaniesBtn = document.getElementById("backToCompaniesBtn");

    function showMessage(message, isError = false) {
        messageBox.textContent = message || "";
        messageBox.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function buildPath() {
        const params = new URLSearchParams();

        if (industryFilter.value) {
            params.append("industry", industryFilter.value);
        }

        if (searchInput.value.trim()) {
            params.append("keyword", searchInput.value.trim());
        }

        return params.toString() ? `/admin/employers?${params}` : "/admin/employers";
    }

    function formatValue(value) {
        if (value === null || value === undefined || value === "") return "-";
        return value;
    }

    function showListView() {
        companyDetailView.classList.add("hidden");
        companyListView.classList.remove("hidden");
        showMessage("");
    }

    function showDetailView() {
        companyListView.classList.add("hidden");
        companyDetailView.classList.remove("hidden");
        showMessage("");
    }

    function renderCompanies(companies) {
        companyInfoBody.innerHTML = "";

        if (!companies || companies.length === 0) {
            companyInfoBody.innerHTML = `<tr><td colspan="10">No companies found</td></tr>`;
            return;
        }

        companies.forEach(company => {
            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td>${formatValue(company.employerProfileId)}</td>
                <td>${formatValue(company.companyName)}</td>
                <td>${formatValue(company.industry)}</td>
                <td>${formatValue(company.companySize)}</td>
                <td>${formatValue(company.location)}</td>
                <td>${formatValue(company.contactPerson)}</td>
                <td>${formatValue(company.contactEmail)}</td>
                <td>${formatValue(company.verificationStatus)}</td>
                <td>${formatValue(company.accountStatus)}</td>
                <td>
                    <button class="detail-btn" data-id="${company.userId}">View</button>
                </td>
            `;

            companyInfoBody.appendChild(tr);
        });

        companyInfoBody.querySelectorAll(".detail-btn").forEach(btn => {
            btn.addEventListener("click", () => loadCompanyDetail(btn.dataset.id));
        });
    }

    function renderCompanyDetail(company) {
        companyDetailCard.innerHTML = `
            <div class="detail-grid">
                <div class="detail-item">
                    <span class="detail-label">Employer Profile ID</span>
                    <span class="detail-value">${formatValue(company.employerProfileId)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">User ID</span>
                    <span class="detail-value">${formatValue(company.userId)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Company Name</span>
                    <span class="detail-value">${formatValue(company.companyName)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Industry</span>
                    <span class="detail-value">${formatValue(company.industry)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Company Size</span>
                    <span class="detail-value">${formatValue(company.companySize)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Location</span>
                    <span class="detail-value">${formatValue(company.location)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Contact Person</span>
                    <span class="detail-value">${formatValue(company.contactPerson)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Contact Email</span>
                    <span class="detail-value">${formatValue(company.contactEmail)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Email</span>
                    <span class="detail-value">${formatValue(company.email)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Phone</span>
                    <span class="detail-value">${formatValue(company.phone)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Website</span>
                    <span class="detail-value">${formatValue(company.website)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Verification Status</span>
                    <span class="detail-value">${formatValue(company.verificationStatus)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Account Status</span>
                    <span class="detail-value">${formatValue(company.accountStatus)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Full Name</span>
                    <span class="detail-value">${formatValue(company.fullName)}</span>
                </div>

                <div class="detail-item full-width">
                    <span class="detail-label">Company Description</span>
                    <span class="detail-value">${formatValue(company.companyDescription)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Created At</span>
                    <span class="detail-value">${formatValue(company.createdAt)}</span>
                </div>

                <div class="detail-item">
                    <span class="detail-label">Updated At</span>
                    <span class="detail-value">${formatValue(company.updatedAt)}</span>
                </div>
            </div>
        `;
    }

    async function loadCompanies() {
        try {
            showMessage("Loading companies...");

            const companies = await apiGet(buildPath());

            renderCompanies(companies);
            showMessage("Companies loaded.");
        } catch (error) {
            renderCompanies([]);
            showMessage(error.message, true);
        }
    }

    async function loadCompanyDetail(userId) {
        try {
            showMessage("Loading company details...");

            const detail = await apiGet(`/admin/employers/${userId}`);

            renderCompanyDetail(detail);
            showDetailView();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    function resetFilters() {
        industryFilter.value = "";
        searchInput.value = "";
        loadCompanies();
    }

    searchBtn.addEventListener("click", loadCompanies);

    resetBtn.addEventListener("click", resetFilters);

    industryFilter.addEventListener("change", loadCompanies);

    searchInput.addEventListener("keydown", e => {
        if (e.key === "Enter") {
            loadCompanies();
        }
    });

    backToCompaniesBtn.addEventListener("click", showListView);

    loadCompanies();
});