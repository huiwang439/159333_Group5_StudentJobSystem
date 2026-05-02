document.addEventListener("DOMContentLoaded", () => {
    if (!requireRole("admin")) return;

    const keywordInput = document.getElementById("keyword");
    const statusFilter = document.getElementById("statusFilter");
    const loadJobsBtn = document.getElementById("loadJobsBtn");
    const jobsTableBody = document.getElementById("jobsTableBody");
    const jobDetailsBox = document.getElementById("jobDetailsBox");
    const jobsMessage = document.getElementById("jobsMessage");

    function showMessage(message, isError = false) {
        jobsMessage.textContent = message || "";
        jobsMessage.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function buildPath() {
        const params = new URLSearchParams();
        if (statusFilter.value) params.append("status", statusFilter.value);
        if (keywordInput.value.trim()) params.append("keyword", keywordInput.value.trim());
        return params.toString() ? `/admin/jobs?${params}` : "/admin/jobs";
    }

    function formatSalary(min, max) {
        if (min == null && max == null) return "-";
        if (min != null && max != null) return `${min} - ${max}`;
        return `${min ?? max}`;
    }

    function renderJobs(jobs) {
        jobsTableBody.innerHTML = "";

        if (!jobs || jobs.length === 0) {
            jobsTableBody.innerHTML = `<tr><td colspan="10">No jobs found</td></tr>`;
            return;
        }

        jobs.forEach(job => {
            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td>${job.jobId ?? "-"}</td>
                <td>${job.title ?? "-"}</td>
                <td>${job.companyName ?? "-"}</td>
                <td>${job.categoryName ?? "-"}</td>
                <td>${job.location ?? "-"}</td>
                <td>${job.employmentType ?? "-"}</td>
                <td>${formatSalary(job.salaryMin, job.salaryMax)}</td>
                <td>${job.deadline ?? "-"}</td>
                <td>${job.status ?? "-"}</td>
                <td>
                    <button class="view-btn" data-id="${job.jobId}">View</button>
                    <button class="approve-btn" data-id="${job.jobId}">Approve</button>
                    <button class="reject-btn" data-id="${job.jobId}">Reject</button>
                    <button class="remove-btn" data-id="${job.jobId}">Remove</button>
                </td>
            `;

            jobsTableBody.appendChild(tr);
        });

        jobsTableBody.querySelectorAll(".view-btn").forEach(btn => {
            btn.addEventListener("click", () => loadJobDetails(btn.dataset.id));
        });

        jobsTableBody.querySelectorAll(".approve-btn").forEach(btn => {
            btn.addEventListener("click", () => updateJobStatus(btn.dataset.id, "approved"));
        });

        jobsTableBody.querySelectorAll(".reject-btn").forEach(btn => {
            btn.addEventListener("click", () => updateJobStatus(btn.dataset.id, "rejected"));
        });

        jobsTableBody.querySelectorAll(".remove-btn").forEach(btn => {
            btn.addEventListener("click", () => removeJob(btn.dataset.id));
        });
    }

    async function loadJobs() {
        try {
            showMessage("Loading jobs...");
            const jobs = await apiGet(buildPath());
            renderJobs(jobs);
            showMessage("Jobs loaded.");
        } catch (error) {
            renderJobs([]);
            showMessage(error.message, true);
        }
    }

    async function loadJobDetails(jobId) {
        try {
            const detail = await apiGet(`/admin/jobs/${jobId}`);
            jobDetailsBox.textContent = JSON.stringify(detail, null, 2);
            showMessage(`Job ${jobId} details loaded.`);
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    async function updateJobStatus(jobId, status) {
        try {
            const reason = status === "rejected" ? prompt("Reason for rejection:", "") || "" : "";
            await apiPatch(`/admin/jobs/${jobId}/status`, { status, reason });
            showMessage(`Job ${jobId} updated to ${status}.`);
            await loadJobs();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    async function removeJob(jobId) {
        if (!confirm(`Remove job ${jobId}?`)) return;

        try {
            await apiDelete(`/admin/jobs/${jobId}`);
            showMessage(`Job ${jobId} removed.`);
            await loadJobs();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    loadJobsBtn.addEventListener("click", loadJobs);
    keywordInput.addEventListener("keydown", e => {
        if (e.key === "Enter") loadJobs();
    });

    loadJobs();
});