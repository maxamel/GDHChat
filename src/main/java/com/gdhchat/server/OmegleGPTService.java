package com.gdhchat.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gdhchat.server.response.ChatGPTResponse;
import com.gdhchat.server.response.ChatGPTResponseError;
import com.gdhchat.server.response.ChatGPTResponseSuccess;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static com.gdhchat.server.ServerConstants.SUMMARY_PROMPT;


public class OmegleGPTService {
	private static OmegleGPTService service = null;
	private ChatGPTConnector chatgpt;
	private static List<ObjectNode> chatHistory = new ArrayList<>();
	private static int unsummarizedMessageStreak = 0;
	private ConcurrentLinkedQueue<String> currEvents = new ConcurrentLinkedQueue<String>();
	private String status = ServerConstants.STATUS_OFFLINE;
	private String likes = "";
	private int timeouts = -1;
	
	/**
	 * Get the service instance currently used. If it's null create it.
	 * @return the service itself
	 */
	@SuppressFBWarnings(value = "MS_EXPOSE_REP")
	public static synchronized OmegleGPTService getInstance(String apiKey)
	{
		if (service == null)
			service = new OmegleGPTService(apiKey);
		return service;
	}
	
	private OmegleGPTService(String apiKey)
	{
		this.chatgpt = new ChatGPTConnector(apiKey);
	}

	public ChatGPTConnector getConnector() {
		return this.chatgpt;
	}

	public String getLikes() {
		return likes;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCurrEvent() {
		return currEvents.poll();
	}

	public int getTimeouts() {
		return timeouts;
	}

	/**
	 * @param msg - query for ChatGPT, which will be send alongside the chat history for converstation context
	 * @param role - the role of the user sending the message
	 * @return Response from ChatGPT
	 */
	public ChatGPTResponse sendChatGPTMessage(String msg, String role) {
		ChatGPTResponse chatGPTResponse;
		if (unsummarizedMessageStreak > 20) {
			summarizeMessages();
		}
		try {
			String response = getConnector().sendMessage(msg, role, chatHistory);
			System.out.println("Received back ChatGPT response: " + response);
			// add user message to chat history after it was processed successfully
			addMessageToHistory(msg, role);
			if (role.equals("user")) {
				unsummarizedMessageStreak++;
			}
			chatGPTResponse = new ChatGPTResponseSuccess();
			chatGPTResponse.parse(response);
			// add chatgpt message to chat history after it we verified it succeeded
			addMessageToHistory(chatGPTResponse.getMessage(), ((ChatGPTResponseSuccess)chatGPTResponse).getRole());
		} catch (Exception e) {
			System.out.println(e.getMessage() + " " + e.getCause());
			chatGPTResponse = new ChatGPTResponseError();
			chatGPTResponse.parse(e.getMessage());
		}
		return chatGPTResponse;
	}

	private void summarizeMessages() {
		int limit = 10;
		int current = 1;
		List<ObjectNode> chatToSummarize = new ArrayList<>();
		List<ObjectNode> newChatHistory = new ArrayList<>();
		// first configuration message (connection message always stays)
		newChatHistory.add(chatHistory.getFirst());
		// collect messages to summarize
		while (current < limit && current < chatHistory.size()) {
			ObjectNode node = chatHistory.get(current);
			// summarize only user messages
			if (node.get("role").equals("system")) {
				newChatHistory.add(node);
				current++;
				continue;
			}
			chatToSummarize.add(node);
			current++;
		}
		if (current < limit) {
			System.out.println("Not enough messages to summarize so far");
			return;
		}
		// request summary
		ChatGPTResponse chatGPTResponse;
		try {
			String response = getConnector().sendMessage(SUMMARY_PROMPT, "system", chatToSummarize);
			chatGPTResponse = new ChatGPTResponseSuccess();
			chatGPTResponse.parse(response);
		} catch (Exception e) {
			System.out.println(e.getMessage() + " " + e.getCause());
			chatGPTResponse = new ChatGPTResponseError();
			chatGPTResponse.parse(e.getMessage());
		}
		if (chatGPTResponse.getStatus().equals(ServerConstants.ResponseStatus.SUCCESS)) {
			ObjectNode payload = new ObjectMapper().createObjectNode();
			payload.put("role", "system");
			payload.put("content", "Summarized context:" + chatGPTResponse.getMessage());
			newChatHistory.add(payload);
		} else {
			System.out.println("Summarization task failed so we are dropping the unsummarized messages");
		}
		unsummarizedMessageStreak -= limit;
		// copy the rest of the messages as-is to the new chat history
		while (current < chatHistory.size()) {
			newChatHistory.add(chatHistory.get(current));
			current++;
		}
		chatHistory = newChatHistory;

	}

	/**
	 * 	Adds message to chat history to retain conversation context
	 * @param content - the message body
	 * @param role - The role behind the message. For user it is user/system, for chatgpt it is assistant
	 */
	private static void addMessageToHistory(String content, String role) {
		ObjectNode payload = new ObjectMapper().createObjectNode();
		payload.put("role", role);
		payload.put("content", content);
		chatHistory.add(payload);
	}

	public void destroy() {
		timeouts = -1;
		chatHistory.clear();
		currEvents.clear();
		likes = "";
		status = ServerConstants.STATUS_OFFLINE;
	}
}
