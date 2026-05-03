
let deleteTargetId = null;
let editingJobId = null;
let applicationRefreshTimer = null;

let actionTargetId = null;
let actionType = "";
let closeTargetId = null;

let statusPieChart = null;
let jobBarChart = null;
let applicationTrendChart = null;

let jobs = [];
let applications = [];

const navVerification = document.getElementById("navVerification");
const navJobs = document.getElementById("navJobs");
const navApplications = document.getElementById("navApplications");
const navDashboard = document.getElementById("navDashboard");
const navMessages = document.getElementById("navMessages");

const verificationPage = document.getElementById("verificationPage");
const editProfilePage = document.getElementById("editProfilePage");
const jobsPage = document.getElementById("jobsPage");
const applicationsPage = document.getElementById("applicationsPage");
const dashboardPage = document.getElementById("dashboardPage");
const messagesPage = document.getElementById("messagesPage");
const createJobPage = document.getElementById("createJobPage");

const pageTitle = document.getElementById("pageTitle");
const jobTableBody = document.getElementById("jobTableBody");
const applicationTableBody = document.getElementById("applicationTableBody");
const applicationJobSelect = document.getElementById("applicationJobSelect");
const applicationStudentSearch = document.getElementById("applicationStudentSearch");
const messagesList = document.getElementById("messagesList");

const loginStatusTag = document.getElementById("loginStatusTag");

const confirmModal = document.getElementById("confirmModal");
const cancelDeleteBtn = document.getElementById("cancelDeleteBtn");
const confirmDeleteBtn = document.getElementById("confirmDeleteBtn");

const publishJobBtn = document.getElementById("publishJobBtn");
const updateJobBtn = document.getElementById("updateJobBtn");

const applicationDetailModal = document.getElementById("applicationDetailModal");
const closeApplicationDetailBtn = document.getElementById("closeApplicationDetailBtn");

const jobDetailModal = document.getElementById("jobDetailModal");
const closeJobDetailBtn = document.getElementById("closeJobDetailBtn");

function showToast(message, type = "success") {
  let toast = document.getElementById("toast");

  if (!toast) {
    toast = document.createElement("div");
    toast.id = "toast";
    toast.className = "toast";
    document.body.appendChild(toast);
  }

  toast.textContent = message;
  toast.className = `toast show ${type}`;

  setTimeout(() => {
    toast.classList.remove("show");
  }, 2500);
}

function showError(message) {
  showToast(message || "Something went wrong", "error");
}

function setConfirmModalText(title, text, confirmButtonText = "Confirm") {
  if (!confirmModal) return;

  const titleEl = document.getElementById("confirmTitle") || confirmModal.querySelector("h3");
  const textEl = document.getElementById("confirmText") || confirmModal.querySelector("p");

  if (titleEl) titleEl.textContent = title;
  if (textEl) textEl.textContent = text;
  if (confirmDeleteBtn) confirmDeleteBtn.textContent = confirmButtonText;
}

function resetConfirmState() {
  deleteTargetId = null;
  closeTargetId = null;
  actionTargetId = null;
  actionType = "";
  setConfirmModalText("Confirm Action", "Are you sure?", "Confirm");
}

function setLoginStatus(loggedIn) {
  const loginBtn = document.getElementById("loginBtn");

  if (loginBtn) {
    loginBtn.textContent = loggedIn ? "Log Out" : "Login";
  }

  if (!loginStatusTag) return;

  loginStatusTag.textContent = loggedIn ? "Logged In" : "Not Logged In";
  loginStatusTag.style.background = loggedIn ? "#ecfdf5" : "#f3f4f6";
  loginStatusTag.style.color = loggedIn ? "#15803d" : "#6b7280";
  loginStatusTag.style.borderColor = loggedIn ? "#bbf7d0" : "#d1d5db";
}

function getToken() {
  return localStorage.getItem("employerToken");
}

async function apiFetch(url, method = "GET", body = null) {
  try {
    const options = {
      method: method,
      headers: {
        "Authorization": `Bearer ${getToken()}`
      }
    };

    if (body) {
      options.headers["Content-Type"] = "application/json";
      options.body = JSON.stringify(body);
    }

    const res = await fetch(url, options);
    const result = await res.json();

    return result;
  } catch (error) {
    console.error("Network request failed:", error);
    return {
      code: 0,
      message: "Network error. Please check backend server.",
      data: null
    };
  }
}

function ensureLoggedIn() {
  const token = getToken();

  if (!token) {
    showError("Please log in as an employer first");
    return false;
  }

  return true;
}

function hideAllPages() {
  [
    verificationPage,
    editProfilePage,
    jobsPage,
    applicationsPage,
    dashboardPage,
    messagesPage,
    createJobPage
  ].forEach(page => {
    if (page) page.classList.remove("active");
  });

  [
    navVerification,
    navJobs,
    navApplications,
    navDashboard,
    navMessages
  ].forEach(nav => {
    if (nav) nav.classList.remove("active");
  });
}

function showPage(page) {
  hideAllPages();

  if (page === "verification") {
    if (verificationPage) verificationPage.classList.add("active");
    if (navVerification) navVerification.classList.add("active");
    if (pageTitle) pageTitle.textContent = "Company Verification";
  }

  if (page === "editProfile") {
    if (editProfilePage) editProfilePage.classList.add("active");
    if (navVerification) navVerification.classList.add("active");
    if (pageTitle) pageTitle.textContent = "Edit Company Profile";
  }

  if (page === "jobs") {
    if (jobsPage) jobsPage.classList.add("active");
    if (navJobs) navJobs.classList.add("active");
    if (pageTitle) pageTitle.textContent = "Job Management";
  }

  if (page === "applications") {
    if (applicationsPage) applicationsPage.classList.add("active");
    if (navApplications) navApplications.classList.add("active");
    if (pageTitle) pageTitle.textContent = "Application Management";
  }

  if (page === "dashboard") {
    if (dashboardPage) dashboardPage.classList.add("active");
    if (navDashboard) navDashboard.classList.add("active");
    if (pageTitle) pageTitle.textContent = "Analytics Dashboard";
  }

  if (page === "messages") {
    if (messagesPage) messagesPage.classList.add("active");
    if (navMessages) navMessages.classList.add("active");
    if (pageTitle) pageTitle.textContent = "My Messages";
  }

  if (page === "createJob") {
    if (createJobPage) createJobPage.classList.add("active");
    if (navJobs) navJobs.classList.add("active");
    if (pageTitle) pageTitle.textContent = editingJobId ? "Edit Job" : "Create Job";
  }
}

