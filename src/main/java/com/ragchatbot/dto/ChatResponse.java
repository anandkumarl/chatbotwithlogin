package com.ragchatbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatResponse {
    @JsonProperty("botMessage")
    private String botMessage;

    @JsonProperty("source")
    private String source; // "cache" or "api"

    @JsonProperty("model")
    private String model;

    @JsonProperty("timestamp")
    private long timestamp;

    // Default constructor
    public ChatResponse() {
    }

    // Constructor with all fields
    public ChatResponse(String botMessage, String source, String model, long timestamp) {
        this.botMessage = botMessage;
        this.source = source;
        this.model = model;
        this.timestamp = timestamp;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String botMessage;
        private String source;
        private String model;
        private long timestamp;

        public Builder botMessage(String botMessage) {
            this.botMessage = botMessage;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ChatResponse build() {
            return new ChatResponse(botMessage, source, model, timestamp);
        }
    }

    // Getters and setters
    public String getBotMessage() {
        return botMessage;
    }

    public void setBotMessage(String botMessage) {
        this.botMessage = botMessage;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
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
        return "ChatResponse{" +
                "botMessage='" + botMessage + '\'' +
                ", source='" + source + '\'' +
                ", model='" + model + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatResponse that = (ChatResponse) o;

        if (timestamp != that.timestamp) return false;
        if (botMessage != null ? !botMessage.equals(that.botMessage) : that.botMessage != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        return model != null ? model.equals(that.model) : that.model == null;
    }

    @Override
    public int hashCode() {
        int result = botMessage != null ? botMessage.hashCode() : 0;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (model != null ? model.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
