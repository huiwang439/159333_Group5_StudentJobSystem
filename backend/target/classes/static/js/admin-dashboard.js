const menuToggleBtn = document.getElementById("menuToggleBtn");
const sidebar = document.getElementById("sidebar");

const totalUsers = document.getElementById("totalUsers");
const totalStudents = document.getElementById("totalStudents");
const totalEmployers = document.getElementById("totalEmployers");
const totalJobs = document.getElementById("totalJobs");
const totalApplications = document.getElementById("totalApplications");
const newJobsToday = document.getElementById("newJobsToday");
const newApplicationsToday = document.getElementById("newApplicationsToday");
const activeUsers = document.getElementById("activeUsers");

const mockDashboardData = {
    totalUsers: 120,
    totalStudents: 78,
    totalEmployers: 42,
    totalJobs: 96,
    totalApplications: 183,
    newJobsToday: 8,
    newApplicationsToday: 15,
    activeUsers: 64
};

function toggleSidebar() {
    sidebar.classList.toggle("hidden");
    menuToggleBtn.textContent = sidebar.classList.contains("hidden") ? "▶" : "◀";
}

function renderDashboard() {
    totalUsers.textContent = mockDashboardData.totalUsers;
    totalStudents.textContent = mockDashboardData.totalStudents;
    totalEmployers.textContent = mockDashboardData.totalEmployers;
    totalJobs.textContent = mockDashboardData.totalJobs;
    totalApplications.textContent = mockDashboardData.totalApplications;
    newJobsToday.textContent = mockDashboardData.newJobsToday;
    newApplicationsToday.textContent = mockDashboardData.newApplicationsToday;
    activeUsers.textContent = mockDashboardData.activeUsers;
}

menuToggleBtn.addEventListener("click", toggleSidebar);

renderDashboard();