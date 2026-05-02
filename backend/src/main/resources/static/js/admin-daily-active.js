const menuToggleBtn = document.getElementById("menuToggleBtn");
const sidebar = document.getElementById("sidebar");
const todayActiveUsers = document.getElementById("todayActiveUsers");
const studentActiveUsers = document.getElementById("studentActiveUsers");
const employerActiveUsers = document.getElementById("employerActiveUsers");
const dailyActiveTableBody = document.getElementById("dailyActiveTableBody");
const messageBox = document.getElementById("messageBox");

const mockDailyActiveData = {
    todayActiveUsers: 126,
    studentActiveUsers: 84,
    employerActiveUsers: 42,
    hourlyStats: [
        { timeRange: "00:00 - 06:00", activeCount: 12 },
        { timeRange: "06:00 - 12:00", activeCount: 38 },
        { timeRange: "12:00 - 18:00", activeCount: 47 },
        { timeRange: "18:00 - 24:00", activeCount: 29 }
    ]
};

function showMessage(message) {
    messageBox.textContent = message;
}

function renderCards() {
    todayActiveUsers.textContent = mockDailyActiveData.todayActiveUsers;
    studentActiveUsers.textContent = mockDailyActiveData.studentActiveUsers;
    employerActiveUsers.textContent = mockDailyActiveData.employerActiveUsers;
}

function renderTable() {
    dailyActiveTableBody.innerHTML = "";

    mockDailyActiveData.hourlyStats.forEach(item => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${item.timeRange}</td>
            <td>${item.activeCount}</td>
        `;
        dailyActiveTableBody.appendChild(tr);
    });
}

function loadDailyActive() {
    renderCards();
    renderTable();
    showMessage("Daily active data loaded.");
}

loadDailyActive();