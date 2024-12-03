package com.gdhchat.testgen;

import static com.gdhchat.client.ClientConstants.*;
import static com.gdhchat.server.ServerConstants.SUMMARIZED_CONTEXT;
import static com.gdhchat.server.ServerConstants.SUMMARY_PROMPT;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gdhchat.client.ClientConstants;
import com.gdhchat.server.ChatGPTConnector;
import com.gdhchat.server.response.ChatGPTResponse;
import com.gdhchat.server.response.ChatGPTResponseError;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;
import org.mockito.Mockito;


import com.gdhchat.server.OmegleGPTService;
import com.gdhchat.server.ServerConstants;

import static org.mockito.Mockito.*;

public class TestService {
	
	@Test
	public void testOmegleServiceSingleton() {
		OmegleGPTService service1 = OmegleGPTService.getInstance("apiKey");
		OmegleGPTService service2 = OmegleGPTService.getInstance("apiKey");
		assertEquals(service1, service2);
	}

	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
	public void testRateLimitMessage() {
		String jsonAnswer = "{\n" +
				"    \"error\": {\n" +
				"        \"message\": \"You exceeded your current quota, please check your plan and billing details.\",\n" +
				"        \"type\": \"invalid_request_error\",\n" +
				"        \"param\": null,\n" +
				"        \"code\": \"rate_limit_exceeded\"\n" +
				"    }\n" +
				"}";
		try {
			OmegleGPTService mockService = Mockito.spy(OmegleGPTService.getInstance("apiKey"));
			ChatGPTConnector mockedConnector = Mockito.mock(ChatGPTConnector.class);
			when(mockedConnector.sendMessage("aloha", ROLE_USER, new ArrayList<>())).thenThrow(new RuntimeException(jsonAnswer));
			doReturn(mockedConnector).when(mockService).getConnector();

			ChatGPTResponse response = mockService.sendChatGPTMessage("aloha", ROLE_USER);
			assertTrue(response.getMessage().startsWith("You exceeded your current quota"));
			assertTrue(((ChatGPTResponseError)response).getType().equals("invalid_request_error"));
			assertTrue(((ChatGPTResponseError)response).getCode().equals("rate_limit_exceeded"));
			assertTrue(response.getStatus().equals(ServerConstants.ResponseStatus.FAIL));
			mockService.destroy();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
	public void testRealBadKeyMessage() {
		try {
			OmegleGPTService service = OmegleGPTService.getInstance("apiKey");
			ChatGPTResponse response = service.sendChatGPTMessage("aloha", ROLE_USER);
			assertTrue(response.getMessage().startsWith("Incorrect API key provided"));
			assertTrue(((ChatGPTResponseError)response).getType().equals("invalid_request_error"));
			assertTrue(((ChatGPTResponseError)response).getCode().equals("invalid_api_key"));
			assertTrue(response.getStatus().equals(ServerConstants.ResponseStatus.FAIL));
			service.destroy();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
	public void testSummary() {
		OmegleGPTService mockService = Mockito.spy(OmegleGPTService.getInstance("apiKey"));
		try {
			// set thresholds for test only
			Field messagesBatchSizeToSummarize = OmegleGPTService.class.getDeclaredField("messagesBatchSizeToSummarize");
			messagesBatchSizeToSummarize.setAccessible(true); // Bypass the private modifier
			messagesBatchSizeToSummarize.set(mockService, 5);

			Field messagesUnsummarizedStreakThreshold = OmegleGPTService.class.getDeclaredField("messagesUnsummarizedStreakThreshold");
			messagesUnsummarizedStreakThreshold.setAccessible(true); // Bypass the private modifier
			messagesUnsummarizedStreakThreshold.set(mockService, 10);

			Field unsummarizedMessageStreak = OmegleGPTService.class.getDeclaredField("unsummarizedMessageStreak");
			unsummarizedMessageStreak.setAccessible(true); // Bypass the private modifier
			unsummarizedMessageStreak.set(mockService, 11);

			Field messagesSummaryDropThreshold = OmegleGPTService.class.getDeclaredField("messagesSummaryDropThreshold");
			messagesSummaryDropThreshold.setAccessible(true); // Bypass the private modifier
			messagesSummaryDropThreshold.set(mockService, 10);

			// populate fake context
			List<ObjectNode> listOfMessages = new ArrayList<>();
			populateMessages(listOfMessages);
			Field chatHistory = OmegleGPTService.class.getDeclaredField("chatHistory");
			chatHistory.setAccessible(true);
			chatHistory.set(mockService, listOfMessages);

			String summaryAnswer = "{\n" +
					"  \"id\": \"chatcmpl-tttt\",\n" +
					"  \"object\": \"chat.completion\",\n" +
					"  \"created\": 1732991613,\n" +
					"  \"model\": \"gpt-4o-mini-2024-07-18\",\n" +
					"  \"choices\": [\n" +
					"    {\n" +
					"      \"index\": 0,\n" +
					"      \"message\": {\n" +
					"        \"role\": \"assistant\",\n" +
					"        \"content\": \"This is the conversation summary\",\n" +
					"        \"refusal\": null\n" +
					"      },\n" +
					"      \"logprobs\": null,\n" +
					"      \"finish_reason\": \"stop\"\n" +
					"    }\n" +
					"  ],\n" +
					"  \"usage\": {\n" +
					"    \"prompt_tokens\": 133,\n" +
					"    \"completion_tokens\": 4,\n" +
					"    \"total_tokens\": 137,\n" +
					"    \"prompt_tokens_details\": {\n" +
					"      \"cached_tokens\": 0,\n" +
					"      \"audio_tokens\": 0\n" +
					"    },\n" +
					"    \"completion_tokens_details\": {\n" +
					"      \"reasoning_tokens\": 0,\n" +
					"      \"audio_tokens\": 0,\n" +
					"      \"accepted_prediction_tokens\": 0,\n" +
					"      \"rejected_prediction_tokens\": 0\n" +
					"    }\n" +
					"  },\n" +
					"  \"system_fingerprint\": \"fp_111\"\n" +
					"}";
			String jsonAnswer = "{\n" +
					"  \"id\": \"chatcmpl-tttt\",\n" +
					"  \"object\": \"chat.completion\",\n" +
					"  \"created\": 1732991613,\n" +
					"  \"model\": \"gpt-4o-mini-2024-07-18\",\n" +
					"  \"choices\": [\n" +
					"    {\n" +
					"      \"index\": 0,\n" +
					"      \"message\": {\n" +
					"        \"role\": \"assistant\",\n" +
					"        \"content\": \"This is the last message\",\n" +
					"        \"refusal\": null\n" +
					"      },\n" +
					"      \"logprobs\": null,\n" +
					"      \"finish_reason\": \"stop\"\n" +
					"    }\n" +
					"  ],\n" +
					"  \"usage\": {\n" +
					"    \"prompt_tokens\": 133,\n" +
					"    \"completion_tokens\": 4,\n" +
					"    \"total_tokens\": 137,\n" +
					"    \"prompt_tokens_details\": {\n" +
					"      \"cached_tokens\": 0,\n" +
					"      \"audio_tokens\": 0\n" +
					"    },\n" +
					"    \"completion_tokens_details\": {\n" +
					"      \"reasoning_tokens\": 0,\n" +
					"      \"audio_tokens\": 0,\n" +
					"      \"accepted_prediction_tokens\": 0,\n" +
					"      \"rejected_prediction_tokens\": 0\n" +
					"    }\n" +
					"  },\n" +
					"  \"system_fingerprint\": \"fp_111\"\n" +
					"}";

			ChatGPTConnector mockedConnector = Mockito.mock(ChatGPTConnector.class);
			when(mockedConnector.sendMessage(eq(SUMMARY_PROMPT), eq(ROLE_SYSTEM), anyList())).thenReturn(summaryAnswer);
			when(mockedConnector.sendMessage(eq("hello again"), eq(ROLE_USER), anyList())).thenReturn(jsonAnswer);
			doReturn(mockedConnector).when(mockService).getConnector();

			mockService.sendChatGPTMessage("hello again", ROLE_USER);
			List<ObjectNode> fieldValue = (List<ObjectNode>) chatHistory.get(mockService);

			ObjectNode first = fieldValue.getFirst();
			ObjectNode second = fieldValue.get(1);
			ObjectNode third = fieldValue.get(2);
			ObjectNode last = fieldValue.getLast();
            assertEquals(9, fieldValue.size());
            assertEquals("This is the first message", first.get("content").asText());
            assertEquals(ROLE_SYSTEM, first.get("role").asText());
            assertEquals("Set name to Drake", second.get("content").asText());
            assertEquals(ROLE_SYSTEM, second.get("role").asText());
			assertTrue(third.get("content").asText().startsWith("Summarized context:"));
            assertEquals(ROLE_SYSTEM, third.get("role").asText());
            assertEquals("This is the last message", last.get("content").asText());
            assertEquals(ROLE_ASSISTANT, last.get("role").asText());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
	public void testDropSummary() {
		OmegleGPTService mockService = Mockito.spy(OmegleGPTService.getInstance("apiKey"));
		try {
			// set thresholds for test only
			Field messagesBatchSizeToSummarize = OmegleGPTService.class.getDeclaredField("messagesBatchSizeToSummarize");
			messagesBatchSizeToSummarize.setAccessible(true); // Bypass the private modifier
			messagesBatchSizeToSummarize.set(mockService, 5);

			Field messagesUnsummarizedStreakThreshold = OmegleGPTService.class.getDeclaredField("messagesUnsummarizedStreakThreshold");
			messagesUnsummarizedStreakThreshold.setAccessible(true); // Bypass the private modifier
			messagesUnsummarizedStreakThreshold.set(mockService, 10);

			Field unsummarizedMessageStreak = OmegleGPTService.class.getDeclaredField("unsummarizedMessageStreak");
			unsummarizedMessageStreak.setAccessible(true); // Bypass the private modifier
			unsummarizedMessageStreak.set(mockService, 11);

			Field messagesSummaryDropThreshold = OmegleGPTService.class.getDeclaredField("messagesSummaryDropThreshold");
			messagesSummaryDropThreshold.setAccessible(true); // Bypass the private modifier
			messagesSummaryDropThreshold.set(mockService, 10);

			// populate fake context
			List<ObjectNode> listOfMessages = new ArrayList<>();
			populateSummaries(listOfMessages);
			Field chatHistory = OmegleGPTService.class.getDeclaredField("chatHistory");
			chatHistory.setAccessible(true);
			chatHistory.set(mockService, listOfMessages);

			String summaryAnswer = "{\n" +
					"  \"id\": \"chatcmpl-tttt\",\n" +
					"  \"object\": \"chat.completion\",\n" +
					"  \"created\": 1732991613,\n" +
					"  \"model\": \"gpt-4o-mini-2024-07-18\",\n" +
					"  \"choices\": [\n" +
					"    {\n" +
					"      \"index\": 0,\n" +
					"      \"message\": {\n" +
					"        \"role\": \"assistant\",\n" +
					"        \"content\": \"This is the conversation summary\",\n" +
					"        \"refusal\": null\n" +
					"      },\n" +
					"      \"logprobs\": null,\n" +
					"      \"finish_reason\": \"stop\"\n" +
					"    }\n" +
					"  ],\n" +
					"  \"usage\": {\n" +
					"    \"prompt_tokens\": 133,\n" +
					"    \"completion_tokens\": 4,\n" +
					"    \"total_tokens\": 137,\n" +
					"    \"prompt_tokens_details\": {\n" +
					"      \"cached_tokens\": 0,\n" +
					"      \"audio_tokens\": 0\n" +
					"    },\n" +
					"    \"completion_tokens_details\": {\n" +
					"      \"reasoning_tokens\": 0,\n" +
					"      \"audio_tokens\": 0,\n" +
					"      \"accepted_prediction_tokens\": 0,\n" +
					"      \"rejected_prediction_tokens\": 0\n" +
					"    }\n" +
					"  },\n" +
					"  \"system_fingerprint\": \"fp_111\"\n" +
					"}";
			String jsonAnswer = "{\n" +
					"  \"id\": \"chatcmpl-tttt\",\n" +
					"  \"object\": \"chat.completion\",\n" +
					"  \"created\": 1732991613,\n" +
					"  \"model\": \"gpt-4o-mini-2024-07-18\",\n" +
					"  \"choices\": [\n" +
					"    {\n" +
					"      \"index\": 0,\n" +
					"      \"message\": {\n" +
					"        \"role\": \"assistant\",\n" +
					"        \"content\": \"This is the last message\",\n" +
					"        \"refusal\": null\n" +
					"      },\n" +
					"      \"logprobs\": null,\n" +
					"      \"finish_reason\": \"stop\"\n" +
					"    }\n" +
					"  ],\n" +
					"  \"usage\": {\n" +
					"    \"prompt_tokens\": 133,\n" +
					"    \"completion_tokens\": 4,\n" +
					"    \"total_tokens\": 137,\n" +
					"    \"prompt_tokens_details\": {\n" +
					"      \"cached_tokens\": 0,\n" +
					"      \"audio_tokens\": 0\n" +
					"    },\n" +
					"    \"completion_tokens_details\": {\n" +
					"      \"reasoning_tokens\": 0,\n" +
					"      \"audio_tokens\": 0,\n" +
					"      \"accepted_prediction_tokens\": 0,\n" +
					"      \"rejected_prediction_tokens\": 0\n" +
					"    }\n" +
					"  },\n" +
					"  \"system_fingerprint\": \"fp_111\"\n" +
					"}";

			ChatGPTConnector mockedConnector = Mockito.mock(ChatGPTConnector.class);
			when(mockedConnector.sendMessage(eq(SUMMARY_PROMPT), eq(ROLE_SYSTEM), anyList())).thenReturn(summaryAnswer);
			when(mockedConnector.sendMessage(eq("hello again"), eq(ROLE_USER), anyList())).thenReturn(jsonAnswer);
			doReturn(mockedConnector).when(mockService).getConnector();

			List<ObjectNode> fieldValue = (List<ObjectNode>) chatHistory.get(mockService);
			ObjectNode fourth_summary = fieldValue.get(5);
			assertTrue(fourth_summary.get("content").asText().equals(SUMMARIZED_CONTEXT+"This is the #4 summary"));
			mockService.sendChatGPTMessage("hello again", ROLE_USER);
			// assert the fourth summary was dropped
			fieldValue = (List<ObjectNode>) chatHistory.get(mockService);
			fourth_summary = fieldValue.get(5);
			assertTrue(fourth_summary.get("content").asText().equals(SUMMARIZED_CONTEXT+"This is the #5 summary"));

			ObjectNode first = fieldValue.getFirst();
			ObjectNode second = fieldValue.get(1);
			ObjectNode last = fieldValue.getLast();
			assertTrue(fieldValue.size() == 15);
			assertTrue(first.get("content").asText().equals("This is the first message"));
			assertTrue(first.get("role").asText().equals(ROLE_SYSTEM));
			assertTrue(second.get("content").asText().equals(SUMMARIZED_CONTEXT+"This is the #0 summary"));
			assertTrue(second.get("role").asText().equals(ROLE_SYSTEM));
			assertTrue(last.get("content").asText().equals("This is the last message"));
			assertTrue(last.get("role").asText().equals(ROLE_ASSISTANT));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
	public void testParseSimpleConnected() {
		String jsonAnswer = "{\n" +
				"  \"id\": \"chatcmpl-tttt\",\n" +
				"  \"object\": \"chat.completion\",\n" +
				"  \"created\": 1732991613,\n" +
				"  \"model\": \"gpt-4o-mini-2024-07-18\",\n" +
				"  \"choices\": [\n" +
				"    {\n" +
				"      \"index\": 0,\n" +
				"      \"message\": {\n" +
				"        \"role\": \"assistant\",\n" +
				"        \"content\": \"OmegleStart\",\n" +
				"        \"refusal\": null\n" +
				"      },\n" +
				"      \"logprobs\": null,\n" +
				"      \"finish_reason\": \"stop\"\n" +
				"    }\n" +
				"  ],\n" +
				"  \"usage\": {\n" +
				"    \"prompt_tokens\": 133,\n" +
				"    \"completion_tokens\": 4,\n" +
				"    \"total_tokens\": 137,\n" +
				"    \"prompt_tokens_details\": {\n" +
				"      \"cached_tokens\": 0,\n" +
				"      \"audio_tokens\": 0\n" +
				"    },\n" +
				"    \"completion_tokens_details\": {\n" +
				"      \"reasoning_tokens\": 0,\n" +
				"      \"audio_tokens\": 0,\n" +
				"      \"accepted_prediction_tokens\": 0,\n" +
				"      \"rejected_prediction_tokens\": 0\n" +
				"    }\n" +
				"  },\n" +
				"  \"system_fingerprint\": \"fp_111\"\n" +
				"}";
		try {
			OmegleGPTService mockService = Mockito.spy(OmegleGPTService.getInstance("apiKey"));
			ChatGPTConnector mockedConnector = Mockito.mock(ChatGPTConnector.class);
			when(mockedConnector.sendMessage("hello", ROLE_USER, new ArrayList<>())).thenReturn(jsonAnswer);
			doReturn(mockedConnector).when(mockService).getConnector();

			ChatGPTResponse response = mockService.sendChatGPTMessage("hello", ROLE_USER);
			assertTrue(response.getMessage().equals(ClientConstants.OMEGLE_START));
			assertTrue(response.getStatus().equals(ServerConstants.ResponseStatus.SUCCESS));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	// populate eleven messages into the messages array
	private void populateMessages(List<ObjectNode> messages) {
		ObjectNode payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_SYSTEM);
		payload.put("content", "This is the first message");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_ASSISTANT);
		payload.put("content", "Hello");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_USER);
		payload.put("content", "What is your name?");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_SYSTEM);
		payload.put("content", "Set name to Drake");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_USER);
		payload.put("content", "Where do you live?");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_ASSISTANT);
		payload.put("content", "I live in Australia");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_USER);
		payload.put("content", "What is your occupation?");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_ASSISTANT);
		payload.put("content", "I work as a woodcutter");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_USER);
		payload.put("content", "How old are you?");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_ASSISTANT);
		payload.put("content", "I am 25 years old");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_USER);
		payload.put("content", "It's very nice to meet you");
		messages.add(payload);
	}

	// populate eleven messages into the messages array
	private void populateSummaries(List<ObjectNode> messages) {
		ObjectNode payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_SYSTEM);
		payload.put("content", "This is the first message");
		messages.add(payload);
		for (int i=0; i<10; i++) {
			payload = new ObjectMapper().createObjectNode();
			payload.put("role", ROLE_SYSTEM);
			payload.put("content", SUMMARIZED_CONTEXT+"This is the #" + i + " summary");
			messages.add(payload);
		}
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_USER);
		payload.put("content", "Where do you live?");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_ASSISTANT);
		payload.put("content", "I live in Australia");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_USER);
		payload.put("content", "What is your occupation?");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_ASSISTANT);
		payload.put("content", "I work as a woodcutter");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_USER);
		payload.put("content", "How old are you?");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_ASSISTANT);
		payload.put("content", "I am 25 years old");
		messages.add(payload);
		payload = new ObjectMapper().createObjectNode();
		payload.put("role", ROLE_USER);
		payload.put("content", "It's very nice to meet you");
		messages.add(payload);
	}
}
