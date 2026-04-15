package com.ragchatbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
    @JsonProperty("error")
    private String error;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("status")
    private int status;

    // Default constructor
    public ErrorResponse() {
    }

    // Constructor with all fields
    public ErrorResponse(String error, long timestamp, int status) {
        this.error = error;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String error;
        private long timestamp;
        private int status;

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(error, timestamp, status);
        }
    }

    // Getters and setters
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    // toString, equals, and hashCode methods
    @Override
    public String toString() {
        return "ErrorResponse{" +
                "error='" + error + '\'' +
                ", timestamp=" + timestamp +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ErrorResponse that = (ErrorResponse) o;

        if (timestamp != that.timestamp) return false;
        if (status != that.status) return false;
        return error != null ? error.equals(that.error) : that.error == null;
    }

    @Override
    public int hashCode() {
        int result = error != null ? error.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + status;
        return result;
    }
}
