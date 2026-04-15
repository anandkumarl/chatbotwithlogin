package com.ragchatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Call Gemini API to get response
     */
    public String callGeminiAPI(String userMessage, String model) throws Exception {
        String url = apiUrl + "/" + model + ":generateContent?key=" + apiKey;

        Map<String, Object> requestBody = buildGeminiRequest(userMessage);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.debug("Calling Gemini API with model: {}", model);

        try {
            String response = restTemplate.postForObject(url, entity, String.class);
            log.debug("Gemini API Response: {}", response);
            
            return extractMessageFromResponse(response);
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Error calling Gemini API: " + e.getMessage());
        }
    }

    /**
     * Build the request body for Gemini API
     */
    private Map<String, Object> buildGeminiRequest(String userMessage) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", "gemini-2.5-flash");
        
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", userMessage);
        
        content.put("parts", new Object[]{parts});
        request.put("contents", new Object[]{content});

        return request;
    }

    /**
     * Extract message from Gemini API response
     */
    private String extractMessageFromResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        
        if (root.has("error")) {
            throw new RuntimeException("Gemini API Error: " + root.get("error").asText());
        }

        JsonNode candidates = root.get("candidates");
        if (candidates == null || candidates.size() == 0) {
            throw new RuntimeException("No candidates in Gemini API response");
        }

        JsonNode content = candidates.get(0).get("content").get("parts").get(0);
        return content.get("text").asText();
    }
}