function formatDeadlineForBackend(value) {
  if (!value) return "";
  return value + "T23:59:59";
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

  if (hasMin && hasMax) return `${min}-${max}`;
  if (hasMin) return `${min}`;
  if (hasMax) return `${max}`;
  return "-";
}

function getApplicationStatusClass(status) {
  if (status === "submitted") return "status-submitted";
  if (status === "reviewing") return "status-reviewing";
  if (status === "accepted") return "status-accepted";
  if (status === "rejected") return "status-rejected";
  return "";
}

function getJobStatusClass(status) {
  if (status === "active" || status === "approved") return "status-active";
  if (status === "closed") return "status-closed";
  if (status === "deleted") return "status-deleted";
  if (status === "pending") return "status-reviewing";
  return "";
}

function getJobStatusText(status) {
  if (status === "pending") return "Pending Review";
  if (status === "approved" || status === "active") return "Active";
  if (status === "closed") return "Closed";
  if (status === "deleted") return "Deleted";
  return status || "-";
}

function getJobTitleById(jobId) {
  const job = jobs.find(j => String(j.jobId) === String(jobId));
  return job ? job.title : "-";
}

function getJobFormPayload() {
  const title = document.getElementById("jobTitle")?.value.trim() || "";
  const description = document.getElementById("jobDescription")?.value.trim() || "";
  const employmentType = document.getElementById("jobEmploymentType")?.value || "";
  const workMode = document.getElementById("jobWorkMode")?.value || "";
  const location = document.getElementById("jobLocation")?.value.trim() || "";
  const fieldOfStudy = document.getElementById("jobFieldOfStudy")?.value.trim() || "";
  const salaryMin = Number(document.getElementById("jobSalaryMin")?.value);
  const salaryMax = Number(document.getElementById("jobSalaryMax")?.value);
  const deadlineInput = document.getElementById("jobDeadline")?.value || "";

  if (!title || !location || !fieldOfStudy || !description || !deadlineInput) {
    showError("Please fill in all required fields: title, location, field of study, deadline, and description.");
    return null;
  }

  if (Number.isNaN(salaryMin) || Number.isNaN(salaryMax)) {
    showError("Please enter valid salary numbers.");
    return null;
  }

  if (salaryMin < 0 || salaryMax < 0) {
    showError("Salary cannot be negative.");
    return null;
  }

  if (salaryMin > salaryMax) {
    showError("Minimum salary cannot be greater than maximum salary.");
    return null;
  }

  return {
    title,
    categoryId: 1,
    description,
    requirements: "Default requirements",
    employmentType,
    workMode,
    location,
    fieldOfStudy,
    salaryMin,
    salaryMax,
    deadline: formatDeadlineForBackend(deadlineInput)
  };
}

// Company profile and verification

function fillProfileForm(profile, verification) {
  if (!profile) profile = {};

  const editCompanyName = document.getElementById("editCompanyName");
  const editIndustry = document.getElementById("editIndustry");
  const editCompanySize = document.getElementById("editCompanySize");
  const editLocation = document.getElementById("editLocation");
  const editContactPerson = document.getElementById("editContactPerson");
  const editContactEmail = document.getElementById("editContactEmail");
  const editCompanyDescription = document.getElementById("editCompanyDescription");
  const editBusinessLicenseUrl = document.getElementById("editBusinessLicenseUrl");
  const editSupportingDocumentUrl = document.getElementById("editSupportingDocumentUrl");

  if (editCompanyName) editCompanyName.value = profile.companyName || "";
  if (editIndustry) editIndustry.value = profile.industry || "";
  if (editCompanySize) editCompanySize.value = profile.companySize || "";
  if (editLocation) editLocation.value = profile.location || "";
  if (editContactPerson) editContactPerson.value = profile.contactPerson || "";
  if (editContactEmail) editContactEmail.value = profile.contactEmail || "";
  if (editCompanyDescription) {
    editCompanyDescription.value = profile.companyDescription || profile.description || "";
  }
  if (editBusinessLicenseUrl) {
    editBusinessLicenseUrl.value = verification?.businessLicenseUrl || "";
  }
  if (editSupportingDocumentUrl) {
    editSupportingDocumentUrl.value = verification?.supportingDocumentUrl || "";
  }
}

