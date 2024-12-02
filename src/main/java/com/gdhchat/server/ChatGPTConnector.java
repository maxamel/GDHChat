package com.gdhchat.server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ChatGPTConnector {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ChatGPTConnector(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String sendMessage(String message, String role, List<ObjectNode> context) throws Exception {
        // Construct the request payload
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("role", role);
        payload.put("content", message);

        if (context == null) {
            context = new ArrayList<>();
        }

        context.add(payload);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", context);

        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        // Create the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        // Send the request and parse the response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Parse the response JSON
            return response.body();
        } else {
            System.out.println("ChatGPT call failed with status code: " + response.statusCode());
            throw new Exception(response.body());
        }
    }
}