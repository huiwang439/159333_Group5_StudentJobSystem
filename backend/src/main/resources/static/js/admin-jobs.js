const menuToggleBtn = document.getElementById("menuToggleBtn");
const sidebar = document.getElementById("sidebar");

const statusFilter = document.getElementById("statusFilter");
const loadJobsBtn = document.getElementById("loadJobsBtn");
const searchInput = document.getElementById("searchInput");
const searchBtn = document.getElementById("searchBtn");
const resetBtn = document.getElementById("resetBtn");
const jobTableBody = document.getElementById("jobTableBody");
const messageBox = document.getElementById("messageBox");

let mockJobs = [
    {
        id: 1,
        title: "Frontend Intern",
        company: "Future Tech",
        location: "Auckland",
        type: "Internship",
        salary: "$25/hour",
        deadline: "2026-04-20",
        status: "pending",
        createdAt: "2026-04-01 10:00:00"
    },
    {
        id: 2,
        title: "Backend Developer",
        company: "Green Company",
        location: "Wellington",
        type: "Full-time",
        salary: "$70,000 - $85,000/year",
        deadline: "2026-04-25",
        status: "approved",
        createdAt: "2026-04-02 11:30:00"
    },
    {
        id: 3,
        title: "Data Analyst",
        company: "Blue Sky Ltd",
        location: "Hamilton",
        type: "Part-time",
        salary: "$30/hour",
        deadline: "2026-04-28",
        status: "rejected",
        createdAt: "2026-04-03 09:45:00"
    },
    {
        id: 4,
        title: "UI Designer",
        company: "Vision Studio",
        location: "Christchurch",
        type: "Internship",
        salary: "$24/hour",
        deadline: "2026-05-05",
        status: "removed",
        createdAt: "2026-04-04 14:20:00"
    },
    {
        id: 5,
        title: "Software Engineer",
        company: "Next Wave",
        location: "Auckland",
        type: "Full-time",
        salary: "$80,000 - $95,000/year",
        deadline: "2026-05-10",
        status: "pending",
        createdAt: "2026-04-05 16:10:00"
    }
];

function showMessage(message) {
    messageBox.textContent = message;
}

function getStatusClass(status) {
    if (status === "pending") return "status-pending";
    if (status === "approved") return "status-approved";
    if (status === "rejected") return "status-rejected";
    return "status-removed";
}

function getFilteredJobs() {
    const status = statusFilter.value;
    const keyword = searchInput.value.trim().toLowerCase();

    let filteredJobs = mockJobs;

    if (status) {
        filteredJobs = filteredJobs.filter(job => job.status === status);
    }

    if (keyword) {
        filteredJobs = filteredJobs.filter(job => {
            const title = (job.title || "").toLowerCase();
            const company = (job.company || "").toLowerCase();
            return title.includes(keyword) || company.includes(keyword);
        });
    }

    return filteredJobs;
}

function renderJobs(jobs) {
    jobTableBody.innerHTML = "";

    if (!jobs || jobs.length === 0) {
        jobTableBody.innerHTML = `<tr><td colspan="10">No jobs found</td></tr>`;
        return;
    }

    jobs.forEach(job => {
        const tr = document.createElement("tr");
        const statusClass = getStatusClass(job.status);

        const isApproved = job.status === "approved";
        const isRejected = job.status === "rejected";
        const isRemoved = job.status === "removed";

        tr.innerHTML = `
            <td>${job.id ?? ""}</td>
            <td>${job.title ?? ""}</td>
            <td>${job.company ?? ""}</td>
            <td>${job.location ?? ""}</td>
            <td>${job.type ?? ""}</td>
            <td>${job.salary ?? ""}</td>
            <td>${job.deadline ?? ""}</td>
            <td><span class="${statusClass}">${job.status ?? ""}</span></td>
            <td>${job.createdAt ?? ""}</td>
            <td>
                <button
                    class="action-btn"
                    onclick="updateJobStatus(${job.id}, 'approved')"
                    ${isApproved || isRemoved ? "disabled" : ""}
                >
                    Approve
                </button>
                <button
                    class="action-btn"
                    onclick="updateJobStatus(${job.id}, 'rejected')"
                    ${isRejected || isRemoved ? "disabled" : ""}
                >
                    Reject
                </button>
                <button
                    class="action-btn"
                    onclick="updateJobStatus(${job.id}, 'removed')"
                    ${isRemoved ? "disabled" : ""}
                >
                    Remove
                </button>
            </td>
        `;

        jobTableBody.appendChild(tr);
    });
}

function loadJobs() {
    const filteredJobs = getFilteredJobs();
    renderJobs(filteredJobs);
    showMessage("Jobs loaded successfully.");
}

function updateJobStatus(jobId, newStatus) {
    mockJobs = mockJobs.map(job => {
        if (job.id === jobId) {
            return {
                ...job,
                status: newStatus
            };
        }
        return job;
    });

    loadJobs();
    showMessage(`Job ${jobId} status updated to ${newStatus}.`);
}

function resetFilters() {
    statusFilter.value = "";
    searchInput.value = "";
    loadJobs();
    showMessage("Filters reset.");
}

loadJobsBtn.addEventListener("click", loadJobs);
searchBtn.addEventListener("click", loadJobs);
resetBtn.addEventListener("click", resetFilters);

searchInput.addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
        loadJobs();
    }
});

loadJobs();