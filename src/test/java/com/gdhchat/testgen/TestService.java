package com.gdhchat.testgen;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;

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
	public void testParseComplexConnected() {
		OmegleGPTService service = OmegleGPTService.getInstance("apiKey");
		String jsonAnswer = "{\"events\":[[\"waiting\"],[\"connected\"],[\"commonLikes\",[\"israel\"] ],[\"gotMessage\",\"hi\"]] }";
		try {
			Class<?>[] cArg = new Class[2];
	        cArg[0] = String.class;
	        cArg[1] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, "", jsonAnswer);
			assertEquals(service.getLikes(),"israel");
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		service.destroy();
	}
	@Test
	public void testParseSimpleMessage() {
		OmegleGPTService service = OmegleGPTService.getInstance("apiKey");
		String jsonAnswer = "[[\"gotMessage\",\"hi\"]] ";
		try {
			Class<?>[] cArg = new Class[2];
	        cArg[0] = String.class;
	        cArg[1] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, "", jsonAnswer);
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		service.destroy();
	}
	
	@Test
	public void testParseManyLikes() {
		OmegleGPTService service = OmegleGPTService.getInstance("apiKey");
		String jsonAnswer =  "[[\"connected\"],[\"commonLikes\",[\"Israel,Russia,Afghanistan\"] ],[\"gotMessage\",\"hi\"]] }";
		try {
			Class<?>[] cArg = new Class[2];
	        cArg[0] = String.class;
	        cArg[1] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, "", jsonAnswer);
			assertTrue(service.getLikes().contains("Israel")&&service.getLikes().contains("Russia")&&service.getLikes().contains("Afghanistan"));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		service.destroy();
	}
	
	@Test
	public void testParseComplexDisconnection() {
		OmegleGPTService service = OmegleGPTService.getInstance("apiKey");
		String jsonAnswer = "[[connected],[\"gotMessage\",\"hi\"],[\"strangerDisconnected\"]]";
		try {
			Class<?>[] cArg = new Class[2];
	        cArg[0] = String.class;
	        cArg[1] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, "", jsonAnswer);
			assertTrue(service.getStatus().equals(ServerConstants.STATUS_OFFLINE));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		service.destroy();
	}

	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
	public void testWrongKeyMessage() {
		OmegleGPTService service = OmegleGPTService.getInstance("apiKey");
		String jsonAnswer = "{\n" +
				"    \"error\": {\n" +
				"        \"message\": \"Incorrect API key provided: sk-proj-*****-. You can find your API key at https://platform.openai.com/account/api-keys.\",\n" +
				"        \"type\": \"invalid_request_error\",\n" +
				"        \"param\": null,\n" +
				"        \"code\": \"invalid_api_key\"\n" +
				"    }\n" +
				"}";
		try {
			OmegleGPTService mockService = Mockito.spy(OmegleGPTService.getInstance("apiKey"));
			ChatGPTConnector mockedConnector = Mockito.mock(ChatGPTConnector.class);
			when(mockedConnector.sendMessage("hello", "user", new ArrayList<>())).thenReturn(jsonAnswer);
			doReturn(mockedConnector).when(mockService).getConnector();

			ChatGPTResponse response = mockService.sendChatGPTMessage("hello", "user");
			assertTrue(response.getMessage().startsWith("Incorrect API key provided"));
			assertTrue(((ChatGPTResponseError)response).getType().equals("invalid_request_error"));
			assertTrue(((ChatGPTResponseError)response).getCode().equals("invalid_api_key"));
			assertTrue(response.getStatus().equals(ServerConstants.ResponseStatus.FAIL));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		service.destroy();
	}
	
	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
	public void testParseSimpleConnected() {
		OmegleGPTService service = OmegleGPTService.getInstance("apiKey");
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
			when(mockedConnector.sendMessage("hello", "user", new ArrayList<>())).thenReturn(jsonAnswer);
			doReturn(mockedConnector).when(mockService).getConnector();

			ChatGPTResponse response = mockService.sendChatGPTMessage("hello", "user");
			assertTrue(response.getMessage().equals(ClientConstants.OMEGLE_START));
			assertTrue(response.getStatus().equals(ServerConstants.ResponseStatus.SUCCESS));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		service.destroy();
	}
	@Test
	public void testParseSimpleDisconnected() {
		OmegleGPTService service = OmegleGPTService.getInstance("apiKey");
		
		String jsonAnswer = "[[\"strangerDisconnected\"],[\"statusInfo\"]]";
		try {
			Class<?>[] cArg = new Class[2];
	        cArg[0] = String.class;
	        cArg[1] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, "", jsonAnswer);
			assertTrue(service.getStatus().equals(ServerConstants.STATUS_OFFLINE));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		service.destroy();
	}
	@Test
	public void testPollingEventsDisconnect() {
		OmegleGPTService mockService = Mockito.spy(OmegleGPTService.getInstance("apiKey"));
		when(mockService.sendChatGPTMessage(ServerConstants.URL_EVENT, "user").getMessage()).thenReturn("");
		when(mockService.getCurrEvent()).thenCallRealMethod();
		try {
			org.powermock.reflect.Whitebox.invokeMethod(mockService, "pollEvent");
			assertTrue(mockService.getCurrEvent().equals(ServerConstants.EVENT_DISCONNECT));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		mockService.destroy();
	}
	@Test
	public void testPollingEventsConnect() {
		OmegleGPTService mockService = Mockito.spy(OmegleGPTService.getInstance("apiKey"));
		when(mockService.sendChatGPTMessage(ServerConstants.URL_EVENT, "user").getMessage()).thenReturn("");
		when(mockService.getCurrEvent()).thenCallRealMethod();
		try {
			org.powermock.reflect.Whitebox.invokeMethod(mockService, "pollEvent");
			assertTrue(mockService.getCurrEvent().equals(ServerConstants.EVENT_CONNECTED));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		mockService.destroy();
	}
	@Test
	public void testPollingEventsMessage() {
		OmegleGPTService mockService = Mockito.spy(OmegleGPTService.getInstance("apiKey"));
		when(mockService.sendChatGPTMessage(ServerConstants.URL_EVENT, "user").getMessage()).thenReturn("");
		when(mockService.getCurrEvent()).thenCallRealMethod();
		try {
			org.powermock.reflect.Whitebox.invokeMethod(mockService, "pollEvent");
			assertTrue(mockService.getCurrEvent().equals(ServerConstants.EVENT_GOTMESSAGE));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		mockService.destroy();

	}
	@Test
	public void testOmeglePersistence() {
		OmegleGPTService service = OmegleGPTService.getInstance("apiKey");
		String jsonAnswer =  "[[\"connected\"],[\"commonLikes\",[\"Israel,Russia,Afghanistan\"] ],[\"gotMessage\",\"hi\"]] }";
		try {
			Class<?>[] cArg = new Class[2];
	        cArg[0] = String.class;
	        cArg[1] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, "", jsonAnswer);
			assertTrue(service.getLikes().contains("Israel")&&service.getLikes().contains("Russia")&&service.getLikes().contains("Afghanistan"));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		service.destroy();
		service = OmegleGPTService.getInstance("apiKey");
		assertTrue(service.getLikes().isEmpty());
		assertTrue(service.getStatus().equals(ServerConstants.STATUS_OFFLINE));
		assertTrue(service.getTimeouts() == -1);
	}
}
