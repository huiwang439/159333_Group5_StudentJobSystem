let allJobs = [];
let currentFilters = {
    keyword: "",
    location: "",
    employmentType: "",
    fieldOfStudy: "",
    companyName: ""
};

let selectedJobId = null;

const cities = [
    "Auckland",
    "Wellington",
    "Christchurch",
    "Hamilton",
    "Dunedin",
    "Tauranga"
];

const categories = [
    {
        title: "IT / Software",
        sub: "Software, web, app development",
        fieldOfStudy: "IT"
    },
    {
        title: "Data / Analytics",
        sub: "Data analyst, BI, reporting",
        fieldOfStudy: "Data"
    },
    {
        title: "Business / Marketing",
        sub: "Marketing, sales, business support",
        fieldOfStudy: "Business"
    },
    {
        title: "Hospitality / Retail",
        sub: "Part-time and customer service",
        fieldOfStudy: "Hospitality"
    },
    {
        title: "Administration",
        sub: "Office, assistant, HR support",
        fieldOfStudy: "Administration"
    },
    {
        title: "Engineering",
        sub: "Engineering internships and graduate roles",
        fieldOfStudy: "Engineering"
    }
];

const fallbackEmployers = [
    {
        name: "Massey University",
        industry: "Education"
    },
    {
        name: "Auckland Tech Ltd",
        industry: "Technology"
    },
    {
        name: "DataWorks NZ",
        industry: "Data & Analytics"
    },
    {
        name: "Campus Cafe",
        industry: "Hospitality"
    },
    {
        name: "NZ Retail Group",
        industry: "Retail"
    }
];

const cityList = document.getElementById("cityList");
const categoryList = document.getElementById("categoryList");
const jobGrid = document.getElementById("jobGrid");
const employerGrid = document.getElementById("employerGrid");
const emptyState = document.getElementById("emptyState");
const keywordInput = document.getElementById("keywordInput");
const searchBtn = document.getElementById("searchBtn");
const clearFiltersBtn = document.getElementById("clearFiltersBtn");
const filterSummary = document.getElementById("filterSummary");
const jobCountText = document.getElementById("jobCountText");

const jobDetailModal = document.getElementById("jobDetailModal");
const closeDetailBtn = document.getElementById("closeDetailBtn");
const applyNowBtn = document.getElementById("applyNowBtn");

function showToast(message) {
    let toast = document.getElementById("toast");

    if (!toast) {
        toast = document.createElement("div");
        toast.id = "toast";
        toast.className = "toast";
        document.body.appendChild(toast);
    }

    toast.textContent = message;
    toast.classList.remove("hidden");

    setTimeout(() => {
        toast.classList.add("hidden");
    }, 2500);
}

function formatValue(value) {
    return value === null || value === undefined || value === "" ? "-" : value;
}

function formatDate(value) {
    if (!value) return "-";
    return String(value).substring(0, 10);
}

function formatSalary(min, max) {
    const hasMin = min !== null && min !== undefined && min !== "";
    const hasMax = max !== null && max !== undefined && max !== "";

    if (hasMin && hasMax) return `$${min} - $${max}`;
    if (hasMin) return `$${min}+`;
    if (hasMax) return `Up to $${max}`;
    return "Salary not specified";
}

function getEmployerProfile(job) {
    return job?.employerProfile || {};
}

function getCompanyName(job) {
    const profile = getEmployerProfile(job);
    return profile.companyName || "Company not provided";
}

function getDisplayStatus(status) {
    if (status === "approved") return "active";
    return status || "active";
}
function shortText(text, maxLength = 120) {
    if (!text) return "No description provided.";
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + "...";
}

function getJobId(job) {
    return job.jobId ?? job.id;
}

function buildJobsUrl() {
    const params = new URLSearchParams();

    if (currentFilters.keyword) {
        params.append("keyword", currentFilters.keyword);
    }

    if (currentFilters.location) {
        params.append("location", currentFilters.location);
    }

    if (currentFilters.employmentType) {
        params.append("employmentType", currentFilters.employmentType);
    }

    if (currentFilters.fieldOfStudy) {
        params.append("fieldOfStudy", currentFilters.fieldOfStudy);
    }

    const queryString = params.toString();

    return queryString ? `/jobs?${queryString}` : "/jobs";
}

async function request(url) {
    try {
        const res = await fetch(url);
        const result = await res.json();
        return result;
    } catch (error) {
        console.error("Request failed:", error);
        return {
            code: 0,
            message: "Network error. Please check whether the Spring Boot server is running.",
            data: null
        };
    }
}

async function loadJobs() {
    const result = await request(buildJobsUrl());

    if (result.code !== 200 || !Array.isArray(result.data)) {
        showToast(result.message || "Failed to load jobs");
        allJobs = [];
        renderJobs([]);
        return;
    }

    allJobs = result.data;

    let displayJobs = allJobs;

    if (currentFilters.companyName) {
        displayJobs = allJobs.filter(job =>
            getCompanyName(job).toLowerCase() === currentFilters.companyName.toLowerCase()
        );
    }

    renderJobs(displayJobs);
    renderEmployersFromJobs(allJobs);
    updateFilterSummary(displayJobs.length);
}

