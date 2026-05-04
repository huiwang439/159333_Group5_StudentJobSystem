(function () {
    const assistantHTML = `
    <button class="student-ai-button" id="studentAiButton" title="Student AI Assistant">
      <img src="./images/robot.png" alt="AI Assistant">
    </button>

    <div class="student-ai-window" id="studentAiWindow">
      <div class="student-ai-header">
        <span>Student AI Assistant</span>
        <button class="student-ai-close" id="studentAiClose">×</button>
      </div>

      <div class="student-ai-messages" id="studentAiMessages">
        <div class="student-ai-message bot">
          Hi! I can help with job fit analysis, job recommendations, job alerts, resume improvement, and application success prediction.
        </div>
      </div>

      <div class="student-ai-actions">
        <button data-question="Job fit analysis">Job Fit</button>
        <button data-question="Recommend jobs">Recommend Jobs</button>
        <button data-question="Job alert">Job Alert</button>
        <button data-question="Resume improvement">Resume Tips</button>
        <button data-question="Success prediction">Success Rate</button>
      </div>

      <div class="student-ai-input-area">
        <input id="studentAiInput" type="text" placeholder="Ask Student AI..." />
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

    document.querySelectorAll(".student-ai-actions button").forEach(function (button) {
        button.addEventListener("click", function () {
            const question = button.getAttribute("data-question");
            addMessage(question, "user");

            setTimeout(function () {
                addMessage(generateStudentAIResponse(question), "bot");
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
            addMessage(generateStudentAIResponse(message), "bot");
        }, 400);
    }

    function addMessage(text, sender) {
        const messageElement = document.createElement("div");
        messageElement.className = "student-ai-message " + sender;
        messageElement.innerHTML = text;
        aiMessages.appendChild(messageElement);
        aiMessages.scrollTop = aiMessages.scrollHeight;
    }

    function generateStudentAIResponse(message) {
        const lowerMessage = message.toLowerCase();

        if (
            lowerMessage.includes("fit") ||
            lowerMessage.includes("match") ||
            lowerMessage.includes("匹配")
        ) {
            return `
        <strong>AI Job Fit Analysis</strong><br>
        Match Score: <strong>82%</strong><br>
        Match Level: <strong>High</strong><br><br>
        Why match:<br>
        • Your major is related to the job field.<br>
        • Your skills match Java, HTML, CSS, and MySQL requirements.<br><br>
        Suggestions:<br>
        • Add more project experience.<br>
        • Improve your resume keywords.
      `;
        }

        if (
            lowerMessage.includes("recommend") ||
            lowerMessage.includes("job") ||
            lowerMessage.includes("推荐")
        ) {
            return `
        <strong>AI Job Recommendation</strong><br>
        Recommended jobs for you:<br><br>
        1. Junior Java Developer - 90% match<br>
        2. Frontend Intern - 84% match<br>
        3. Data Assistant - 76% match<br><br>
        Reason: These jobs match your major, skills, and preferred location.
      `;
        }

        if (
            lowerMessage.includes("alert") ||
            lowerMessage.includes("notification") ||
            lowerMessage.includes("提醒") ||
            lowerMessage.includes("通知")
        ) {
            return `
        <strong>AI Job Alert</strong><br>
        New job matches your profile!<br><br>
        • Software Developer Intern<br>
        • Location: Auckland<br>
        • Match Score: 86%<br><br>
        Suggestion: Apply soon before the deadline.
      `;
        }

        if (
            lowerMessage.includes("resume") ||
            lowerMessage.includes("cv") ||
            lowerMessage.includes("简历")
        ) {
            return `
        <strong>AI Resume Improvement</strong><br>
        Suggestions:<br><br>
        • Add a skills section with technical keywords.<br>
        • Add project experience related to web development.<br>
        • Use action verbs such as developed, designed, implemented.<br>
        • Keep the resume clear and concise.
      `;
        }

        if (
            lowerMessage.includes("success") ||
            lowerMessage.includes("probability") ||
            lowerMessage.includes("rate") ||
            lowerMessage.includes("成功率")
        ) {
            return `
        <strong>AI Application Success Prediction</strong><br>
        Success Probability: <strong>70%</strong><br><br>
        Based on:<br>
        • Job match score<br>
        • Required skills<br>
        • Student background<br><br>
        Suggestion: Improve your resume and add more project evidence to increase your chance.
      `;
        }

        return `
      I can help with:<br>
      • Job fit analysis<br>
      • Job recommendations<br>
      • Job alerts<br>
      • Resume improvement<br>
      • Application success prediction<br><br>
      You can click the quick buttons below or type your question.
    `;
    }
})();