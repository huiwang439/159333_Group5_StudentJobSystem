document.addEventListener("DOMContentLoaded", async () => {
    if (!requireRole("admin")) return;

    const todayActiveUsers = document.getElementById("todayActiveUsers");
    const studentActiveUsers = document.getElementById("studentActiveUsers");
    const employerActiveUsers = document.getElementById("employerActiveUsers");
    const dailyActiveTableBody = document.getElementById("dailyActiveTableBody");
    const messageBox = document.getElementById("messageBox");

    function showMessage(message, isError = false) {
        messageBox.textContent = message || "";
        messageBox.style.color = isError ? "#d93025" : "#2b7a0b";
    }

    function renderHourly(data) {
        dailyActiveTableBody.innerHTML = "";

        if (!data || data.length === 0) {
            dailyActiveTableBody.innerHTML = `<tr><td colspan="3">No hourly data found</td></tr>`;
            return;
        }

        data.forEach(item => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${String(item.hour).padStart(2, "0")}:00 - ${String(Number(item.hour) + 1).padStart(2, "0")}:00</td>
                <td>${item.activeUsers ?? 0}</td>
                <td>${item.logCount ?? 0}</td>
            `;
            dailyActiveTableBody.appendChild(tr);
        });
    }

    try {
        showMessage("Loading daily active data...");
        const [activeToday, hourly] = await Promise.all([
            apiGet("/admin/analytics/active-today"),
            apiGet("/admin/analytics/hourly-active")
        ]);

        todayActiveUsers.textContent = activeToday.activeUsers ?? 0;
        studentActiveUsers.textContent = activeToday.activeStudents ?? 0;
        employerActiveUsers.textContent = activeToday.activeEmployers ?? 0;

        renderHourly(hourly);
        showMessage("Daily active data loaded.");
    } catch (error) {
        showMessage(error.message, true);
    }
});