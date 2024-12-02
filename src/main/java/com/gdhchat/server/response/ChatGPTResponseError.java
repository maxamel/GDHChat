package com.gdhchat.server.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdhchat.server.ServerConstants;

public class ChatGPTResponseError extends ChatGPTResponse {

    private String type;
    private String param;
    private String code;

    public ChatGPTResponseError() {
        this.status = ServerConstants.ResponseStatus.FAIL;
    }

    public void parse(String errorMessage) {
        try {
            // Step 2: Parse the JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(errorMessage);

            // Step 3: Extract error details
            JsonNode errorNode = rootNode.get("error");
            String message = errorNode.get("message").asText();
            String type = errorNode.get("type").asText();
            String param = errorNode.get("param").isNull() ? null : errorNode.get("param").asText();
            String code = errorNode.get("code").asText();

            this.message = message;
            this.code = code;
            this.param = param;
            this.type = type;

        } catch (Exception e) {
            System.err.println("Failed to parse error message: " + e.getMessage());
        }
    }

    public String getType() {
        return this.type;
    }

    public String getParam() {
        return this.param;
    }

    public String getCode() {
        return this.code;
    }
}
