(function () {
    const API_BASE_URL = "http://localhost:8080";
    const STORAGE_KEY = "studentAiChatHistory";
    const WELCOME_MESSAGE = "Hi! I can help with job fit analysis, job recommendations, alerts, resume improvement, and success prediction.";

    const assistantHTML = `
    <button class="student-ai-button" id="studentAiButton" title="Student Assistant">
      <img src="./images/robot.png" alt="Student Assistant">
    </button>

    <div class="student-ai-window" id="studentAiWindow">
      <div class="student-ai-header">
        <span>Student Assistant</span>
        <button class="student-ai-close" id="studentAiClose">×</button>
      </div>

      <div class="student-ai-messages" id="studentAiMessages"></div>

      <div class="student-ai-actions">
        <button data-question="Job fit analysis">Job Fit</button>
        <button data-question="Recommend jobs">Recommend Jobs</button>
        <button data-question="Job alert">Job Alert</button>
        <button data-question="Resume improvement">Resume Tips</button>
        <button data-question="Success prediction">Success Rate</button>
        <button data-clear="true">Clear</button>
      </div>

      <div class="student-ai-input-area">
        <input id="studentAiInput" type="text" placeholder="Ask Student Assistant..." />
        <button id="studentAiSend">Send</button>
      </div>
    </div>
  `;

    document.body.insertAdjacentHTML("beforeend", assistantHTML);

    const aiButton = document.getElementById("studentAiButton");
    const aiWindow = document.getElementById("studentAiWindow");
    const aiClose = document.getElementById("studentAiClose");
    const aiMessages = document.getElementById("studentAiMessages");
    const aiInput = document.getElementById("studentAiInput");
    const aiSend = document.getElementById("studentAiSend");

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

    document.querySelectorAll(".student-ai-actions button").forEach(function (button) {
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
        const loading = addMessage("Assistant is loading data from backend...", "bot", true);

        try {
            const responseHtml = await getBackendAIResponse(message);
            loading.innerHTML = responseHtml;
            saveChatHistory();
        } catch (error) {
            loading.innerHTML = `<strong>Connection failed</strong><br>${escapeHtml(error.message)}<br><br>Please make sure Spring Boot is running on port 8080 and you have logged in as a student.`;
            saveChatHistory();
        }
    }

    function addMessage(text, sender, shouldSave) {
        const messageElement = document.createElement("div");
        messageElement.className = "student-ai-message " + sender;
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
            messages.forEach(function (item) {
                addMessage(item.html, item.sender, false);
            });
            if (messages.length === 0) addMessage(escapeHtml(WELCOME_MESSAGE), "bot", true);
        } catch (e) {
            localStorage.removeItem(STORAGE_KEY);
            addMessage(escapeHtml(WELCOME_MESSAGE), "bot", true);
        }
    }

    function saveChatHistory() {
        const messages = Array.from(aiMessages.querySelectorAll(".student-ai-message")).map(function (el) {
            return {
                sender: el.classList.contains("user") ? "user" : "bot",
                html: el.innerHTML
            };
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

        if (lowerMessage.includes("recommend")) {
            const data = await apiGet("/ai/jobs/recommendations");
            return renderRecommendations(data);
        }

        if (lowerMessage.includes("alert") || lowerMessage.includes("notification")) {
            const data = await apiGet("/ai/jobs/alerts");
            return renderAlerts(data);
        }

        if (lowerMessage.includes("resume") || lowerMessage.includes("cv")) {
            const data = await apiGet("/ai/resume-improvement");
            return renderResumeSuggestions(data);
        }

        if (lowerMessage.includes("fit") || lowerMessage.includes("match") || lowerMessage.includes("success") || lowerMessage.includes("probability") || lowerMessage.includes("rate")) {
            const jobId = getJobIdFromPageOrText(message);
            if (!jobId) {
                return "<strong>Job Fit Analysis</strong><br>Please open a job detail page or type a job id, for example: <strong>fit job 1</strong>.";
            }
            const data = await apiGet(`/ai/jobs/${jobId}/fit`);
            return renderJobFit(data);
        }

        return `
          I can call backend AI APIs for:<br>
          • <strong>Recommend jobs</strong><br>
          • <strong>Job alert</strong><br>
          • <strong>Resume improvement</strong><br>
          • <strong>Job fit analysis</strong> / <strong>Success prediction</strong><br><br>
          For job fit, open a job detail page or type: <strong>fit job 1</strong>.
        `;
    }

    async function apiGet(path) {
        const token = localStorage.getItem("token") || localStorage.getItem("studentToken") || "";
        const response = await fetch(API_BASE_URL + path, {
            method: "GET",
            headers: token ? { Authorization: "Bearer " + token } : {}
        });
        const result = await response.json();
        if (!response.ok || result.code !== 200) throw new Error(result.message || "Backend request failed.");
        return result.data;
    }

    function getJobIdFromPageOrText(text) {
        const params = new URLSearchParams(window.location.search);
        const idFromUrl = params.get("jobId") || params.get("id");
        if (idFromUrl) return idFromUrl;
        const match = text.match(/\d+/);
        return match ? match[0] : null;
    }

    function renderJobFit(data) {
        return `
          <strong>Job Fit Analysis</strong><br>
          Job: ${escapeHtml(data.jobTitle)}<br>
          Match Score: <strong>${data.matchScore}%</strong><br>
          Match Level: <strong>${escapeHtml(data.matchLevel)}</strong><br>
          Success Probability: <strong>${data.successProbability}%</strong><br><br>
          <strong>Why match:</strong><br>${renderList(data.whyMatch)}<br>
          <strong>Suggestions:</strong><br>${renderList(data.suggestions)}
        `;
    }

    function renderRecommendations(data) {
        const jobs = data.recommendedJobs || [];
        if (jobs.length === 0) return "<strong>Job Recommendation</strong><br>No approved matching jobs found.";
        return `
          <strong>Job Recommendation</strong><br>
          ${jobs.map((job, index) => `${index + 1}. ${escapeHtml(job.title)} - <strong>${job.matchScore}%</strong> (${escapeHtml(job.matchLevel)})<br>Location: ${escapeHtml(job.location)}<br>`).join("<br>")}
          <strong>Resume suggestions:</strong><br>${renderList(data.resumeSuggestions)}
        `;
    }

    function renderAlerts(data) {
        const alerts = data.alerts || [];
        if (alerts.length === 0) return "<strong>Job Alert</strong><br>No matching job alerts right now.";
        return `
          <strong>Job Alert</strong><br>
          Total alerts: ${data.totalAlerts}<br><br>
          ${alerts.map(alert => `• ${escapeHtml(alert.title)} - ${alert.matchScore}% match<br>${escapeHtml(alert.message)}<br>`).join("<br>")}
        `;
    }

    function renderResumeSuggestions(data) {
        return `<strong>Resume Improvement</strong><br>${renderList(data)}`;
    }

    function renderList(items) {
        if (!items || items.length === 0) return "None";
        return items.map(item => `• ${escapeHtml(String(item))}`).join("<br>");
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