function renderProfile(profile, verification) {
  if (!profile) profile = {};

  const companyNameText = document.getElementById("companyNameText");
  const companyMetaText = document.getElementById("companyMetaText");
  const contactPersonText = document.getElementById("contactPersonText");
  const contactEmailText = document.getElementById("contactEmailText");
  const companyDescriptionText = document.getElementById("companyDescriptionText");
  const businessLicenseText = document.getElementById("businessLicenseText");
  const supportDocumentText = document.getElementById("supportDocumentText");
const verificationStatusBadge = document.getElementById("verificationStatusBadge");

if (verificationStatusBadge) {
  const status =
    verification?.reviewStatus ||
    verification?.verificationStatus ||
    profile.verificationStatus ||
    "pending";

  verificationStatusBadge.textContent =
    status === "approved" ? "Verified" :
    status === "rejected" ? "Rejected" :
    "Pending";

  verificationStatusBadge.className =
    status === "approved" ? "verify-badge status-active" :
    status === "rejected" ? "verify-badge status-rejected" :
    "verify-badge status-reviewing";
}
  if (companyNameText) companyNameText.textContent = profile.companyName || "-";
  if (companyMetaText) {
    companyMetaText.textContent =
      `${profile.industry || "-"} | ${profile.companySize || "-"} | ${profile.location || "-"}`;
  }
  if (contactPersonText) contactPersonText.textContent = profile.contactPerson || "-";
  if (contactEmailText) contactEmailText.textContent = profile.contactEmail || "-";
  if (companyDescriptionText) {
    companyDescriptionText.textContent =
      profile.companyDescription || profile.description || "No company description";
  }
  if (businessLicenseText) {
    businessLicenseText.textContent =
      verification?.businessLicenseUrl || "No verification information";
  }
  if (supportDocumentText) {
    supportDocumentText.textContent =
      verification?.supportingDocumentUrl || "No verification information";
  }
}

async function loadEmployerProfile() {
  if (!ensureLoggedIn()) return;

  const profileRes = await request("/employers/profile", {
    method: "GET",
    headers: authHeaders()
  });

  if (profileRes.code !== 200 || !profileRes.data) {
    showError(profileRes.message || "Failed to load company profile");
    return;
  }

  const verifyRes = await request("/verification/my", {
    method: "GET",
    headers: authHeaders()
  });

  const verificationData = verifyRes.code === 200 ? verifyRes.data : null;
  fillProfileForm(profileRes.data, verificationData);
  renderProfile(profileRes.data, verificationData);
}

async function loadEmployerProfile() {
  if (!ensureLoggedIn()) return;

  const profileRes = await apiFetch("/employers/profile");

  if (profileRes.code !== 200 || !profileRes.data) {
    showError(profileRes.message || "Failed to load company profile");
    return;
  }

  const verifyRes = await apiFetch("/verification/my");

  const verificationData = verifyRes.code === 200 ? verifyRes.data : null;

  fillProfileForm(profileRes.data, verificationData);
  renderProfile(profileRes.data, verificationData);
}

async function saveEmployerProfile() {
  if (!ensureLoggedIn()) return;

  const profilePayload = {
    companyName: document.getElementById("editCompanyName")?.value || "",
    industry: document.getElementById("editIndustry")?.value || "",
    companySize: document.getElementById("editCompanySize")?.value || "",
    location: document.getElementById("editLocation")?.value || "",
    contactPerson: document.getElementById("editContactPerson")?.value || "",
    contactEmail: document.getElementById("editContactEmail")?.value || "",
    companyDescription: document.getElementById("editCompanyDescription")?.value || ""
  };

  const profileResult = await apiFetch("/employers/profile", "PUT", profilePayload);

  if (profileResult.code !== 200) {
    showError(profileResult.message || "Failed to save company profile");
    return;
  }

  const businessLicenseUrl =
    document.getElementById("editBusinessLicenseUrl")?.value.trim() || "";

  const supportingDocumentUrl =
    document.getElementById("editSupportingDocumentUrl")?.value.trim() || "";

  if (businessLicenseUrl) {
    const verificationResult = await apiFetch(
      `/verification/submit?businessLicenseUrl=${encodeURIComponent(businessLicenseUrl)}&supportingDocumentUrl=${encodeURIComponent(supportingDocumentUrl)}`,
      "POST"
    );

    if (
      verificationResult.code !== 200 &&
      verificationResult.message !== "Verification request already exists"
    ) {
      showError(verificationResult.message || "Failed to submit verification documents");
      return;
    }
  }

  await loadEmployerProfile();
  showPage("verification");
}

// Jobs

function renderJobSummary(jobList) {
  if (!Array.isArray(jobList)) jobList = [];

  const jobTotal = document.getElementById("jobTotal");
  const jobPending = document.getElementById("jobPending");
  const jobApproved = document.getElementById("jobApproved");
  const jobClosed = document.getElementById("jobClosed");

  if (jobTotal) jobTotal.textContent = jobList.length;

  if (jobPending) {
    jobPending.textContent =
      jobList.filter(j => j.status === "pending").length;
  }

  if (jobApproved) {
    jobApproved.textContent =
      jobList.filter(j => j.status === "approved" || j.status === "active").length;
  }

  if (jobClosed) {
    jobClosed.textContent =
      jobList.filter(j => j.status === "closed").length;
  }
}

function renderJobs(list) {
  if (!Array.isArray(list)) list = [];

  renderJobSummary(jobs);

  if (!jobTableBody) return;

  if (!list.length) {
    jobTableBody.innerHTML = `
      <tr>
        <td colspan="9" class="empty-state">
          <div>No matching jobs found</div>
          <small>Try changing the keyword or status filter.</small>
        </td>
      </tr>
    `;
    return;
  }

  jobTableBody.innerHTML = list.map(job => `
    <tr>
      <td>${formatValue(job.jobId)}</td>
      <td>${formatValue(job.title)}</td>
      <td>${formatValue(job.fieldOfStudy)}</td>
      <td>${formatValue(job.employmentType)}</td>
      <td>${formatValue(job.workMode)}</td>
      <td>${formatSalary(job.salaryMin, job.salaryMax)}</td>
      <td>${formatValue(job.location)}</td>
      <td>
        <span class="status-pill ${getJobStatusClass(job.status)}">
          ${getJobStatusText(job.status)}
        </span>
      </td>
      <td>
        <div class="action-cell">
          <button class="mini-btn" onclick="viewJobDetail(${job.jobId})">View</button>
          <button class="mini-btn" onclick="editJob(${job.jobId})">Edit</button>
          <button class="mini-btn" onclick="closeJob(${job.jobId})">Close</button>
          <button class="mini-btn" onclick="deleteJob(${job.jobId})">Delete</button>
        </div>
      </td>
    </tr>
  `).join("");
}

