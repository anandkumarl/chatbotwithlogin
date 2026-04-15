// script.js
document.addEventListener("DOMContentLoaded", function () {
  const chatbotContainer = document.getElementById("chatbot-container");
  const closeBtn = document.getElementById("close-btn");
  const sendBtn = document.getElementById("send-btn");
  const chatbotInput = document.getElementById("chatbot-input");
  const chatbotMessages = document.getElementById("chatbot-messages");

  const chatbotIcon = document.getElementById("chatbot-icon");
  const closeButton = document.getElementById("close-btn");
  const modelSelect = document.getElementById("chatbot-model");

  // Toggle chatbot visibility when clicking the icon
  // Show chatbot when clicking the icon
  chatbotIcon.addEventListener("click", function () {
    chatbotContainer.classList.remove("hidden");
    chatbotIcon.style.display = "none"; // Hide chat icon
  });

  // Also toggle when clicking the close button
  closeButton.addEventListener("click", function () {
    chatbotContainer.classList.add("hidden");
    chatbotIcon.style.display = "flex"; // Show chat icon again
  });

  sendBtn.addEventListener("click", sendMessage);
  chatbotInput.addEventListener("keypress", function (e) {
    if (e.key === "Enter") {
      sendMessage();
    }
  });

  function sendMessage() {
    const userMessage = chatbotInput.value.trim();
    if (userMessage) {
      appendMessage("user", userMessage);
      chatbotInput.value = "";
      getGeminiResponse(userMessage, modelSelect.value);
    }
  }

  function appendMessage(sender, message) {
    const messageElement = document.createElement("div");
    messageElement.classList.add("message", sender);
    messageElement.textContent = message;
    chatbotMessages.appendChild(messageElement);
    chatbotMessages.scrollTop = chatbotMessages.scrollHeight;
  }

  async function getGeminiResponse(userMessage, selectedModel) {
    const model = selectedModel || "gemini-2.5-flash";
    const serverUrl = "http://localhost:8080/api/chat"; // Spring Boot backend server endpoint

    try {
      const response = await fetch(serverUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          userMessage,
          model,
        }),
      });

      const data = await response.json();
      console.log("Server Response:", data);

      // Check if the server returned an error
      if (!response.ok || data.error) {
        const errorMsg = data.error || "Server returned an error";
        console.error("Server Error:", errorMsg);
        appendMessage("bot", `Error: ${errorMsg}`);
        return;
      }

      const botMessage = data.botMessage || "Unable to extract message";
      const source = data.source || "unknown";
      
      // Log cache source for debugging
      console.log(`Response source: ${source} (${source === "cache" ? "🔄 Cached" : "🌐 API"})`);
      
      appendMessage("bot", botMessage);
    } catch (error) {
      console.error("Error fetching bot response:", error);
      appendMessage("bot", "Sorry, something went wrong. Please try again.");
    }
  }
});
