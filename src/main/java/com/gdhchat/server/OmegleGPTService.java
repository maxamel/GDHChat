package com.gdhchat.server;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gdhchat.server.response.ChatGPTResponse;
import com.gdhchat.server.response.ChatGPTResponseError;
import com.gdhchat.server.response.ChatGPTResponseSuccess;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static com.gdhchat.client.ClientConstants.ROLE_SYSTEM;
import static com.gdhchat.server.ServerConstants.SUMMARIZED_CONTEXT;
import static com.gdhchat.server.ServerConstants.SUMMARY_PROMPT;


public class OmegleGPTService {

	private static OmegleGPTService service = null;
	private boolean debug = false;
	private ChatGPTConnector chatgpt;
	private List<ObjectNode> chatHistory = new ArrayList<>();

	private int unsummarizedMessageStreak = 0;
	// how many non-system messages to summarize each time a summarization is run
	private static int messagesBatchSizeToSummarize = 10;
	// how many non-system messages to accumulate before running summarization
	private static int messagesUnsummarizedStreakThreshold = 20;
	// how many messages to accumulate in total before starting to drop summaries
	private static int messagesSummaryDropThreshold = 100;

	private String status = ServerConstants.STATUS_OFFLINE;

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

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @param msg - query for ChatGPT, which will be sent alongside the chat history for converstation context
	 * @param role - the role of the user sending the message
	 * @return Response from ChatGPT
	 */
	public ChatGPTResponse sendChatGPTMessage(String msg, String role) {
		ChatGPTResponse chatGPTResponse;
		if (unsummarizedMessageStreak > messagesUnsummarizedStreakThreshold) {
			summarizeMessages();
		}
		try {
			String response = getConnector().sendMessage(msg, role, chatHistory);
			if (debug)
				System.out.println("Received ChatGPT response: " + response);
			// add user message to chat history after it was processed successfully
			addMessageToHistory(msg, role);
			if (!role.equals(ROLE_SYSTEM)) {
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

	/**
	 * 	Summarize the messages in chatHistory according to the parameters:
	 * 	unsummarizedMessageStreak, messagesBatchSizeToSummarize, messagesSummaryDropThreshold
	 * 	It works by summarizing the first messagesBatchSizeToSummarize non-system messages
	 * 	In addition, if messagesSummaryDropThreshold is crossed we also drop a single summary system message
	 */
	private void summarizeMessages() {
		int current = 1;
		List<ObjectNode> chatToSummarize = new ArrayList<>();
		List<ObjectNode> newChatHistory = new ArrayList<>();
		// first configuration message (connection message always stays)
		newChatHistory.add(chatHistory.getFirst());
		// collect messages to summarize
		while (chatToSummarize.size() < messagesBatchSizeToSummarize && current < chatHistory.size()) {
			ObjectNode node = chatHistory.get(current);
			// summarize only user/assistant messages
			if (node.get("role").asText().equals(ROLE_SYSTEM)) {
				newChatHistory.add(node);
				current++;
				continue;
			}
			chatToSummarize.add(node);
			current++;
		}
		if (current < messagesBatchSizeToSummarize) {
			System.out.println("Not enough messages to summarize so far");
			return;
		}
		// request summary
		ChatGPTResponse chatGPTResponse;
		try {
			String response = getConnector().sendMessage(SUMMARY_PROMPT, ROLE_SYSTEM, chatToSummarize);
			chatGPTResponse = new ChatGPTResponseSuccess();
			chatGPTResponse.parse(response);
		} catch (Exception e) {
			System.out.println(e.getMessage() + " " + e.getCause());
			chatGPTResponse = new ChatGPTResponseError();
			chatGPTResponse.parse(e.getMessage());
		}
		if (chatGPTResponse.getStatus().equals(ServerConstants.ResponseStatus.SUCCESS)) {
			ObjectNode payload = new ObjectMapper().createObjectNode();
			payload.put("role", ROLE_SYSTEM);
			payload.put("content", SUMMARIZED_CONTEXT + chatGPTResponse.getMessage());
			newChatHistory.add(payload);
		} else {
			System.out.println("Summarization task failed so we are dropping the unsummarized messages");
		}
		unsummarizedMessageStreak -= messagesBatchSizeToSummarize;
		// copy the rest of the messages as-is to the new chat history
		while (current < chatHistory.size()) {
			newChatHistory.add(chatHistory.get(current));
			current++;
		}
		chatHistory = newChatHistory;
		if (chatHistory.size() > messagesSummaryDropThreshold) {
			// drop one summary
			int index = 5;
			while (index < chatHistory.size()) {
				ObjectNode node = chatHistory.get(index);
				if (node.get("role").asText().equals(ROLE_SYSTEM) && node.get("content").asText().startsWith(SUMMARIZED_CONTEXT)) {
					chatHistory.remove(index);
					break;
				}
				index++;
			}
		}
	}

	/**
	 * 	Adds message to chat history to retain conversation context
	 * @param content - the message body
	 * @param role - The role behind the message. For user it is user/system, for chatgpt it is assistant
	 */
	private void addMessageToHistory(String content, String role) {
		ObjectNode payload = new ObjectMapper().createObjectNode();
		payload.put("role", role);
		payload.put("content", content);
		chatHistory.add(payload);
	}

	public void destroy() {
		chatHistory.clear();
		status = ServerConstants.STATUS_OFFLINE;
	}

    public String getStatus() {
        return status;
    }
}
