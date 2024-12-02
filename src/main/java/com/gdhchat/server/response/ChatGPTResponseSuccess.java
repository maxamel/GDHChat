package com.gdhchat.server.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdhchat.server.ServerConstants;


public class ChatGPTResponseSuccess extends ChatGPTResponse {

    private String finishReason;
    private String role;

    public ChatGPTResponseSuccess() {
        this.status = ServerConstants.ResponseStatus.SUCCESS;
    }

    public void parse(String successMessage) {
        try {
            // Step 2: Parse the JSON string
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode successNode = objectMapper.readTree(successMessage);
            JsonNode node = successNode.get("choices").get(0);

            this.message = node.get("message").get("content").asText(null);
            this.role = node.get("message").get("role").asText(null);
            this.finishReason = node.get("finish_reason").asText(null);

        } catch (Exception e) {
            System.err.println("Failed to parse success message: " + e.getMessage());
        }
    }

    public String getRole() {
        return this.role;
    }

    public String getFinishReason() {
        return this.finishReason;
    }
}
