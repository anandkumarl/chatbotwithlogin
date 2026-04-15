package com.ragchatbot.service;

import com.ragchatbot.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private GeminiService geminiService;

    private static final long CACHE_TTL = 24 * 60 * 60; // 24 hours in seconds

    /**
     * Get chat response with Redis caching
     */
    public ChatResponse getChatResponse(String userMessage, String model) {
        try {
            if (userMessage == null || userMessage.trim().isEmpty()) {
                throw new IllegalArgumentException("User message cannot be empty");
            }

            String normalizedModel = model != null ? model : "gemini-2.5-flash";
            String cacheKey = generateCacheKey(userMessage, normalizedModel);

            // Check Redis cache first
            Object cachedResponse = getCachedResponse(cacheKey);
            if (cachedResponse != null) {
                log.info("Cache HIT for key: {}", cacheKey);
                return ChatResponse.builder()
                        .botMessage((String) cachedResponse)
                        .source("cache")
                        .model(normalizedModel)
                        .timestamp(System.currentTimeMillis())
                        .build();
            }

            log.info("Cache MISS for key: {}", cacheKey);

            // Fetch from Gemini API
            String apiResponse = geminiService.callGeminiAPI(userMessage, normalizedModel);

            // Cache the response
            cacheResponse(cacheKey, apiResponse);
            log.info("Cached response for key: {} with TTL: {}s", cacheKey, CACHE_TTL);

            return ChatResponse.builder()
                    .botMessage(apiResponse)
                    .source("api")
                    .model(normalizedModel)
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("Error in getChatResponse: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing chat request: " + e.getMessage());
        }
    }

    /**
     * Generate cache key from user message and model
     */
    private String generateCacheKey(String userMessage, String model) {
        return "gemini:" + model + ":" + userMessage.toLowerCase().trim();
    }

    /**
     * Get cached response from Redis
     */
    private Object getCachedResponse(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Error reading from Redis cache: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Cache response in Redis
     */
    private void cacheResponse(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value, CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Error writing to Redis cache: {}", e.getMessage());
        }
    }

    /**
     * Clear specific cache key
     */
    public void clearCacheKey(String key) {
        try {
            redisTemplate.delete(key);
            log.info("Cleared cache for key: {}", key);
        } catch (Exception e) {
            log.error("Error clearing cache: {}", e.getMessage());
            throw new RuntimeException("Error clearing cache: " + e.getMessage());
        }
    }

    /**
     * Clear all cache
     */
    public void clearAllCache() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            log.info("Cleared all cache");
        } catch (Exception e) {
            log.error("Error clearing all cache: {}", e.getMessage());
            throw new RuntimeException("Error clearing all cache: " + e.getMessage());
        }
    }

    /**
     * Get cache size
     */
    public Long getCacheSize() {
        try {
            return redisTemplate.getConnectionFactory().getConnection().dbSize();
        } catch (Exception e) {
            log.warn("Error getting cache size: {}", e.getMessage());
            return 0L;
        }
    }
}
