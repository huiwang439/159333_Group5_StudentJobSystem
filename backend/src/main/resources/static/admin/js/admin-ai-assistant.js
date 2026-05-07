(function () {
    const API_BASE_URL = "http://localhost:8080";
    const STORAGE_KEY = "adminAiChatHistory";
    const WELCOME_MESSAGE = "Hi! I can connect to backend AI services for job moderation, fraud detection, platform analytics, and monthly reports.";

    const assistantHTML = `
    <button class="admin-ai-button" id="adminAiButton" title="Admin AI Assistant">
      <img src="images/robot.png" alt="AI Assistant">
    </button>

    <div class="admin-ai-window" id="adminAiWindow">
      <div class="admin-ai-header">
        <span>Admin AI Assistant</span>
        <button class="admin-ai-close" id="adminAiClose">×</button>
      </div>

      <div class="admin-ai-messages" id="adminAiMessages"></div>

      <div class="admin-ai-actions">
        <button data-question="moderate job 1">Job Moderation</button>
        <button data-question="Detect fraud">Fraud Detection</button>
        <button data-question="Show platform analytics">Platform Analytics</button>
        <button data-question="Generate monthly report">Auto Report</button>
        <button data-clear="true">Clear</button>
      </div>

      <div class="admin-ai-input-area">
        <input id="adminAiInput" type="text" placeholder="Ask Admin AI..." />
        <button id="adminAiSend">Send</button>
      </div>
    </div>
  `;

    document.body.insertAdjacentHTML("beforeend", assistantHTML);

    const aiButton = document.getElementById("adminAiButton");
    const aiWindow = document.getElementById("adminAiWindow");
    const aiClose = document.getElementById("adminAiClose");
    const aiMessages = document.getElementById("adminAiMessages");
    const aiInput = document.getElementById("adminAiInput");
    const aiSend = document.getElementById("adminAiSend");

    loadChatHistory();

    aiButton.addEventListener("click", function () {
        aiWindow.classList.toggle("open");
        aiMessages.scrollTop = aiMessages.scrollHeight;
    });

    aiClose.addEventListener("click", function () {
        aiWindow.classList.remove("open");
        saveChatHistory();
    });

    aiSend.addEventListener("click", sendMessage);

    aiInput.addEventListener("keydown", function (event) {
        if (event.key === "Enter") sendMessage();
    });

    document.querySelectorAll(".admin-ai-actions button").forEach(function (button) {
        button.addEventListener("click", function () {
            if (button.dataset.clear === "true") {
                clearChatHistory();
                return;
            }
            handleQuestion(button.getAttribute("data-question"));
        });
    });

    function sendMessage() {
        const message = aiInput.value.trim();
        if (!message) return;
        aiInput.value = "";
        handleQuestion(message);
    }

    async function handleQuestion(message) {
        addMessage(escapeHtml(message), "user", true);
        const loading = addMessage("AI is loading data from backend...", "bot", true);

        try {
            const responseHtml = await getBackendAIResponse(message);
            loading.innerHTML = responseHtml;
            saveChatHistory();
        } catch (error) {
            loading.innerHTML = `<strong>Connection failed</strong><br>${escapeHtml(error.message)}<br><br>Please make sure Spring Boot is running on port 8080 and you have logged in as an admin.`;
            saveChatHistory();
        }
    }

    function addMessage(text, sender, shouldSave) {
        const messageElement = document.createElement("div");
        messageElement.className = "admin-ai-message " + sender;
        messageElement.innerHTML = text;
        aiMessages.appendChild(messageElement);
        aiMessages.scrollTop = aiMessages.scrollHeight;
        if (shouldSave) saveChatHistory();
        return messageElement;
    }

    function loadChatHistory() {
        const saved = localStorage.getItem(STORAGE_KEY);
        if (!saved) {
            addMessage(escapeHtml(WELCOME_MESSAGE), "bot", true);
            return;
        }
        try {
            const messages = JSON.parse(saved);
            aiMessages.innerHTML = "";
            messages.forEach(function (item) { addMessage(item.html, item.sender, false); });
            if (messages.length === 0) addMessage(escapeHtml(WELCOME_MESSAGE), "bot", true);
        } catch (e) {
            localStorage.removeItem(STORAGE_KEY);
            addMessage(escapeHtml(WELCOME_MESSAGE), "bot", true);
        }
    }

    function saveChatHistory() {
        const messages = Array.from(aiMessages.querySelectorAll(".admin-ai-message")).map(function (el) {
            return { sender: el.classList.contains("user") ? "user" : "bot", html: el.innerHTML };
        });
        localStorage.setItem(STORAGE_KEY, JSON.stringify(messages));
    }

    function clearChatHistory() {
        localStorage.removeItem(STORAGE_KEY);
        aiMessages.innerHTML = "";
        addMessage(escapeHtml(WELCOME_MESSAGE), "bot", true);
    }

    async function getBackendAIResponse(message) {
        const lowerMessage = message.toLowerCase();

        if (lowerMessage.includes("fraud") || lowerMessage.includes("spam") || lowerMessage.includes("user") || lowerMessage.includes("异常") || lowerMessage.includes("虚假") || lowerMessage.includes("用户")) {
            const data = await apiGet("/ai/admin/fraud-detection");
            return renderFraudDetection(data);
        }

        if (lowerMessage.includes("analytics") || lowerMessage.includes("platform") || lowerMessage.includes("dashboard") || lowerMessage.includes("trend") || lowerMessage.includes("分析") || lowerMessage.includes("数据")) {
            const data = await apiGet("/ai/admin/platform-analytics");
            return renderPlatformAnalytics(data);
        }

        if (lowerMessage.includes("report") || lowerMessage.includes("monthly") || lowerMessage.includes("generate") || lowerMessage.includes("报告") || lowerMessage.includes("生成")) {
            const data = await apiGet("/ai/admin/monthly-report");
            return renderMonthlyReport(data);
        }

        if (lowerMessage.includes("moderation") || lowerMessage.includes("job") || lowerMessage.includes("审核") || lowerMessage.includes("岗位")) {
            const jobId = getJobIdFromTextOrPage(message);
            if (!jobId) return "<strong>AI Job Moderation</strong><br>Please type a job id, for example: <strong>moderate job 1</strong>.";
            const data = await apiGet(`/ai/admin/jobs/${jobId}/moderation`);
            return renderJobModeration(data);
        }

        return `
          I can call backend AI APIs for:<br>
          • <strong>Detect fraud</strong><br>
          • <strong>Show platform analytics</strong><br>
          • <strong>Generate monthly report</strong><br>
          • <strong>Moderate job 1</strong>
        `;
    }

    async function apiGet(path) {
        const token = localStorage.getItem("adminToken") || localStorage.getItem("token") || "";
        const response = await fetch(API_BASE_URL + path, {
            method: "GET",
            headers: token ? { Authorization: "Bearer " + token } : {}
        });
        const result = await response.json();
        if (!response.ok || result.code !== 200) throw new Error(result.message || "Backend request failed.");
        return result.data;
    }

    function getJobIdFromTextOrPage(text) {
        const match = text.match(/\d+/);
        if (match) return match[0];
        const params = new URLSearchParams(window.location.search);
        return params.get("jobId") || params.get("id");
    }

    function renderJobModeration(data) {
        return `
          <strong>AI Job Moderation</strong><br>
          Job: ${escapeHtml(data.title)}<br>
          Risk Score: <strong>${data.riskScore}</strong><br>
          Risk Level: <strong>${escapeHtml(data.riskLevel)}</strong><br>
          Recommendation: ${escapeHtml(data.recommendation)}<br><br>
          <strong>Flags:</strong><br>${renderList(data.flags)}
        `;
    }

    function renderFraudDetection(data) {
        return `
          <strong>AI Fraud Detection</strong><br>
          Risky users: ${data.totalRiskyUsers}<br>
          Risky jobs: ${data.totalRiskyJobs}<br><br>
          <strong>Risky users:</strong><br>
          ${(data.riskyUsers || []).slice(0, 5).map(u => `• ${escapeHtml(u.name)} (${escapeHtml(u.role)}) - ${escapeHtml(u.riskLevel)} risk`).join("<br>") || "No risky users found."}<br><br>
          <strong>Risky jobs:</strong><br>
          ${(data.riskyJobs || []).slice(0, 5).map(j => `• ${escapeHtml(j.title)} - ${escapeHtml(j.riskLevel)} risk`).join("<br>") || "No risky jobs found."}
        `;
    }

    function renderPlatformAnalytics(data) {
        return `
          <strong>AI Platform Analytics</strong><br>
          Total jobs: ${data.totalJobs}<br>
          Approved jobs: ${data.approvedJobs}<br>
          Pending jobs: ${data.pendingJobs}<br>
          Closed jobs: ${data.closedJobs}<br>
          Total students: ${data.totalStudents}<br>
          Total applications: ${data.totalApplications}<br>
          Application success rate: ${data.applicationSuccessRate}%<br><br>
          <strong>Popular industries:</strong><br>${renderObject(data.popularIndustries)}
        `;
    }

    function renderMonthlyReport(data) {
        return `
          <strong>AI Auto Report</strong><br>
          Month: ${escapeHtml(data.month)}<br>
          New jobs: ${data.newJobs}<br>
          Monthly applications: ${data.monthlyApplications}<br><br>
          <pre style="white-space:pre-wrap;margin:0;">${escapeHtml(data.reportContent)}</pre>
        `;
    }

    function renderList(items) {
        if (!items || items.length === 0) return "None";
        return items.map(item => `• ${escapeHtml(String(item))}`).join("<br>");
    }

    function renderObject(obj) {
        if (!obj || Object.keys(obj).length === 0) return "None";
        return Object.entries(obj).map(([key, value]) => `• ${escapeHtml(key)}: ${value}`).join("<br>");
    }

    function escapeHtml(value) {
        return String(value ?? "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
})();
