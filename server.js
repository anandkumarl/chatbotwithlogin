const express = require("express");
const cors = require("cors");
const { createClient } = require("redis");
const path = require("path");
require("dotenv").config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, "HTML-CSS-JS-CHATGPT")));

// Redis client setup
const redisClient = createClient({
  host: process.env.REDIS_HOST || "localhost",
  port: process.env.REDIS_PORT || 6379,
  password: process.env.REDIS_PASSWORD || undefined,
});

redisClient.on("error", (err) => console.log("Redis Client Error", err));
redisClient.on("connect", () => console.log("Connected to Redis"));

// Connect to Redis
(async () => {
  try {
    await redisClient.connect();
    console.log("Redis connected successfully");
  } catch (err) {
    console.error("Failed to connect to Redis:", err);
  }
})();

// Cache configuration
const CACHE_TTL = 24 * 60 * 60; // 24 hours in seconds

/**
 * Generate cache key from user message and model
 * @param {string} userMessage - The user input message
 * @param {string} model - The selected model
 * @returns {string} - The cache key
 */
function generateCacheKey(userMessage, model) {
  // Create a simple hash-like key from user message and model
  const key = `gemini:${model}:${userMessage.toLowerCase().trim()}`;
  return key;
}

/**
 * Main endpoint for getting LLM response with Redis caching
 */
app.post("/api/chat", async (req, res) => {
  try {
    const { userMessage, model = "gemini-2.5-flash" } = req.body;

    if (!userMessage || userMessage.trim() === "") {
      return res.status(400).json({ error: "User message is required" });
    }

    const cacheKey = generateCacheKey(userMessage, model);

    // Check Redis cache first
    let cachedResponse = null;
    try {
      cachedResponse = await redisClient.get(cacheKey);
      if (cachedResponse) {
        console.log(`Cache HIT for key: ${cacheKey}`);
        return res.json({
          botMessage: cachedResponse,
          source: "cache",
        });
      }
    } catch (cacheErr) {
      console.warn("Redis cache read error:", cacheErr.message);
    }

    console.log(`Cache MISS for key: ${cacheKey}`);

    // If not in cache, fetch from Gemini API
    const apiKey = process.env.GEMINI_API_KEY || "AIzaSyDbSz7B4GypD0kRKqDOdxq4swrv9JF9RSI";
    const apiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`;

    const response = await fetch(apiUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model,
        contents: [
          {
            parts: [
              {
                text: userMessage,
              },
            ],
          },
        ],
      }),
    });

    const data = await response.json();

    if (!response.ok || data.error) {
      const errorMsg = data.error?.message || "Gemini API returned an error";
      console.error("Gemini API Error:", errorMsg);
      return res.status(response.status || 500).json({
        error: errorMsg,
      });
    }

    if (!data.candidates || data.candidates.length === 0) {
      return res.status(500).json({
        error: "No response received from Gemini API",
      });
    }

    const botMessage = data.candidates[0].content?.parts?.[0]?.text || "Unable to extract message from Gemini";

    // Store response in Redis cache
    try {
      await redisClient.setEx(cacheKey, CACHE_TTL, botMessage);
      console.log(`Cached response for key: ${cacheKey} with TTL: ${CACHE_TTL}s`);
    } catch (cacheErr) {
      console.warn("Redis cache write error:", cacheErr.message);
    }

    res.json({
      botMessage,
      source: "api",
    });
  } catch (error) {
    console.error("Error in /api/chat:", error);
    res.status(500).json({
      error: "Server error: " + error.message,
    });
  }
});

/**
 * Health check endpoint
 */
app.get("/api/health", (req, res) => {
  res.json({ status: "OK", redis: redisClient.isOpen });
});

/**
 * Endpoint to clear cache
 */
app.post("/api/cache/clear", async (req, res) => {
  try {
    const { key } = req.body;
    if (key) {
      await redisClient.del(key);
      res.json({ message: `Cache cleared for key: ${key}` });
    } else {
      await redisClient.flushDb();
      res.json({ message: "All cache cleared" });
    }
  } catch (error) {
    console.error("Error clearing cache:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * Endpoint to get cache stats
 */
app.get("/api/cache/stats", async (req, res) => {
  try {
    const info = await redisClient.info("stats");
    const dbSize = await redisClient.dbSize();
    res.json({
      dbSize,
      info,
    });
  } catch (error) {
    console.error("Error getting cache stats:", error);
    res.status(500).json({ error: error.message });
  }
});

// Start server
app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
  console.log("Waiting for Redis connection...");
});

// Graceful shutdown
process.on("SIGINT", async () => {
  console.log("Shutting down gracefully...");
  await redisClient.quit();
  process.exit(0);
});
