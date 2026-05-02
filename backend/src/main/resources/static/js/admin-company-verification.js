document.addEventListener("DOMContentLoaded", () => {
    if (!requireRole("admin")) {
        return;
    }

    const statusFilter = document.getElementById("statusFilter");
    const loadBtn = document.getElementById("loadBtn");
    const companyVerificationBody = document.getElementById("companyVerificationBody");
    const messageBox = document.getElementById("messageBox");

    function showMessage(message, isError = false) {
        messageBox.textContent = message || "";
        messageBox.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function getStatusClass(status) {
        if (status === "approved") return "status-approved";
        if (status === "rejected") return "status-rejected";
        return "status-pending";
    }

    function formatText(value) {
        if (value === null || value === undefined || value === "") {
            return "-";
        }
        return value;
    }

    function formatLink(url) {
        if (!url) {
            return "-";
        }
        return `<a href="${url}" target="_blank">${url}</a>`;
    }

    function renderCompanies(items) {
        companyVerificationBody.innerHTML = "";

        if (!items || items.length === 0) {
            companyVerificationBody.innerHTML = `<tr><td colspan="7">No verification requests found</td></tr>`;
            return;
        }

        items.forEach((item) => {
            const tr = document.createElement("tr");
            const status = item.reviewStatus || "pending";
            const statusClass = getStatusClass(status);

            tr.innerHTML = `
                <td>${item.verificationRequestId}</td>
                <td>Employer #${item.employerProfileId}</td>
                <td>${formatLink(item.businessLicenseUrl)}</td>
                <td>${formatLink(item.supportingDocumentUrl)}</td>
                <td>${formatText(item.submittedAt)}</td>
                <td><span class="${statusClass}">${status}</span></td>
                <td>
                    <button class="action-btn" data-id="${item.verificationRequestId}" data-status="approved" ${status === "approved" ? "disabled" : ""}>Approve</button>
                    <button class="action-btn" data-id="${item.verificationRequestId}" data-status="rejected" ${status === "rejected" ? "disabled" : ""}>Reject</button>
                </td>
            `;

            companyVerificationBody.appendChild(tr);
        });

        companyVerificationBody.querySelectorAll(".action-btn").forEach((button) => {
            button.addEventListener("click", async () => {
                const requestId = button.dataset.id;
                const newStatus = button.dataset.status;

                try {
                    showMessage(`Updating request ${requestId}...`);
                    await apiPutForm(`/verification/admin/${requestId}/review`, {
                        reviewStatus: newStatus,
                        reviewNote: ""
                    });
                    showMessage(`Request ${requestId} updated to ${newStatus}.`);
                    await loadCompanies();
                } catch (error) {
                    showMessage(error.message, true);
                }
            });
        });
    }

    async function loadCompanies() {
        try {
            showMessage("Loading verification requests...");
            const selectedStatus = statusFilter.value.trim();
            const path = selectedStatus
                ? `/verification/admin?reviewStatus=${encodeURIComponent(selectedStatus)}`
                : "/verification/admin";

            const data = await apiGet(path);
            renderCompanies(data);
            showMessage("Verification requests loaded.");
        } catch (error) {
            renderCompanies([]);
            showMessage(error.message, true);
        }
    }

    loadBtn.addEventListener("click", loadCompanies);

    loadCompanies();
});