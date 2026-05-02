(function () {
    const assistantHTML = `
    <button class="admin-ai-button" id="adminAiButton" title="Admin AI Assistant">
      <img src="images/robot.png" alt="AI Assistant">
    </button>

    <div class="admin-ai-window" id="adminAiWindow">
      <div class="admin-ai-header">
        <span>Admin AI Assistant</span>
        <button class="admin-ai-close" id="adminAiClose">×</button>
      </div>

      <div class="admin-ai-messages" id="adminAiMessages">
        <div class="admin-ai-message bot">
          Hi! I can help with job moderation, fraud detection, platform analytics, and automatic report generation.
        </div>
      </div>

      <div class="admin-ai-actions">
        <button data-question="Check job moderation">Job Moderation</button>
        <button data-question="Detect suspicious users">Fraud Detection</button>
        <button data-question="Show platform analytics">Platform Analytics</button>
        <button data-question="Generate monthly report">Auto Report</button>
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

    aiButton.addEventListener("click", function () {
        aiWindow.classList.toggle("open");
    });

    aiClose.addEventListener("click", function () {
        aiWindow.classList.remove("open");
    });

    aiSend.addEventListener("click", sendMessage);

    aiInput.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            sendMessage();
        }
    });

    document.querySelectorAll(".admin-ai-actions button").forEach(function (button) {
        button.addEventListener("click", function () {
            const question = button.getAttribute("data-question");
            addMessage(question, "user");

            setTimeout(function () {
                addMessage(generateAdminAIResponse(question), "bot");
            }, 400);
        });
    });

    function sendMessage() {
        const message = aiInput.value.trim();

        if (!message) {
            return;
        }

        addMessage(message, "user");
        aiInput.value = "";

        setTimeout(function () {
            addMessage(generateAdminAIResponse(message), "bot");
        }, 400);
    }

    function addMessage(text, sender) {
        const messageElement = document.createElement("div");
        messageElement.className = "admin-ai-message " + sender;
        messageElement.innerHTML = text;
        aiMessages.appendChild(messageElement);
        aiMessages.scrollTop = aiMessages.scrollHeight;
    }

    function generateAdminAIResponse(message) {
        const lowerMessage = message.toLowerCase();

        if (
            lowerMessage.includes("moderation") ||
            lowerMessage.includes("job") ||
            lowerMessage.includes("审核") ||
            lowerMessage.includes("岗位")
        ) {
            return `
        <strong>AI Job Moderation</strong><br>
        I can help check whether job posts are complete, safe, and suitable for students.<br><br>
        Example result:<br>
        • Missing company information: Medium risk<br>
        • Unrealistic salary description: Warning<br>
        • Incomplete job requirements: Needs review<br><br>
        Suggested action: Review flagged job posts before approval.
      `;
        }

        if (
            lowerMessage.includes("fraud") ||
            lowerMessage.includes("suspicious") ||
            lowerMessage.includes("spam") ||
            lowerMessage.includes("user") ||
            lowerMessage.includes("异常") ||
            lowerMessage.includes("虚假") ||
            lowerMessage.includes("用户")
        ) {
            return `
        <strong>AI Fraud Detection</strong><br>
        I can detect suspicious employers, spam accounts, and fake job activity.<br><br>
        Example risk report:<br>
        • Incomplete employer profile: Medium risk<br>
        • Repeated similar job posts: High risk<br>
        • Reported by multiple students: High risk<br><br>
        Risk Level: <strong>High</strong><br>
        Suggested action: Temporarily restrict the account and review activity.
      `;
        }

        if (
            lowerMessage.includes("analytics") ||
            lowerMessage.includes("platform") ||
            lowerMessage.includes("dashboard") ||
            lowerMessage.includes("trend") ||
            lowerMessage.includes("分析") ||
            lowerMessage.includes("数据")
        ) {
            return `
        <strong>AI Platform Analytics</strong><br>
        I can summarise platform activity and job market trends.<br><br>
        Example insights:<br>
        • Popular industry: Information Technology<br>
        • Total active students: 128<br>
        • Total job posts: 42<br>
        • Application success rate: 68%<br>
        • Job posting trend: Increasing this month<br><br>
        This helps administrators monitor platform performance.
      `;
        }

        if (
            lowerMessage.includes("report") ||
            lowerMessage.includes("monthly") ||
            lowerMessage.includes("generate") ||
            lowerMessage.includes("报告") ||
            lowerMessage.includes("生成")
        ) {
            return `
        <strong>AI Auto Report</strong><br>
        Monthly Platform Report Summary:<br><br>
        • New job posts: 18<br>
        • Student applications: 96<br>
        • Active employers: 15<br>
        • Most popular field: Software Development<br>
        • Platform risk level: Low to Medium<br><br>
        Suggested conclusion:<br>
        The platform shows stable growth in student engagement and employer participation.
      `;
        }

        return `
      I can help administrators with:<br>
      • AI job moderation<br>
      • Fraud detection<br>
      • Platform analytics<br>
      • Automatic report generation<br><br>
      Try asking: <strong>“Check job moderation”</strong> or <strong>“Generate monthly report”</strong>.
    `;
    }
})();