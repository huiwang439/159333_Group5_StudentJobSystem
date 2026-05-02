(function () {
    const assistantHTML = `
    <button class="ai-chat-button" id="aiChatButton" title="AI Assistant">🤖</button>

    <div class="ai-chat-window" id="aiChatWindow">
      <div class="ai-chat-header">
        <span>AI Student Assistant</span>
        <button class="ai-chat-close" id="aiChatClose">×</button>
      </div>

      <div class="ai-chat-messages" id="aiChatMessages">
        <div class="ai-message bot">
          Hi! I am your AI assistant. You can ask me about job matching, job recommendations, resume improvement, job alerts, or application success prediction.
        </div>
      </div>

      <div class="ai-quick-actions">
        <button data-question="Recommend jobs for me">Job Recommendation</button>
        <button data-question="Analyse job fit">Job Fit</button>
        <button data-question="Give resume suggestions">Resume Tips</button>
        <button data-question="Predict my application success">Success Prediction</button>
        <button data-question="Show job alerts">Job Alerts</button>
      </div>

      <div class="ai-chat-input-area">
        <input id="aiChatInput" type="text" placeholder="Ask AI Assistant..." />
        <button id="aiChatSend">Send</button>
      </div>
    </div>
  `;

    document.body.insertAdjacentHTML("beforeend", assistantHTML);

    const chatButton = document.getElementById("aiChatButton");
    const chatWindow = document.getElementById("aiChatWindow");
    const chatClose = document.getElementById("aiChatClose");
    const chatMessages = document.getElementById("aiChatMessages");
    const chatInput = document.getElementById("aiChatInput");
    const chatSend = document.getElementById("aiChatSend");

    chatButton.addEventListener("click", function () {
        chatWindow.classList.toggle("open");
    });

    chatClose.addEventListener("click", function () {
        chatWindow.classList.remove("open");
    });

    chatSend.addEventListener("click", sendMessage);

    chatInput.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            sendMessage();
        }
    });

    document.querySelectorAll(".ai-quick-actions button").forEach(function (button) {
        button.addEventListener("click", function () {
            const question = button.getAttribute("data-question");
            addMessage(question, "user");
            setTimeout(function () {
                addMessage(generateAIResponse(question), "bot");
            }, 400);
        });
    });

    function sendMessage() {
        const message = chatInput.value.trim();

        if (!message) {
            return;
        }

        addMessage(message, "user");
        chatInput.value = "";

        setTimeout(function () {
            const response = generateAIResponse(message);
            addMessage(response, "bot");
        }, 400);
    }

    function addMessage(text, sender) {
        const messageElement = document.createElement("div");
        messageElement.className = "ai-message " + sender;
        messageElement.innerHTML = text;
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function generateAIResponse(message) {
        const lowerMessage = message.toLowerCase();

        if (
            lowerMessage.includes("recommend") ||
            lowerMessage.includes("推荐") ||
            lowerMessage.includes("job recommendation")
        ) {
            return `
        <strong>AI Job Recommendation</strong><br>
        Based on your academic background, skills, and preferred location, I suggest checking:<br>
        1. Software Developer Internship<br>
        2. Data Analyst Intern<br>
        3. Web Developer Assistant<br><br>
        Tip: Update your profile skills to improve recommendation accuracy.
      `;
        }

        if (
            lowerMessage.includes("fit") ||
            lowerMessage.includes("match") ||
            lowerMessage.includes("匹配")
        ) {
            return `
        <strong>AI Job Fit Analysis</strong><br>
        Estimated Match Score: <strong>78%</strong><br>
        Match Level: <strong>Medium to High</strong><br><br>
        Why it matches:<br>
        • Your study background is related to this job.<br>
        • Your profile contains relevant technical skills.<br>
        • The job location is suitable.<br><br>
        Suggestion: Add more project experience to increase your match score.
      `;
        }

        if (
            lowerMessage.includes("resume") ||
            lowerMessage.includes("cv") ||
            lowerMessage.includes("简历")
        ) {
            return `
        <strong>AI Resume Improvement Suggestions</strong><br>
        • Add more specific technical skills, such as Java, HTML, CSS, JavaScript, and MySQL.<br>
        • Include at least one project experience.<br>
        • Use action verbs such as developed, designed, implemented, and tested.<br>
        • Keep your resume clear and job-focused.
      `;
        }

        if (
            lowerMessage.includes("success") ||
            lowerMessage.includes("probability") ||
            lowerMessage.includes("成功率")
        ) {
            return `
        <strong>AI Application Success Prediction</strong><br>
        Estimated Success Probability: <strong>70%</strong><br><br>
        This is based on your profile completeness, skill relevance, and job requirements.<br>
        To improve your chance, tailor your cover letter and highlight matching skills.
      `;
        }

        if (
            lowerMessage.includes("alert") ||
            lowerMessage.includes("notification") ||
            lowerMessage.includes("提醒") ||
            lowerMessage.includes("通知")
        ) {
            return `
        <strong>AI Job Alerts</strong><br>
        I can help identify new jobs that match your profile.<br><br>
        Example alert:<br>
        “New Software Internship matches your profile!”<br><br>
        In the future, this can be connected to the backend notification module.
      `;
        }

        if (
            lowerMessage.includes("apply") ||
            lowerMessage.includes("application") ||
            lowerMessage.includes("申请")
        ) {
            return `
        <strong>Application Advice</strong><br>
        Before applying, please check:<br>
        • Your resume is uploaded.<br>
        • Your profile information is complete.<br>
        • Your cover letter mentions the job title and company.<br>
        • Your skills match the job requirements.
      `;
        }

        return `
      I can help with:<br>
      • AI job recommendations<br>
      • Job fit analysis<br>
      • Resume improvement suggestions<br>
      • Application success prediction<br>
      • Job alerts<br><br>
      Try asking: <strong>“Analyse job fit”</strong> or <strong>“Give resume suggestions”</strong>.
    `;
    }
})();