function renderCities() {
    cityList.innerHTML = cities.map(city => `
        <button class="city-item" data-city="${city}">
            ${city}
        </button>
    `).join("");

    document.querySelectorAll(".city-item").forEach(btn => {
        btn.addEventListener("click", async () => {
            currentFilters.location = btn.dataset.city;

            document.querySelectorAll(".city-item").forEach(item => {
                item.classList.remove("active");
            });

            btn.classList.add("active");

            await loadJobs();
            scrollToJobs();
        });
    });
}

function renderCategories() {
    categoryList.innerHTML = categories.map(category => `
        <button class="category-item" data-field="${category.fieldOfStudy}">
            <div>
                <div class="category-main">${category.title}</div>
                <div class="category-sub">${category.sub}</div>
            </div>
            <span class="category-arrow">›</span>
        </button>
    `).join("");

    document.querySelectorAll(".category-item").forEach(btn => {
        btn.addEventListener("click", async () => {
            currentFilters.fieldOfStudy = btn.dataset.field;

            document.querySelectorAll(".category-item").forEach(item => {
                item.classList.remove("active");
            });

            btn.classList.add("active");

            await loadJobs();
            scrollToJobs();
        });
    });
}

function renderJobs(list) {
    if (!Array.isArray(list)) list = [];

    if (jobCountText) {
        jobCountText.textContent = list.length;
    }

    if (!list.length) {
        jobGrid.innerHTML = "";
        emptyState.classList.remove("hidden");
        return;
    }

    emptyState.classList.add("hidden");

    jobGrid.innerHTML = list.map(job => {
        const jobId = getJobId(job);
        const companyName = getCompanyName(job);
        const displayStatus = getDisplayStatus(job.status);

        return `
            <article class="job-card" onclick="openJobDetail(${jobId})">
                <div class="job-top">
                    <div>
                        <div class="job-title">${formatValue(job.title)}</div>
                        <div class="job-company">${formatValue(companyName)}</div>
                    </div>
                    <span class="status-pill">${formatValue(displayStatus)}</span>
                </div>

                <div class="job-meta">
                    <span class="meta-tag">${formatValue(job.location)}</span>
                    <span class="meta-tag">${formatValue(job.employmentType)}</span>
                    <span class="meta-tag">${formatValue(job.workMode)}</span>
                    <span class="meta-tag">${formatValue(job.fieldOfStudy)}</span>
                </div>

                <div class="job-description">
                    ${shortText(job.description)}
                </div>

                <div class="job-deadline">
                    Deadline: ${formatDate(job.deadline)}
                </div>

                <div class="job-footer">
                    <span class="salary">${formatSalary(job.salaryMin, job.salaryMax)}</span>
                    <span class="view-link">View Details</span>
                </div>
            </article>
        `;
    }).join("");
}
function escapeHtmlAttribute(value) {
    return String(value || "")
        .replaceAll("&", "&amp;")
        .replaceAll("'", "&#39;")
        .replaceAll('"', "&quot;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;");
}

