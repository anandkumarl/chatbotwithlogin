package com.ragchatbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatRequest {
    @JsonProperty("userMessage")
    private String userMessage;

    @JsonProperty("model")
    private String model;

    // Default constructor
    public ChatRequest() {
    }

    // Constructor with all fields
    public ChatRequest(String userMessage, String model) {
        this.userMessage = userMessage;
        this.model = model;
    }

    // Getters and setters
    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    // toString, equals, and hashCode methods
    @Override
    public String toString() {
        return "ChatRequest{" +
                "userMessage='" + userMessage + '\'' +
                ", model='" + model + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatRequest that = (ChatRequest) o;

        if (userMessage != null ? !userMessage.equals(that.userMessage) : that.userMessage != null) return false;
        return model != null ? model.equals(that.model) : that.model == null;
    }

    @Override
    public int hashCode() {
        int result = userMessage != null ? userMessage.hashCode() : 0;
        result = 31 * result + (model != null ? model.hashCode() : 0);
        return result;
    }
}
