document.addEventListener("DOMContentLoaded", () => {
    if (!requireRole("admin")) return;

    const searchInput = document.getElementById("companysearchInput");
    const searchBtn = document.getElementById("companysearchBtn");
    const resetBtn = document.getElementById("companyresetBtn");
    const industryFilter = document.getElementById("industryFilter");
    const companyInfoBody = document.getElementById("companyInfoBody");
    const messageBox = document.getElementById("messageBox");

    function showMessage(message, isError = false) {
        messageBox.textContent = message || "";
        messageBox.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function buildPath() {
        const params = new URLSearchParams();
        if (industryFilter.value) params.append("industry", industryFilter.value);
        if (searchInput.value.trim()) params.append("keyword", searchInput.value.trim());
        return params.toString() ? `/admin/employers?${params}` : "/admin/employers";
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
                <td>${company.employerProfileId ?? "-"}</td>
                <td>${company.companyName ?? "-"}</td>
                <td>${company.industry ?? "-"}</td>
                <td>${company.companySize ?? "-"}</td>
                <td>${company.location ?? "-"}</td>
                <td>${company.contactPerson ?? "-"}</td>
                <td>${company.contactEmail ?? "-"}</td>
                <td>${company.verificationStatus ?? "-"}</td>
                <td>${company.accountStatus ?? "-"}</td>
                <td><button class="detail-btn" data-id="${company.userId}">View</button></td>
            `;

            companyInfoBody.appendChild(tr);
        });

        companyInfoBody.querySelectorAll(".detail-btn").forEach(btn => {
            btn.addEventListener("click", async () => {
                await loadCompanyDetail(btn.dataset.id);
            });
        });
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
            const detail = await apiGet(`/admin/employers/${userId}`);
            showMessage(JSON.stringify(detail, null, 2));
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
        if (e.key === "Enter") loadCompanies();
    });

    loadCompanies();
});