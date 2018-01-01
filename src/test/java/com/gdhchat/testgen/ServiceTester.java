package com.gdhchat.testgen;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.Test;
import org.mockito.Mockito;

import com.gdhchat.server.GDHChatService;
import com.gdhchat.server.ServerConstants;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import static org.mockito.Mockito.*;

public class ServiceTester {
	
	@Test
	public void testOmegleServiceSingleton() {
		GDHChatService service1 = GDHChatService.getInstance();
		GDHChatService service2 = GDHChatService.getInstance();
		assertEquals(service1, service2);
	}

	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testParseComplexConnected() {
		GDHChatService service = GDHChatService.getInstance();		
		String jsonAnswer = "{\"events\":[[\"waiting\"],[\"connected\"],[\"commonLikes\",[\"israel\"] ],[\"gotMessage\",\"hi\"]] }";
		try {
			Class<?>[] cArg = new Class[2];
	        cArg[0] = String.class;
	        cArg[1] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, "", jsonAnswer);
			assertEquals(service.getLikes(),"israel");
			assertTrue(service.getMsgs().contains("hi") && service.getMsgs().size() == 1);
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		service.destroy();
	}
	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testParseSimpleMessage() {
		GDHChatService service = GDHChatService.getInstance();	
		String jsonAnswer = "[[\"gotMessage\",\"hi\"]] ";
		try {
			Class<?>[] cArg = new Class[2];
	        cArg[0] = String.class;
	        cArg[1] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, "", jsonAnswer);
			assertTrue(service.getMsgs().contains("hi"));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		service.destroy();
	}
	
	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testParseManyLikes() {
		GDHChatService service = GDHChatService.getInstance();		
		String jsonAnswer =  "[[\"connected\"],[\"commonLikes\",[\"Israel,Russia,Afghanistan\"] ],[\"gotMessage\",\"hi\"]] }";
		try {
			Class<?>[] cArg = new Class[2];
	        cArg[0] = String.class;
	        cArg[1] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, "", jsonAnswer);
			assertTrue(service.getMsgs().contains("hi") && service.getMsgs().size() == 1);
			assertTrue(service.getLikes().contains("Israel")&&service.getLikes().contains("Russia")&&service.getLikes().contains("Afghanistan"));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		service.destroy();
	}
	
	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testParseComplexDisconnection() {
		GDHChatService service = GDHChatService.getInstance();	
		String jsonAnswer = "[[connected],[\"gotMessage\",\"hi\"],[\"strangerDisconnected\"]]";
		try {
			Class<?>[] cArg = new Class[2];
	        cArg[0] = String.class;
	        cArg[1] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, "", jsonAnswer);
			assertTrue(service.getStatus().equals(ServerConstants.STATUS_OFFLINE));
			assertTrue(service.getMsgs().isEmpty());
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		service.destroy();
	}
	
	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testParseSimpleConnected() {
		GDHChatService service = GDHChatService.getInstance();		
		String jsonAnswer = "[[\"connected\"]]";
		try {
			Class<?>[] cArg = new Class[2];
	        cArg[0] = String.class;
	        cArg[1] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, "", jsonAnswer);
			assertTrue(service.getStatus().equals(ServerConstants.STATUS_ONLINE));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		service.destroy();
	}
	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testParseSimpleDisconnected() {
		GDHChatService service = GDHChatService.getInstance();
		
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
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testPollingEventsDisconnect() {
		GDHChatService mockService = Mockito.spy(GDHChatService.getInstance());
		ArrayList<String> list = new ArrayList<String>() ;
		list.add(ServerConstants.EVENT_DISCONNECT);
		when(mockService.sendOmegleMult(ServerConstants.URL_EVENT, null)).thenReturn(list);
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
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testPollingEventsConnect() {
		GDHChatService mockService = Mockito.spy(GDHChatService.getInstance());
		ArrayList<String> list = new ArrayList<String>() ;
		list.add(ServerConstants.EVENT_CONNECTED);
		when(mockService.sendOmegleMult(ServerConstants.URL_EVENT, null)).thenReturn(list);
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
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testPollingEventsMessage() {
		GDHChatService mockService = Mockito.spy(GDHChatService.getInstance());
		ArrayList<String> list = new ArrayList<String>() ;
		list.add(ServerConstants.EVENT_GOTMESSAGE);
		when(mockService.sendOmegleMult(ServerConstants.URL_EVENT, null)).thenReturn(list);
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
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testOmeglePersistence() {
		GDHChatService service = GDHChatService.getInstance();
		String jsonAnswer =  "[[\"connected\"],[\"commonLikes\",[\"Israel,Russia,Afghanistan\"] ],[\"gotMessage\",\"hi\"]] }";
		try {
			Class<?>[] cArg = new Class[2];
	        cArg[0] = String.class;
	        cArg[1] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, "", jsonAnswer);
			assertTrue(service.getMsgs().contains("hi") && service.getMsgs().size() == 1);
			assertTrue(service.getLikes().contains("Israel")&&service.getLikes().contains("Russia")&&service.getLikes().contains("Afghanistan"));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		service.destroy();
		service = GDHChatService.getInstance();
		assertTrue(service.getMsgs().isEmpty());
		assertTrue(service.getLikes().isEmpty());
		assertTrue(service.getStatus().equals(ServerConstants.STATUS_OFFLINE));
		assertTrue(service.getTimeouts() == -1);
	}
	
}
