document.addEventListener("DOMContentLoaded", () => {
    if (!requireRole("admin")) return;

    const statusFilter = document.getElementById("statusFilter");
    const loadBtn = document.getElementById("loadBtn");
    const companyVerificationBody = document.getElementById("companyVerificationBody");
    const messageBox = document.getElementById("messageBox");

    function showMessage(message, isError = false) {
        messageBox.textContent = message || "";
        messageBox.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function renderCompanies(items) {
        companyVerificationBody.innerHTML = "";

        if (!items || items.length === 0) {
            companyVerificationBody.innerHTML = `<tr><td colspan="8">No verification requests found</td></tr>`;
            return;
        }

        items.forEach(item => {
            const tr = document.createElement("tr");
            const reviewNote = item.reviewNote && item.reviewNote.trim() !== "" ? item.reviewNote : "";

            tr.innerHTML = `
                <td>${item.verificationRequestId}</td>
                <td>
                    ${item.companyName ?? `Employer #${item.employerProfileId}`}<br>
                    <small>${item.industry ?? "-"} | ${item.location ?? "-"}</small><br>
                    <small>${item.contactPerson ?? "-"} / ${item.contactEmail ?? "-"}</small>
                </td>
                <td>${item.businessLicenseUrl ? `<a href="${item.businessLicenseUrl}" target="_blank">View</a>` : "-"}</td>
                <td>${item.supportingDocumentUrl ? `<a href="${item.supportingDocumentUrl}" target="_blank">View</a>` : "-"}</td>
                <td>${item.submittedAt ?? "-"}</td>
                <td>${item.reviewStatus ?? "-"}</td>
                <td>
                    ${reviewNote ? `<button class="note-btn" data-note="${encodeURIComponent(reviewNote)}">View Note</button>` : "-"}
                </td>
                <td>
                    <button class="review-btn" data-id="${item.verificationRequestId}">Review</button>
                    <button class="approve-btn" data-id="${item.verificationRequestId}">Approve</button>
                    <button class="reject-btn" data-id="${item.verificationRequestId}">Reject</button>
                </td>
            `;

            companyVerificationBody.appendChild(tr);
        });

        companyVerificationBody.querySelectorAll(".review-btn").forEach(btn => {
            btn.addEventListener("click", () => review(btn.dataset.id, "reviewed"));
        });

        companyVerificationBody.querySelectorAll(".approve-btn").forEach(btn => {
            btn.addEventListener("click", () => review(btn.dataset.id, "approved"));
        });

        companyVerificationBody.querySelectorAll(".reject-btn").forEach(btn => {
            btn.addEventListener("click", () => review(btn.dataset.id, "rejected"));
        });

        companyVerificationBody.querySelectorAll(".note-btn").forEach(btn => {
            btn.addEventListener("click", () => {
                alert(decodeURIComponent(btn.dataset.note));
            });
        });
    }

    async function loadCompanies() {
        try {
            const status = statusFilter.value;
            const path = status ? `/verification/admin?reviewStatus=${status}` : "/verification/admin";
            const data = await apiGet(path);
            renderCompanies(data);
            showMessage("Verification requests loaded.");
        } catch (error) {
            renderCompanies([]);
            showMessage(error.message, true);
        }
    }

    async function review(id, status) {
        try {
            const note = prompt("Review note:", "") || "";
            await apiPutForm(`/verification/admin/${id}/review`, {
                reviewStatus: status,
                reviewNote: note
            });
            showMessage(`Request ${id} updated to ${status}.`);
            await loadCompanies();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    loadBtn.addEventListener("click", loadCompanies);
    loadCompanies();
});