window.filterByEmployer = function(companyName) {
    currentFilters.companyName = companyName;

    renderJobs(
        allJobs.filter(job =>
            getCompanyName(job).toLowerCase() === companyName.toLowerCase()
        )
    );

    renderEmployersFromJobs(allJobs);
    updateFilterSummary(
        allJobs.filter(job =>
            getCompanyName(job).toLowerCase() === companyName.toLowerCase()
        ).length
    );

    scrollToJobs();
};
function renderEmployersFromJobs(jobList) {
    const employerMap = new Map();

    if (Array.isArray(jobList)) {
        jobList.forEach(job => {
            const profile = getEmployerProfile(job);

            const employerName = profile.companyName || "";
            const employerId = profile.employerProfileId || profile.userId || job.employerId || employerName;

            if (employerName && !employerMap.has(employerId)) {
                employerMap.set(employerId, {
                    name: employerName,
                    industry: profile.industry || job.fieldOfStudy || "Hiring",
                    logoUrl: profile.logoUrl || "",
                    location: profile.location || job.location || "-",
                    jobCount: 1
                });
            } else if (employerMap.has(employerId)) {
                const employer = employerMap.get(employerId);
                employer.jobCount += 1;
            }
        });
    }

    const employers = Array.from(employerMap.values()).slice(0, 5);

    employerGrid.innerHTML = employers.map(employer => {
        const logoHtml = employer.logoUrl
            ? `<img src="${employer.logoUrl}" alt="${employer.name}" />`
            : employer.name.substring(0, 1).toUpperCase();

        const jobText = `${employer.jobCount} active job${employer.jobCount > 1 ? "s" : ""}`;

        const activeClass = currentFilters.companyName === employer.name
            ? "active"
            : "";

        return `
            <div class="employer-card ${activeClass}" onclick="filterByEmployer('${escapeHtmlAttribute(employer.name)}')">
                <div class="employer-logo">${logoHtml}</div>
                <div class="employer-name">${employer.name}</div>
                <div class="employer-meta">${employer.industry}</div>
                <div class="employer-extra">${employer.location} · ${jobText}</div>
            </div>
        `;
    }).join("");
}
function updateFilterSummary(displayCount) {
    const count = typeof displayCount === "number"
        ? displayCount
        : Array.isArray(allJobs) ? allJobs.length : 0;

    const parts = [];

    if (currentFilters.keyword) {
        parts.push(`keyword "${currentFilters.keyword}"`);
    }

    if (currentFilters.location) {
        parts.push(`city "${currentFilters.location}"`);
    }

    if (currentFilters.employmentType) {
        parts.push(`type "${currentFilters.employmentType}"`);
    }

    if (currentFilters.fieldOfStudy) {
        parts.push(`field "${currentFilters.fieldOfStudy}"`);
    }

    if (currentFilters.companyName) {
        parts.push(`company "${currentFilters.companyName}"`);
    }

    const jobText = count === 1 ? "active job" : "active jobs";

    if (parts.length) {
        filterSummary.textContent = `Showing ${count} ${jobText} for ${parts.join(", ")}`;
    } else {
        filterSummary.textContent = `Showing ${count} ${jobText} from the database`;
    }
}
async function openJobDetail(id) {
    selectedJobId = id;

    const result = await request(`/jobs/${id}`);

    if (result.code !== 200 || !result.data) {
        showToast(result.message || "Failed to load job details");
        return;
        const detailCompanyName = document.getElementById("detailCompanyName");
        if (detailCompanyName) {
            detailCompanyName.textContent = formatValue(getCompanyName(job));
        }
    }

    const job = result.data;

    document.getElementById("detailTitle").textContent = formatValue(job.title);
    document.getElementById("detailJobId").textContent = formatValue(getJobId(job));
    document.getElementById("detailLocation").textContent = formatValue(job.location);
    document.getElementById("detailEmploymentType").textContent = formatValue(job.employmentType);
    document.getElementById("detailWorkMode").textContent = formatValue(job.workMode);
    document.getElementById("detailFieldOfStudy").textContent = formatValue(job.fieldOfStudy);
    document.getElementById("detailSalary").textContent = formatSalary(job.salaryMin, job.salaryMax);
    document.getElementById("detailDeadline").textContent = formatDate(job.deadline);
    document.getElementById("detailStatus").textContent = formatValue(getDisplayStatus(job.status));
    document.getElementById("detailDescription").textContent =
        job.description || "No job description provided.";

    jobDetailModal.classList.remove("hidden");
}

function closeJobDetail() {
    jobDetailModal.classList.add("hidden");
}

function scrollToJobs() {
    document.getElementById("latestJobsSection").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

function clearFilters() {
    currentFilters = {
        keyword: "",
        location: "",
        employmentType: "",
        fieldOfStudy: "",
        companyName: ""
    };

    if (keywordInput) {
        keywordInput.value = "";
    }

    document.querySelectorAll(".city-item, .category-item, .employer-card").forEach(item => {
        item.classList.remove("active");
    });

    loadJobs();
}

function bindEvents() {
    searchBtn.addEventListener("click", async () => {
        currentFilters.keyword = keywordInput.value.trim();
        await loadJobs();
        scrollToJobs();
    });

    keywordInput.addEventListener("keydown", async event => {
        if (event.key === "Enter") {
            currentFilters.keyword = keywordInput.value.trim();
            await loadJobs();
            scrollToJobs();
        }
    });

    clearFiltersBtn.addEventListener("click", clearFilters);

    document.querySelectorAll(".employment-link").forEach(link => {
        link.addEventListener("click", async event => {
            event.preventDefault();
            currentFilters.employmentType = link.dataset.type;
            await loadJobs();
            scrollToJobs();
        });
    });

    document.getElementById("browseJobsBtn").addEventListener("click", scrollToJobs);

    document.getElementById("postJobBtn").addEventListener("click", () => {
        window.location.href = "/admin/login.html";
    });

    document.getElementById("loginBtn").addEventListener("click", () => {
        window.location.href = "/admin/login.html";
    });

    closeDetailBtn.addEventListener("click", closeJobDetail);

    jobDetailModal.addEventListener("click", event => {
        if (event.target === jobDetailModal) {
            closeJobDetail();
        }
    });

    applyNowBtn.addEventListener("click", () => {
        if (!selectedJobId) {
            showToast("Please select a job first.");
            return;
        }

        window.location.href = "/admin/login.html";
    });
}
function initHomePage() {
    renderCities();
    renderCategories();
    bindEvents();
    loadJobs();
}

document.addEventListener("DOMContentLoaded", initHomePage);