function loadJobOptions() {
  if (!applicationJobSelect) return;

  const safeJobs = Array.isArray(jobs) ? jobs : [];

  applicationJobSelect.innerHTML = `
    <option value="">All Jobs</option>
    ${safeJobs.map(j => `<option value="${j.jobId}">${j.title}</option>`).join("")}
  `;
}

function fillCreateFormDefault() {
  const jobTitle = document.getElementById("jobTitle");
  const jobIdInput = document.getElementById("jobIdInput");
  const jobEmploymentType = document.getElementById("jobEmploymentType");
  const jobWorkMode = document.getElementById("jobWorkMode");
  const jobLocation = document.getElementById("jobLocation");
  const jobFieldOfStudy = document.getElementById("jobFieldOfStudy");
  const jobSalaryMin = document.getElementById("jobSalaryMin");
  const jobSalaryMax = document.getElementById("jobSalaryMax");
  const jobDeadline = document.getElementById("jobDeadline");
  const jobDescription = document.getElementById("jobDescription");

  if (jobTitle) jobTitle.value = "";
  if (jobIdInput) jobIdInput.value = "Auto Generated";
  if (jobEmploymentType) jobEmploymentType.value = "internship";
  if (jobWorkMode) jobWorkMode.value = "hybrid";
  if (jobLocation) jobLocation.value = "";
  if (jobFieldOfStudy) jobFieldOfStudy.value = "";
  if (jobSalaryMin) jobSalaryMin.value = "";
  if (jobSalaryMax) jobSalaryMax.value = "";
  if (jobDeadline) jobDeadline.value = "";
  if (jobDescription) jobDescription.value = "";
}

function fillEditJobForm(job) {
  if (!job) job = {};

  const jobTitle = document.getElementById("jobTitle");
  const jobIdInput = document.getElementById("jobIdInput");
  const jobEmploymentType = document.getElementById("jobEmploymentType");
  const jobWorkMode = document.getElementById("jobWorkMode");
  const jobLocation = document.getElementById("jobLocation");
  const jobFieldOfStudy = document.getElementById("jobFieldOfStudy");
  const jobSalaryMin = document.getElementById("jobSalaryMin");
  const jobSalaryMax = document.getElementById("jobSalaryMax");
  const jobDeadline = document.getElementById("jobDeadline");
  const jobDescription = document.getElementById("jobDescription");

  if (jobTitle) jobTitle.value = job.title || "";
  if (jobIdInput) jobIdInput.value = job.jobId || "";
  if (jobEmploymentType) jobEmploymentType.value = job.employmentType || "internship";
  if (jobWorkMode) jobWorkMode.value = job.workMode || "hybrid";
  if (jobLocation) jobLocation.value = job.location || "";
  if (jobFieldOfStudy) jobFieldOfStudy.value = job.fieldOfStudy || "";
  if (jobSalaryMin) jobSalaryMin.value = job.salaryMin ?? "";
  if (jobSalaryMax) jobSalaryMax.value = job.salaryMax ?? "";
  if (jobDeadline) {
    jobDeadline.value = job.deadline
      ? job.deadline.substring(0, 10)
      : "";
  }
  if (jobDescription) jobDescription.value = job.description || "";
}

function setCreateMode() {
  editingJobId = null;
  if (publishJobBtn) publishJobBtn.classList.remove("hidden");
  if (updateJobBtn) updateJobBtn.classList.add("hidden");
}

function setEditMode() {
  if (publishJobBtn) publishJobBtn.classList.add("hidden");
  if (updateJobBtn) updateJobBtn.classList.remove("hidden");
}

async function loadJobs() {
  if (!ensureLoggedIn()) return;

  const keyword = document.getElementById("jobSearchInput")?.value.trim().toLowerCase() || "";
  const statusFilter = document.getElementById("jobStatusFilter")?.value || "";

  const result = await apiFetch("/jobs/my");

  if (result.code !== 200 || !Array.isArray(result.data)) {
    showError(result.message || "Failed to load jobs");
    return;
  }

  jobs = result.data.filter(job => job.status !== "deleted");

  let filtered = jobs;

  if (statusFilter) {
    filtered = filtered.filter(job => job.status === statusFilter);
  }

  if (keyword) {
    filtered = filtered.filter(job =>
      `${job.jobId} ${job.title} ${job.fieldOfStudy} ${job.employmentType} ${job.workMode} ${job.location}`
        .toLowerCase()
        .includes(keyword)
    );
  }

  renderJobs(filtered);
}

async function loginEmployer() {
  const email = document.getElementById("loginEmail")?.value.trim() || "";
  const password = document.getElementById("loginPassword")?.value.trim() || "";

  if (!email || !password) {
    showError("Please enter email and password.");
    return;
  }

  const res = await fetch("/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      email: email,
      password: password
    })
  });

  const result = await res.json();

  if (result.code !== 200 || !result.data || !result.data.token) {
    showError(result.message || "Login failed");
    return;
  }

  localStorage.setItem("employerToken", result.data.token);

  setLoginStatus(true);
  await loadEmployerProfile();
}

async function publishJob() {
  if (!ensureLoggedIn()) return;

  const payload = getJobFormPayload();
  if (!payload) return;

  const result = await apiFetch("/jobs", "POST", payload);

  if (result.code !== 200) {
    showError(result.message || "Failed to create job");
    return;
  }

  await loadJobs();
  setCreateMode();
  showPage("jobs");
}

async function updateJob() {
  if (!ensureLoggedIn()) return;

  if (!editingJobId) {
    showError("No job is currently being edited");
    return;
  }

  const payload = getJobFormPayload();
  if (!payload) return;

  const result = await apiFetch(`/jobs/${editingJobId}`, "PUT", payload);

  if (result.code !== 200) {
    showError(result.message || "Failed to update job");
    return;
  }

  await loadJobs();
  setCreateMode();
  showPage("jobs");
}

