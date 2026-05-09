(function () {
    const API_BASE = "http://localhost:8080";
    const STORAGE_KEY = "studentQwenAiChatHistory";

    const aiMessages = document.getElementById("aiMessages");
    const aiInput = document.getElementById("aiInput");
    const aiSend = document.getElementById("aiSend");
    const clearBtn = document.getElementById("clearAiHistory");

    loadHistory();

    aiSend.addEventListener("click", sendMessage);

    aiInput.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            sendMessage();
        }
    });

    document.querySelectorAll(".ai-quick-actions button[data-question]").forEach(function (button) {
        button.addEventListener("click", function () {
            sendToGemini(button.dataset.question);
        });
    });

    clearBtn.addEventListener("click", function () {
        localStorage.removeItem(STORAGE_KEY);
        aiMessages.innerHTML = "";
        addMessage("Hi! I am your Qwen AI assistant. You can ask me about jobs, resumes, applications, and interviews.", "bot");
    });

    function sendMessage() {
        const message = aiInput.value.trim();
        if (!message) return;

        aiInput.value = "";
        sendToGemini(message);
    }

    async function sendToGemini(message) {
        addMessage(message, "user");
        const loading = addMessage("Qwen is thinking...", "bot");

        try {
            const token = localStorage.getItem("token") || localStorage.getItem("studentToken");

            const response = await fetch(API_BASE + "/ai/student/chat", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...(token ? { Authorization: "Bearer " + token } : {})
                },
                body: JSON.stringify({ message: message })
            });

            const result = await response.json();

            if (!response.ok || result.code !== 200) {
                throw new Error(result.message || "AI request failed");
            }

            loading.textContent = result.data.answer;
            saveHistory();

        } catch (error) {
            loading.textContent =
                "Connection failed: " + error.message +
                "\n\nPlease check: 1) Spring Boot is running; 2) Qwen API key is correct; 3) you are logged in as student.";
            saveHistory();
        }
    }

    function addMessage(text, sender) {
        const div = document.createElement("div");
        div.className = "ai-message " + sender;
        div.textContent = text;
        aiMessages.appendChild(div);
        aiMessages.scrollTop = aiMessages.scrollHeight;
        saveHistory();
        return div;
    }

    function saveHistory() {
        const messages = Array.from(aiMessages.querySelectorAll(".ai-message")).map(function (el) {
            return {
                sender: el.classList.contains("user") ? "user" : "bot",
                text: el.textContent
            };
        });
        localStorage.setItem(STORAGE_KEY, JSON.stringify(messages));
    }

    function loadHistory() {
        const saved = localStorage.getItem(STORAGE_KEY);

        if (!saved) {
            addMessage("Hi! I am your Qwen AI assistant. You can ask me about jobs, resumes, applications, and interviews.", "bot");
            return;
        }

        try {
            JSON.parse(saved).forEach(function (item) {
                addMessage(item.text, item.sender);
            });
        } catch (e) {
            localStorage.removeItem(STORAGE_KEY);
            addMessage("Hi! I am your Qwen AI assistant.", "bot");
        }
    }
})();