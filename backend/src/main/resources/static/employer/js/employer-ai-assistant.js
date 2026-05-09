(function () {
    const API_BASE_URL = "http://localhost:8080";
    const STORAGE_KEY = "employerAiChatHistory";
    const WELCOME_MESSAGE = "Hi! I can connect to backend AI services for resume screening, candidate recommendations, job description improvement, and hiring analytics.";

    const assistantHTML = `
    <button class="employer-ai-button" id="employerAiButton" title="Employer AI Assistant">
      <img src="images/robot.png" alt="AI Assistant">
    </button>

    <div class="employer-ai-window" id="employerAiWindow">
      <div class="employer-ai-header">
        <span>Employer AI Assistant</span>
        <button class="employer-ai-close" id="employerAiClose">×</button>
      </div>

      <div class="employer-ai-messages" id="employerAiMessages"></div>

      <div class="employer-ai-actions">
        <button data-question="Screen resumes for job 1">Resume Screening</button>
        <button data-question="Recommend candidates for job 1">Candidate Recommendation</button>
        <button data-question="Improve job description">Improve Job Description</button>
        <button data-question="Show hiring analytics">Hiring Analytics</button>
        <button data-clear="true">Clear</button>
      </div>

      <div class="employer-ai-input-area">
        <input id="employerAiInput" type="text" placeholder="Ask Employer AI..." />
        <button id="employerAiSend">Send</button>
      </div>
    </div>
  `;

    document.body.insertAdjacentHTML("beforeend", assistantHTML);

    const aiButton = document.getElementById("employerAiButton");
    const aiWindow = document.getElementById("employerAiWindow");
    const aiClose = document.getElementById("employerAiClose");
    const aiMessages = document.getElementById("employerAiMessages");
    const aiInput = document.getElementById("employerAiInput");
    const aiSend = document.getElementById("employerAiSend");

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

    document.querySelectorAll(".employer-ai-actions button").forEach(function (button) {
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
            loading.innerHTML = `<strong>Connection failed</strong><br>${escapeHtml(error.message)}<br><br>Please make sure Spring Boot is running on port 8080 and you have logged in as an employer.`;
            saveChatHistory();
        }
    }

    function addMessage(text, sender, shouldSave) {
        const messageElement = document.createElement("div");
        messageElement.className = "employer-ai-message " + sender;
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
        const messages = Array.from(aiMessages.querySelectorAll(".employer-ai-message")).map(function (el) {
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

        if (lowerMessage.includes("analytics") || lowerMessage.includes("trend") || lowerMessage.includes("dashboard") || lowerMessage.includes("分析") || lowerMessage.includes("数据")) {
            const data = await apiGet("/ai/employer/hiring-analytics");
            return renderHiringAnalytics(data);
        }

        if (lowerMessage.includes("description") || lowerMessage.includes("job post") || lowerMessage.includes("improve") || lowerMessage.includes("岗位描述") || lowerMessage.includes("优化")) {
            const body = collectJobDescriptionInput(message);
            const data = await apiPost("/ai/employer/job-description/improve", body);
            return renderImprovedDescription(data);
        }

        if (lowerMessage.includes("resume") || lowerMessage.includes("screen") || lowerMessage.includes("cv") || lowerMessage.includes("简历") || lowerMessage.includes("筛选")) {
            const jobId = getJobIdFromTextOrPage(message);
            if (!jobId) return "<strong>AI Resume Screening</strong><br>Please type a job id, for example: <strong>screen resumes for job 1</strong>.";
            const data = await apiGet(`/ai/employer/jobs/${jobId}/ranked-candidates`);
            return renderRankedCandidates(data);
        }

        if (lowerMessage.includes("candidate") || lowerMessage.includes("recommend") || lowerMessage.includes("student") || lowerMessage.includes("候选人") || lowerMessage.includes("推荐")) {
            const jobId = getJobIdFromTextOrPage(message);
            if (!jobId) return "<strong>AI Candidate Recommendation</strong><br>Please type a job id, for example: <strong>recommend candidates for job 1</strong>.";
            const data = await apiGet(`/ai/employer/jobs/${jobId}/recommended-students`);
            return renderRecommendedStudents(data);
        }

        return `
          I can call backend AI APIs for:<br>
          • <strong>Show hiring analytics</strong><br>
          • <strong>Improve job description</strong><br>
          • <strong>Screen resumes for job 1</strong><br>
          • <strong>Recommend candidates for job 1</strong>
        `;
    }

    async function apiGet(path) { return apiRequest(path, { method: "GET" }); }

    async function apiPost(path, body) {
        return apiRequest(path, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });
    }

    async function apiRequest(path, options) {
        const token = localStorage.getItem("employerToken") || localStorage.getItem("token") || "";
        const headers = Object.assign({}, options.headers || {});
        if (token) headers.Authorization = "Bearer " + token;
        const response = await fetch(API_BASE_URL + path, Object.assign({}, options, { headers }));
        const result = await response.json();
        if (!response.ok || result.code !== 200) throw new Error(result.message || "Backend request failed.");
        return result.data;
    }

    function getJobIdFromTextOrPage(text) {
        const match = text.match(/\d+/);
        if (match) return match[0];
        const selectedJob = document.querySelector("#applicationJobFilter, #jobSelect, select[name='jobId']");
        if (selectedJob && selectedJob.value && selectedJob.value !== "all") return selectedJob.value;
        return null;
    }

    function collectJobDescriptionInput(message) {
        const titleInput = document.querySelector("#jobTitle, #title, input[name='title']");
        const descriptionInput = document.querySelector("#jobDescription, #description, textarea[name='description']");
        const requirementsInput = document.querySelector("#jobRequirements, #requirements, textarea[name='requirements']");
        return {
            title: titleInput ? titleInput.value : "Student Job Position",
            description: descriptionInput ? descriptionInput.value : message,
            requirements: requirementsInput ? requirementsInput.value : "communication, teamwork, relevant skills"
        };
    }

    function renderHiringAnalytics(data) {
        return `
          <strong>AI Hiring Analytics</strong><br>
          Total jobs: ${data.totalJobs}<br>
          Approved jobs: ${data.approvedJobs}<br>
          Closed jobs: ${data.closedJobs}<br>
          Total applicants: ${data.totalApplicants}<br>
          Average applicants per job: ${Number(data.averageApplicantsPerJob || 0).toFixed(2)}<br><br>
          <strong>Job analytics:</strong><br>
          ${(data.jobAnalytics || []).map(j => `• ${escapeHtml(j.title)}: ${j.numberOfApplicants} applicants, ${j.bestFitCandidates} best-fit candidates`).join("<br>") || "No job data."}
        `;
    }

    function renderRankedCandidates(data) {
        const candidates = data.topCandidates || [];
        return `
          <strong>AI Resume Screening</strong><br>
          Job: ${escapeHtml(data.jobTitle)}<br>
          Total applicants: ${data.totalApplicants}<br><br>
          ${candidates.map((c, i) => `${i + 1}. ${escapeHtml(c.studentName || "Student")} - <strong>${c.matchScore}%</strong> (${escapeHtml(c.matchLevel)})<br>${renderList(c.whyMatch)}`).join("<br><br>") || "No applicants found."}
        `;
    }

    function renderRecommendedStudents(data) {
        const students = data.recommendedStudents || [];
        return `
          <strong>AI Candidate Recommendation</strong><br>
          Job: ${escapeHtml(data.jobTitle)}<br><br>
          ${students.map((s, i) => `${i + 1}. ${escapeHtml(s.studentName || "Student")} - <strong>${s.matchScore}%</strong> (${escapeHtml(s.matchLevel)})<br>Skills: ${escapeHtml(s.skills)}`).join("<br><br>") || "No recommended students found."}
        `;
    }

    function renderImprovedDescription(data) {
        return `
          <strong>AI Job Description Assistant</strong><br>
          <strong>Improved description:</strong><br>${escapeHtml(data.improvedDescription)}<br><br>
          <strong>Recommended keywords:</strong><br>${renderList(data.recommendedKeywords)}<br><br>
          <strong>Suggestions:</strong><br>${renderList(data.suggestions)}
        `;
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
