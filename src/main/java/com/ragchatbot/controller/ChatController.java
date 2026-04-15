package com.ragchatbot.controller;

import com.ragchatbot.dto.ChatRequest;
import com.ragchatbot.dto.ChatResponse;
import com.ragchatbot.dto.ErrorResponse;
import com.ragchatbot.dto.HealthResponse;
import com.ragchatbot.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * Main endpoint for getting LLM response with Redis caching
     */
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        try {
            log.info("Received chat request: {}", request);

            ChatResponse response = chatService.getChatResponse(request.getUserMessage(), request.getModel());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("Error in chat endpoint: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        try {
            boolean redisConnected = isRedisConnected();
            HealthResponse response = HealthResponse.builder()
                    .status("OK")
                    .redis(redisConnected)
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in health check: {}", e.getMessage());
            HealthResponse response = HealthResponse.builder()
                    .status("ERROR")
                    .redis(false)
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    /**
     * Clear specific cache key
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<?> clearCache(@RequestBody(required = false) Map<String, String> request) {
        try {
            if (request != null && request.containsKey("key")) {
                String key = request.get("key");
                chatService.clearCacheKey(key);
                return ResponseEntity.ok(new CacheMessageResponse("Cache cleared for key: " + key));
            } else {
                chatService.clearAllCache();
                return ResponseEntity.ok(new CacheMessageResponse("All cache cleared"));
            }

        } catch (Exception e) {
            log.error("Error clearing cache: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get cache statistics
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<?> cacheStats() {
        try {
            Long dbSize = chatService.getCacheSize();
            CacheStatsResponse response = CacheStatsResponse.builder()
                    .dbSize(dbSize)
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting cache stats: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Check if Redis is connected
     */
    private boolean isRedisConnected() {
        try {
            redisConnectionFactory.getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Inner classes for response formatting
    public static class CacheMessageResponse {
        private String message;

        // Default constructor
        public CacheMessageResponse() {
        }

        // Constructor with all fields
        public CacheMessageResponse(String message) {
            this.message = message;
        }

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String message;

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public CacheMessageResponse build() {
                return new CacheMessageResponse(message);
            }
        }

        // Getters and setters
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        // toString, equals, and hashCode methods
        @Override
        public String toString() {
            return "CacheMessageResponse{" +
                    "message='" + message + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheMessageResponse that = (CacheMessageResponse) o;
            return message != null ? message.equals(that.message) : that.message == null;
        }

        @Override
        public int hashCode() {
            return message != null ? message.hashCode() : 0;
        }
    }

    public static class CacheStatsResponse {
        private Long dbSize;
        private long timestamp;

        // Default constructor
        public CacheStatsResponse() {
        }

        // Constructor with all fields
        public CacheStatsResponse(Long dbSize, long timestamp) {
            this.dbSize = dbSize;
            this.timestamp = timestamp;
        }

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long dbSize;
            private long timestamp;

            public Builder dbSize(Long dbSize) {
                this.dbSize = dbSize;
                return this;
            }

            public Builder timestamp(long timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public CacheStatsResponse build() {
                return new CacheStatsResponse(dbSize, timestamp);
            }
        }

        // Getters and setters
        public Long getDbSize() {
            return dbSize;
        }

        public void setDbSize(Long dbSize) {
            this.dbSize = dbSize;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        // toString, equals, and hashCode methods
        @Override
        public String toString() {
            return "CacheStatsResponse{" +
                    "dbSize=" + dbSize +
                    ", timestamp=" + timestamp +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheStatsResponse that = (CacheStatsResponse) o;

            if (timestamp != that.timestamp) return false;
            return dbSize != null ? dbSize.equals(that.dbSize) : that.dbSize == null;
        }

        @Override
        public int hashCode() {
            int result = dbSize != null ? dbSize.hashCode() : 0;
            result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
            return result;
        }
    }
}

