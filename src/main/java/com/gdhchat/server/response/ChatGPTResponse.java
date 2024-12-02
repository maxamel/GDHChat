package com.gdhchat.server.response;

import com.gdhchat.server.ServerConstants;

public abstract class ChatGPTResponse {

    String message;
    ServerConstants.ResponseStatus status;

    public abstract void parse(String errorMessage);

    public ServerConstants.ResponseStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
