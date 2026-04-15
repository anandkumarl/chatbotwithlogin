package com.ragchatbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthResponse {
    @JsonProperty("status")
    private String status;

    @JsonProperty("redis")
    private boolean redis;

    @JsonProperty("timestamp")
    private long timestamp;

    // Default constructor
    public HealthResponse() {
    }

    // Constructor with all fields
    public HealthResponse(String status, boolean redis, long timestamp) {
        this.status = status;
        this.redis = redis;
        this.timestamp = timestamp;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String status;
        private boolean redis;
        private long timestamp;

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder redis(boolean redis) {
            this.redis = redis;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public HealthResponse build() {
            return new HealthResponse(status, redis, timestamp);
        }
    }

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRedis() {
        return redis;
    }

    public void setRedis(boolean redis) {
        this.redis = redis;
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
        return "HealthResponse{" +
                "status='" + status + '\'' +
                ", redis=" + redis +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HealthResponse that = (HealthResponse) o;

        if (redis != that.redis) return false;
        if (timestamp != that.timestamp) return false;
        return status != null ? status.equals(that.status) : that.status == null;
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (redis ? 1 : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
