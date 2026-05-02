(function () {
    const assistantHTML = `
    <button class="admin-ai-button" id="adminAiButton" title="Admin AI Assistant">
      <img src="./assets/images/robot.png" alt="AI Assistant">
    </button>

    <div class="admin-ai-window" id="adminAiWindow">
      <div class="admin-ai-header">
        <span>Admin AI Assistant</span>
        <button class="admin-ai-close" id="adminAiClose">×</button>
      </div>

      <div class="admin-ai-messages" id="adminAiMessages">
        <div class="admin-ai-message bot">
          Hi! I can help administrators with job moderation, fraud detection, platform analytics, and automatic report generation.
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
        Example moderation result:<br>
        • Missing company information: Medium risk<br>
        • Unrealistic salary description: Warning<br>
        • Incomplete job requirements: Needs review<br><br>
        Suggested action: Review flagged job posts before approving them.
      `;
        }

        if (
            lowerMessage.includes("fraud") ||
            lowerMessage.includes("suspicious") ||
            lowerMessage.includes("user") ||
            lowerMessage.includes("spam") ||
            lowerMessage.includes("异常") ||
            lowerMessage.includes("虚假") ||
            lowerMessage.includes("用户")
        ) {
            return `
        <strong>AI Fraud Detection</strong><br>
        I can identify suspicious employers, spam accounts, or fake job activities.<br><br>
        Example risk report:<br>
        • Employer account with incomplete profile: Medium risk<br>
        • Multiple similar job posts in a short time: High risk<br>
        • User reported by several students: High risk<br><br>
        Risk Level: <strong>High</strong><br>
        Suggested action: Temporarily restrict the account and review its activity.
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
        I can summarise platform activity and employment trends.<br><br>
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
        Suggested report conclusion:<br>
        The platform shows stable growth in student engagement and employer participation.
      `;
        }

        if (
            lowerMessage.includes("approve") ||
            lowerMessage.includes("remove") ||
            lowerMessage.includes("delete") ||
            lowerMessage.includes("批准") ||
            lowerMessage.includes("删除")
        ) {
            return `
        <strong>Admin Action Advice</strong><br>
        Before approving or removing content, check:<br>
        • Whether the company profile is verified<br>
        • Whether the job description is complete<br>
        • Whether users have reported this post<br>
        • Whether the post contains misleading information<br><br>
        This supports safer admin decision-making.
      `;
        }

        return `
      I can help administrators with:<br>
      • AI job moderation<br>
      • Fraud detection<br>
      • Platform analytics<br>
      • Automatic report generation<br>
      • Admin action advice<br><br>
      Try asking: <strong>“Check job moderation”</strong> or <strong>“Generate monthly report”</strong>.
    `;
    }
})();