window.editJob = async function(id) {
  if (!ensureLoggedIn()) return;

  const result = await apiFetch(`/jobs/${id}`);

  if (result.code !== 200 || !result.data) {
    showError(result.message || "Failed to load job details");
    return;
  }

  editingJobId = id;
  fillEditJobForm(result.data);
  setEditMode();
  showPage("createJob");
};

window.viewJobDetail = async function(id) {
  if (!ensureLoggedIn()) return;

  const result = await apiFetch(`/jobs/${id}`);

  if (result.code !== 200 || !result.data) {
    showError(result.message || "Failed to load job details");
    return;
  }

  const j = result.data;

  const jobDetailId = document.getElementById("jobDetailId");
  const jobDetailTitle = document.getElementById("jobDetailTitle");
  const jobDetailField = document.getElementById("jobDetailField");
  const jobDetailEmploymentType = document.getElementById("jobDetailEmploymentType");
  const jobDetailWorkMode = document.getElementById("jobDetailWorkMode");
  const jobDetailLocation = document.getElementById("jobDetailLocation");
  const jobDetailSalary = document.getElementById("jobDetailSalary");
  const jobDetailDeadline = document.getElementById("jobDetailDeadline");
  const jobDetailStatus = document.getElementById("jobDetailStatus");
  const jobDetailDescription = document.getElementById("jobDetailDescription");

  if (jobDetailId) jobDetailId.textContent = formatValue(j.jobId);
  if (jobDetailTitle) jobDetailTitle.textContent = formatValue(j.title);
  if (jobDetailField) jobDetailField.textContent = formatValue(j.fieldOfStudy);
  if (jobDetailEmploymentType) jobDetailEmploymentType.textContent = formatValue(j.employmentType);
  if (jobDetailWorkMode) jobDetailWorkMode.textContent = formatValue(j.workMode);
  if (jobDetailLocation) jobDetailLocation.textContent = formatValue(j.location);
  if (jobDetailSalary) jobDetailSalary.textContent = formatSalary(j.salaryMin, j.salaryMax);
  if (jobDetailDeadline) jobDetailDeadline.textContent = formatDate(j.deadline);
  if (jobDetailStatus) jobDetailStatus.textContent = getJobStatusText(j.status);

  if (jobDetailDescription) {
    jobDetailDescription.textContent = j.description || "No job description provided.";
  }

  if (jobDetailModal) {
    jobDetailModal.classList.remove("hidden");
  }
};

window.closeJob = function(id) {
  resetConfirmState();

  closeTargetId = id;

  setConfirmModalText(
    "Confirm Close",
    "Are you sure you want to close this job?",
    "Close"
  );

  if (confirmModal) confirmModal.classList.remove("hidden");
};

window.deleteJob = function(id) {
  resetConfirmState();

  deleteTargetId = id;

  setConfirmModalText(
    "Confirm Delete",
    "Are you sure you want to delete this job?",
    "Delete"
  );

  if (confirmModal) confirmModal.classList.remove("hidden");
};

if (cancelDeleteBtn) {
  cancelDeleteBtn.onclick = () => {
    if (confirmModal) confirmModal.classList.add("hidden");
    resetConfirmState();
  };
}

if (confirmDeleteBtn) {
  confirmDeleteBtn.onclick = async () => {
    if (!ensureLoggedIn()) return;

    if (deleteTargetId) {
      const result = await apiFetch(`/jobs/${deleteTargetId}`, "DELETE");

      if (result.code !== 200) {
        showError(result.message || "Failed to delete job");
        return;
      }

      if (confirmModal) confirmModal.classList.add("hidden");
      resetConfirmState();

      await loadJobs();
      return;
    }

    if (closeTargetId) {
      const result = await apiFetch(`/jobs/${closeTargetId}/close`, "PATCH");

      if (result.code !== 200) {
        showError(result.message || "Failed to close job");
        return;
      }

      if (confirmModal) confirmModal.classList.add("hidden");
      resetConfirmState();

      await loadJobs();
      return;
    }

    if (actionTargetId && actionType) {
      const result = await apiFetch(`/applications/${actionTargetId}/status`, "PATCH", {
        status: actionType
      });

      if (result.code !== 200) {
        showError(result.message || "Failed to update status");
        return;
      }

      if (confirmModal) confirmModal.classList.add("hidden");
      resetConfirmState();

      await loadApplications();
    }
  };
}

// Application management

function renderApplicationSummary(list) {
  if (!Array.isArray(list)) list = [];

  const applicationSummaryTotal = document.getElementById("applicationSummaryTotal");
  const applicationSummarySubmitted = document.getElementById("applicationSummarySubmitted");
  const applicationSummaryReviewing = document.getElementById("applicationSummaryReviewing");
  const applicationSummaryAccepted = document.getElementById("applicationSummaryAccepted");
  const applicationSummaryRejected = document.getElementById("applicationSummaryRejected");

  if (applicationSummaryTotal) applicationSummaryTotal.textContent = list.length;
  if (applicationSummarySubmitted) {
    applicationSummarySubmitted.textContent =
      list.filter(a => a.status === "submitted").length;
  }
  if (applicationSummaryReviewing) {
    applicationSummaryReviewing.textContent =
      list.filter(a => a.status === "reviewing").length;
  }
  if (applicationSummaryAccepted) {
    applicationSummaryAccepted.textContent =
      list.filter(a => a.status === "accepted").length;
  }
  if (applicationSummaryRejected) {
    applicationSummaryRejected.textContent =
      list.filter(a => a.status === "rejected").length;
  }
}

