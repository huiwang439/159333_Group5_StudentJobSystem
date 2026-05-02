(function () {
    const assistantHTML = `
    <button class="employer-ai-button" id="employerAiButton" title="Employer AI Assistant">
      <img src="./assets/images/robot.png" alt="AI Assistant">
    </button>

    <div class="employer-ai-window" id="employerAiWindow">
      <div class="employer-ai-header">
        <span>Employer AI Assistant</span>
        <button class="employer-ai-close" id="employerAiClose">×</button>
      </div>

      <div class="employer-ai-messages" id="employerAiMessages">
        <div class="employer-ai-message bot">
          Hi! I can help employers with resume screening, candidate recommendations, job description improvement, and hiring analytics.
        </div>
      </div>

      <div class="employer-ai-actions">
        <button data-question="Screen resumes">Resume Screening</button>
        <button data-question="Recommend candidates">Candidate Recommendation</button>
        <button data-question="Improve job description">Improve Job Description</button>
        <button data-question="Show hiring analytics">Hiring Analytics</button>
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

    document.querySelectorAll(".employer-ai-actions button").forEach(function (button) {
        button.addEventListener("click", function () {
            const question = button.getAttribute("data-question");
            addMessage(question, "user");

            setTimeout(function () {
                addMessage(generateEmployerAIResponse(question), "bot");
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
            addMessage(generateEmployerAIResponse(message), "bot");
        }, 400);
    }

    function addMessage(text, sender) {
        const messageElement = document.createElement("div");
        messageElement.className = "employer-ai-message " + sender;
        messageElement.innerHTML = text;
        aiMessages.appendChild(messageElement);
        aiMessages.scrollTop = aiMessages.scrollHeight;
    }

    function generateEmployerAIResponse(message) {
        const lowerMessage = message.toLowerCase();

        if (
            lowerMessage.includes("resume") ||
            lowerMessage.includes("screen") ||
            lowerMessage.includes("cv") ||
            lowerMessage.includes("简历") ||
            lowerMessage.includes("筛选")
        ) {
            return `
        <strong>AI Resume Screening</strong><br>
        I can rank applicants based on skills, academic background, and project experience.<br><br>
        Example result:<br>
        1. Candidate A - 88% match<br>
        2. Candidate B - 76% match<br>
        3. Candidate C - 65% match<br><br>
        Suggestion: Prioritise candidates with matching skills and relevant project experience.
      `;
        }

        if (
            lowerMessage.includes("candidate") ||
            lowerMessage.includes("recommend") ||
            lowerMessage.includes("student") ||
            lowerMessage.includes("候选人") ||
            lowerMessage.includes("推荐")
        ) {
            return `
        <strong>AI Candidate Recommendation</strong><br>
        Based on the job requirements, I recommend students who match the required skills, study background, and application history.<br><br>
        Example recommended students:<br>
        • Student A - strong Java and MySQL skills<br>
        • Student B - web development project experience<br>
        • Student C - relevant internship background<br><br>
        This helps employers find suitable candidates faster.
      `;
        }

        if (
            lowerMessage.includes("description") ||
            lowerMessage.includes("job post") ||
            lowerMessage.includes("improve") ||
            lowerMessage.includes("岗位描述") ||
            lowerMessage.includes("优化")
        ) {
            return `
        <strong>AI Job Description Assistant</strong><br>
        I can help improve job descriptions by making them clearer and more attractive.<br><br>
        Before:<br>
        Need developer.<br><br>
        After:<br>
        We are seeking a motivated Web Developer Intern with knowledge of HTML, CSS, JavaScript, Java, and MySQL. The role involves supporting website development, testing features, and working with the development team.<br><br>
        Suggestion: Include job responsibilities, required skills, location, deadline, and company information.
      `;
        }

        if (
            lowerMessage.includes("analytics") ||
            lowerMessage.includes("trend") ||
            lowerMessage.includes("dashboard") ||
            lowerMessage.includes("分析") ||
            lowerMessage.includes("数据")
        ) {
            return `
        <strong>AI Hiring Analytics</strong><br>
        I can summarise recruitment data for employers.<br><br>
        Example insights:<br>
        • Total applicants: 24<br>
        • Best-fit candidates: 6<br>
        • Most common applicant skill: JavaScript<br>
        • Application trend: increasing this week<br><br>
        This helps employers make better recruitment decisions.
      `;
        }

        if (
            lowerMessage.includes("interview") ||
            lowerMessage.includes("面试")
        ) {
            return `
        <strong>AI Interview Support</strong><br>
        Suggested interview questions:<br>
        • Can you describe one project related to this role?<br>
        • What technical skills are you most confident in?<br>
        • How do you solve problems when working in a team?<br>
        • Why are you interested in this position?
      `;
        }

        return `
      I can help employers with:<br>
      • AI resume screening<br>
      • Candidate recommendations<br>
      • Job description improvement<br>
      • Hiring analytics<br>
      • Interview question suggestions<br><br>
      Try asking: <strong>“Screen resumes”</strong> or <strong>“Improve job description”</strong>.
    `;
    }
})();