function registerNewApplicationMessages(list) {
  // temporarily disabled to avoid initialization error
}
function renderApplications(list) {
  if (!Array.isArray(list)) list = [];

  renderApplicationSummary(list);

  // 按最新申请排序
  list.sort((a, b) => (b.applicationId ?? 0) - (a.applicationId ?? 0));

  // ✅ 新增：新申请自动生成消息
  registerNewApplicationMessages(list);

  if (!applicationTableBody) return;

  if (!list.length) {
    applicationTableBody.innerHTML = `
      <tr>
        <td colspan="7" class="empty-state">
          <div>No application data</div>
          <small>Applications submitted by students will appear here.</small>
        </td>
      </tr>
    `;
    return;
  }

  applicationTableBody.innerHTML = list.map((a, index) => `
    <tr>
      <td>No.${index + 1}</td>
      <td>${formatValue(a.jobId)}</td>
      <td class="application-job-title">${getJobTitleById(a.jobId)}</td>
      <td>${formatValue(a.studentId)}</td>
      <td>${a.studentName || "-"}</td>
      <td>
        <span class="status-pill ${getApplicationStatusClass(a.status)}">
          ${a.status ?? ""}
        </span>
      </td>
      <td>
        <div class="application-action-group">
         <button class="mini-btn" onclick="viewApplicationDetail(${a.applicationId})">View</button>

          <button class="mini-btn status-btn-reviewing" onclick="openStatusConfirm(${a.applicationId}, 'reviewing')">Review</button>
          <button class="mini-btn status-btn-accepted" onclick="openStatusConfirm(${a.applicationId}, 'accepted')">Accept</button>
          <button class="mini-btn status-btn-rejected" onclick="openStatusConfirm(${a.applicationId}, 'rejected')">Reject</button>
        </div>
      </td>
    </tr>
  `).join("");
}

function openStatusConfirm(id, type) {
  resetConfirmState();

  actionTargetId = id;
  actionType = type;

  if (type === "reviewing") {
    setConfirmModalText(
      "Confirm Action",
      "Mark this application as reviewing?",
      "Confirm"
    );
  }

  if (type === "accepted") {
    setConfirmModalText(
      "Confirm Action",
      "Accept this application?",
      "Confirm"
    );
  }

  if (type === "rejected") {
    setConfirmModalText(
      "Confirm Action",
      "Reject this application?",
      "Confirm"
    );
  }

  if (confirmModal) confirmModal.classList.remove("hidden");
}

async function loadApplications() {
  if (!ensureLoggedIn()) return;

  const selectedJobId = applicationJobSelect?.value || "";
  const status = document.getElementById("applicationStatusFilter")?.value || "";
  const keyword = applicationStudentSearch?.value.trim().toLowerCase() || "";

  let allApplications = [];

  const safeJobs = Array.isArray(jobs) ? jobs : [];

  const targetJobs = selectedJobId
    ? safeJobs.filter(j => String(j.jobId) === String(selectedJobId))
    : safeJobs;

  for (const job of targetJobs) {
    const result = await apiFetch(`/applications/job/${job.jobId}`);

    if (result.code === 200 && Array.isArray(result.data)) {
      allApplications = allApplications.concat(result.data);
    }
  }

  applications = allApplications;

  let filtered = applications;

  if (status) {
    filtered = filtered.filter(a => a.status === status);
  }

  if (keyword) {
    filtered = filtered.filter(a =>
      String(a.studentId ?? "").toLowerCase().includes(keyword) ||
      String(a.studentName ?? "").toLowerCase().includes(keyword) ||
      String(a.studentEmail ?? "").toLowerCase().includes(keyword) ||
      String(a.applicationId ?? "").toLowerCase().includes(keyword)
    );
  }

  renderApplications(filtered);
  renderDashboardCharts();
}

window.viewApplicationDetail = async function(applicationId) {
  if (!ensureLoggedIn()) return;

  const result = await apiFetch(`/applications/${applicationId}`);

  if (result.code !== 200 || !result.data) {
    showError(result.message || "Failed to load application details");
    return;
  }

  const d = result.data;

  const detailApplicationId = document.getElementById("detailApplicationId");
  const detailJobId = document.getElementById("detailJobId");
  const detailJobTitle = document.getElementById("detailJobTitle");
  const detailStudentId = document.getElementById("detailStudentId");
  const detailStudentName = document.getElementById("detailStudentName");
  const detailStudentEmail = document.getElementById("detailStudentEmail");
  const detailStatus = document.getElementById("detailStatus");
  const detailAppliedAt = document.getElementById("detailAppliedAt");
  const detailCoverLetter = document.getElementById("detailCoverLetter");

  if (detailApplicationId) detailApplicationId.textContent = d.applicationId ?? "-";
  if (detailJobId) detailJobId.textContent = d.jobId ?? "-";
  if (detailJobTitle) detailJobTitle.textContent = d.jobTitle || getJobTitleById(d.jobId);
  if (detailStudentId) detailStudentId.textContent = d.studentId ?? "-";
  if (detailStudentName) detailStudentName.textContent = d.studentName || "-";
  if (detailStudentEmail) detailStudentEmail.textContent = d.studentEmail || "-";
  if (detailStatus) detailStatus.textContent = d.status ?? "-";

  if (detailAppliedAt) {
    detailAppliedAt.textContent = d.appliedAt
      ? String(d.appliedAt).replace("T", " ")
      : "-";
  }

  if (detailCoverLetter) {
    detailCoverLetter.textContent =
      d.coverLetterText || "No cover letter provided";
  }

  if (applicationDetailModal) {
    applicationDetailModal.classList.remove("hidden");
  }
};

if (closeApplicationDetailBtn) {
  closeApplicationDetailBtn.addEventListener("click", () => {
    if (applicationDetailModal) {
      applicationDetailModal.classList.add("hidden");
    }
  });
}

if (closeJobDetailBtn) {
  closeJobDetailBtn.addEventListener("click", () => {
    if (jobDetailModal) {
      jobDetailModal.classList.add("hidden");
    }
  });
}

window.updateApplicationStatus = async function(id, status) {
  if (!ensureLoggedIn()) return;

  const result = await apiFetch(`/applications/${id}/status`, "PATCH", {
    status: status
  });

  if (result.code !== 200) {
    showError(result.message || "Failed to update status");
    return;
  }

  await loadApplications();
};

// Dashboard charts

function setChartEmptyState(canvasId, emptyId, isEmpty) {
  const canvas = document.getElementById(canvasId);
  const emptyText = document.getElementById(emptyId);

  if (canvas) {
    canvas.style.display = isEmpty ? "none" : "block";
  }

  if (emptyText) {
    emptyText.style.display = isEmpty ? "block" : "none";
  }
}

function updateDashboardEmptyStates() {
  const safeApplications = Array.isArray(applications) ? applications : [];
  const hasApplications = safeApplications.length > 0;
  const hasTrendData = safeApplications.some(a => a.appliedAt);

  setChartEmptyState("statusPieChart", "statusEmpty", !hasApplications);
  setChartEmptyState("jobBarChart", "jobEmpty", !hasApplications);
  setChartEmptyState("applicationTrendChart", "trendEmpty", !hasTrendData);
}

function renderDashboardCharts() {
  if (!dashboardPage || !dashboardPage.classList.contains("active")) return;

  updateDashboardEmptyStates();

  if (!Array.isArray(applications) || !applications.length) {
    statusPieChart = destroyChart(statusPieChart);
    jobBarChart = destroyChart(jobBarChart);
    applicationTrendChart = destroyChart(applicationTrendChart);
    return;
  }

  renderStatusPieChart();
  renderJobBarChart();
  renderApplicationTrendChart();
}

function destroyChart(chart) {
  if (chart) {
    chart.destroy();
  }
  return null;
}

function renderStatusPieChart() {
  statusPieChart = destroyChart(statusPieChart);

  const safeApplications = Array.isArray(applications) ? applications : [];

  const submitted = safeApplications.filter(a => a.status === "submitted").length;
  const reviewing = safeApplications.filter(a => a.status === "reviewing").length;
  const accepted = safeApplications.filter(a => a.status === "accepted").length;
  const rejected = safeApplications.filter(a => a.status === "rejected").length;

  const ctx = document.getElementById("statusPieChart");
  if (!ctx || typeof Chart === "undefined") return;

  statusPieChart = new Chart(ctx, {
    type: "doughnut",
    data: {
      labels: ["submitted", "reviewing", "accepted", "rejected"],
      datasets: [{
        data: [submitted, reviewing, accepted, rejected]
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: "bottom"
        }
      }
    }
  });
}

function renderJobBarChart() {
  jobBarChart = destroyChart(jobBarChart);

  const safeApplications = Array.isArray(applications) ? applications : [];
  const jobCountMap = {};

  safeApplications.forEach(a => {
    const title = getJobTitleById(a.jobId);
    jobCountMap[title] = (jobCountMap[title] || 0) + 1;
  });

  const ctx = document.getElementById("jobBarChart");
  if (!ctx || typeof Chart === "undefined") return;

  jobBarChart = new Chart(ctx, {
    type: "bar",
    data: {
      labels: Object.keys(jobCountMap),
      datasets: [{
        label: "Applications",
        data: Object.values(jobCountMap)
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            precision: 0
          }
        }
      }
    }
  });
}

function renderApplicationTrendChart() {
  applicationTrendChart = destroyChart(applicationTrendChart);

  const safeApplications = Array.isArray(applications) ? applications : [];
  const dateCountMap = {};

  safeApplications.forEach(a => {
    if (!a.appliedAt) return;
    const date = String(a.appliedAt).substring(0, 10);
    dateCountMap[date] = (dateCountMap[date] || 0) + 1;
  });

  const sortedDates = Object.keys(dateCountMap).sort();

  if (!sortedDates.length) {
    applicationTrendChart = destroyChart(applicationTrendChart);
    return;
  }

  const ctx = document.getElementById("applicationTrendChart");
  if (!ctx || typeof Chart === "undefined") return;

  applicationTrendChart = new Chart(ctx, {
    type: "line",
    data: {
      labels: sortedDates,
      datasets: [{
        label: "Applications",
        data: sortedDates.map(date => dateCountMap[date]),
        tension: 0.3
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: "bottom"
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            precision: 0
          }
        }
      }
    }
  });
}

async function refreshDashboard() {
  if (!ensureLoggedIn()) return;

  await loadJobs();

  let allApplications = [];

  const safeJobs = Array.isArray(jobs) ? jobs : [];

  for (const job of safeJobs) {
    const result = await apiFetch(`/applications/job/${job.jobId}`);

    if (result.code === 200 && Array.isArray(result.data)) {
      allApplications = allApplications.concat(result.data);
    }
  }

  applications = allApplications;
  renderDashboardCharts();
}

// Auto refresh

function startApplicationAutoRefresh() {
  stopApplicationAutoRefresh();

  applicationRefreshTimer = setInterval(async () => {
    const token = localStorage.getItem("employerToken");

    if (applicationsPage && applicationsPage.classList.contains("active") && token) {
      await loadApplications();
    }

    if (dashboardPage && dashboardPage.classList.contains("active") && token) {
      await refreshDashboard();
    }
  }, 8000);
}

function stopApplicationAutoRefresh() {
  if (applicationRefreshTimer) {
    clearInterval(applicationRefreshTimer);
    applicationRefreshTimer = null;
  }
}

// Messages

function renderMessages(list) {
  if (!Array.isArray(list)) list = [];

  if (!messagesList) return;

  if (!list.length) {
    messagesList.innerHTML = `
      <div class="card message-card">
        <div class="message-main">
          <div class="message-type">System Notification</div>
          <div class="message-title">No Messages</div>
          <div class="message-content">There are no new system notifications or application updates.</div>
        </div>
        <div class="message-time">-</div>
      </div>
    `;
    return;
  }

  messagesList.innerHTML = list.map(message => `
    <div class="card message-card">
      <div class="message-main">
        <div class="message-type">${message.type}</div>
        <div class="message-title">${message.title}</div>
        <div class="message-content">${message.content}</div>
      </div>
      <div class="message-time">${message.time}</div>
    </div>
  `).join("");
}

async function loadDynamicMessages() {
  if (!ensureLoggedIn()) return;

  await refreshDashboard();

  const storedMessages = JSON.parse(localStorage.getItem("messages") || "[]");

  const dynamicMessages = [];

  const safeApplications = Array.isArray(applications) ? applications : [];
  const safeJobs = Array.isArray(jobs) ? jobs : [];

  const submittedCount = safeApplications.filter(a => a.status === "submitted").length;
  const reviewingCount = safeApplications.filter(a => a.status === "reviewing").length;
  const activeJobs = safeJobs.filter(j => j.status === "active" || j.status === "approved").length;

  if (submittedCount > 0) {
    dynamicMessages.push({
      type: "Application Alert",
      title: "New applications to review",
      content: `There are currently ${submittedCount} submitted applications.`,
      time: "Today"
    });
  }

  if (reviewingCount > 0) {
    dynamicMessages.push({
      type: "Application Alert",
      title: "Applications under review",
      content: `There are currently ${reviewingCount} applications under review.`,
      time: "Today"
    });
  }

  dynamicMessages.push({
    type: "Job Updates",
    title: "Job status update",
    content: `There are currently ${activeJobs} active job postings.`,
    time: "Today"
  });

  renderMessages([...storedMessages, ...dynamicMessages]);
}
// Events

if (navVerification) {
  navVerification.addEventListener("click", async () => {
    showPage("verification");
    if (localStorage.getItem("employerToken")) await loadEmployerProfile();
  });
}

if (navJobs) {
  navJobs.addEventListener("click", async () => {
    showPage("jobs");
    if (localStorage.getItem("employerToken")) await loadJobs();
  });
}

if (navApplications) {
  navApplications.addEventListener("click", async () => {
    showPage("applications");
    if (localStorage.getItem("employerToken")) {
      await loadJobs();
      loadJobOptions();
      await loadApplications();
      startApplicationAutoRefresh();
    }
  });
}

if (navDashboard) {
  navDashboard.addEventListener("click", async () => {
    showPage("dashboard");
    if (localStorage.getItem("employerToken")) {
      await refreshDashboard();
      startApplicationAutoRefresh();
    }
  });
}

if (navMessages) {
  navMessages.addEventListener("click", async () => {
    showPage("messages");
    await loadDynamicMessages();
  });
}

const applicationSearchBtn = document.getElementById("applicationSearchBtn");
if (applicationSearchBtn) {
  applicationSearchBtn.addEventListener("click", loadApplications);
}

const applicationJobSelectEl = document.getElementById("applicationJobSelect");
if (applicationJobSelectEl) {
  applicationJobSelectEl.addEventListener("change", loadApplications);
}

const applicationStatusFilter = document.getElementById("applicationStatusFilter");
if (applicationStatusFilter) {
  applicationStatusFilter.addEventListener("change", loadApplications);
}

if (applicationStudentSearch) {
  applicationStudentSearch.addEventListener("input", loadApplications);
}

const goEditProfileBtn = document.getElementById("goEditProfileBtn");
if (goEditProfileBtn) {
  goEditProfileBtn.addEventListener("click", async () => {
    if (!ensureLoggedIn()) return;
    await loadEmployerProfile();
    showPage("editProfile");
  });
}

const saveProfileBtn = document.getElementById("saveProfileBtn");
if (saveProfileBtn) {
  saveProfileBtn.addEventListener("click", saveEmployerProfile);
}

const cancelEditProfileBtn = document.getElementById("cancelEditProfileBtn");
if (cancelEditProfileBtn) {
  cancelEditProfileBtn.addEventListener("click", () => showPage("verification"));
}

const goCreateJobBtn = document.getElementById("goCreateJobBtn");
if (goCreateJobBtn) {
  goCreateJobBtn.addEventListener("click", () => {
    setCreateMode();
    fillCreateFormDefault();
    showPage("createJob");
  });
}

const cancelCreateBtn = document.getElementById("cancelCreateBtn");
if (cancelCreateBtn) {
  cancelCreateBtn.addEventListener("click", () => {
    setCreateMode();
    showPage("jobs");
  });
}

const backToJobsBtn = document.getElementById("backToJobsBtn");
if (backToJobsBtn) {
  backToJobsBtn.addEventListener("click", () => {
    setCreateMode();
    showPage("jobs");
  });
}

if (publishJobBtn) {
  publishJobBtn.addEventListener("click", publishJob);
}

if (updateJobBtn) {
  updateJobBtn.addEventListener("click", updateJob);
}

const jobSearchBtn = document.getElementById("jobSearchBtn");
if (jobSearchBtn) {
  jobSearchBtn.addEventListener("click", loadJobs);
}

const loginBtn = document.getElementById("loginBtn");
if (loginBtn) {
  loginBtn.textContent = "Log Out";

  loginBtn.addEventListener("click", () => {
    localStorage.removeItem("employerToken");
    window.location.href = "/admin/login.html";
  });
}
// Initialization

renderJobs([]);
renderApplications([]);
renderMessages([]);
fillCreateFormDefault();
setCreateMode();

employerToken = localStorage.getItem("employerToken") || "";

setLoginStatus(Boolean(employerToken));

if (employerToken) {
  loadEmployerProfile();

  setInterval(() => {
    const token = localStorage.getItem("employerToken");

    if (!token) {
      setLoginStatus(false);
      return;
    }

    loadEmployerProfile();
  }, 5000);
}
