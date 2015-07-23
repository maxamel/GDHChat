package test.java.gen;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;

import main.java.server.OmegleService;
import main.java.server.ServerConstants;

import org.junit.Test;
import org.mockito.Mockito;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import static org.mockito.Mockito.*;

public class ServiceTester {
	
	@Test
	public void testOmegleServiceSingleton() {
		OmegleService service1 = OmegleService.getInstance();
		OmegleService service2 = OmegleService.getInstance();
		assertEquals(service1, service2);
	}

	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testParseComplexConnected() {
		OmegleService service = OmegleService.getInstance();
		
		String jsonAnswer = "{\"events\":[[\"waiting\"],[\"connected\"],[\"commonLikes\",[\"israel\"] ],[\"gotMessage\",\"hi\"]] }";
		try {
			Class<?>[] cArg = new Class[1];
	        cArg[0] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, jsonAnswer);
			assertEquals(service.getLikes(),"israel");
			assertTrue(service.getMsgs().contains("hi") && service.getMsgs().size() == 1);
			//assertTrue(findEvent(service, ServerConstants.EVENT_GOTMESSAGE));
			//assertTrue(findEvent(service, ServerConstants.EVENT_CONNECTED));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 
		OmegleService.destroy();
	}
	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testParseSimpleMessage() {
		OmegleService service = OmegleService.getInstance();
		
		String jsonAnswer = "[[\"gotMessage\",\"hi\"]] ";
		try {
			Class<?>[] cArg = new Class[1];
	        cArg[0] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, jsonAnswer);
			assertTrue(service.getMsgs().contains("hi"));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		OmegleService.destroy();
	}
	
	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testParseManyLikes() {
		OmegleService service = OmegleService.getInstance();
		
		String jsonAnswer =  "[[\"connected\"],[\"commonLikes\",[\"Israel,Russia,Afghanistan\"] ],[\"gotMessage\",\"hi\"]] }";
		try {
			Class<?>[] cArg = new Class[1];
	        cArg[0] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, jsonAnswer);
			assertTrue(service.getMsgs().contains("hi") && service.getMsgs().size() == 1);
			assertTrue(service.getLikes().contains("Israel")&&service.getLikes().contains("Russia")&&service.getLikes().contains("Afghanistan"));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		OmegleService.destroy();
	}
	
	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testParseComplexDisconnection() {
		OmegleService service = OmegleService.getInstance();
		
		String jsonAnswer = "[[connected],[\"gotMessage\",\"hi\"],[\"strangerDisconnected\"]]";
		try {
			Class<?>[] cArg = new Class[1];
	        cArg[0] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, jsonAnswer);
			assertTrue(service.getStatus().equals(ServerConstants.STATUS_OFFLINE));
			assertTrue(service.getMsgs().isEmpty());
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		OmegleService.destroy();
	}
	
	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testParseSimpleConnected() {
		OmegleService service = OmegleService.getInstance();
		
		String jsonAnswer = "[[\"connected\"]]";
		try {
			Class<?>[] cArg = new Class[1];
	        cArg[0] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, jsonAnswer);
			assertTrue(service.getStatus().equals(ServerConstants.STATUS_ONLINE));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		OmegleService.destroy();
	}
	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testParseSimpleDisconnected() {
		OmegleService service = OmegleService.getInstance();
		
		String jsonAnswer = "[[\"strangerDisconnected\"],[\"statusInfo\"]]";
		try {
			Class<?>[] cArg = new Class[1];
	        cArg[0] = String.class;
			Method method = service.getClass().getDeclaredMethod("processJson",cArg);
			method.setAccessible(true);
			method.invoke(service, jsonAnswer);
			assertTrue(service.getStatus().equals(ServerConstants.STATUS_OFFLINE));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		OmegleService.destroy();
	}
	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testPollingEventsDisconnect() {
		OmegleService mockService = Mockito.spy(OmegleService.getInstance());
		ArrayList<String> list = new ArrayList<String>() ;
		list.add(ServerConstants.EVENT_DISCONNECT);
		when(mockService.sendOmegleHttpRequest(ServerConstants.URL_EVENT, null)).thenReturn(list);
		when(mockService.getCurrEvent()).thenCallRealMethod();
		try {
			org.powermock.reflect.Whitebox.invokeMethod(mockService, "pollEvent");
			assertTrue(mockService.getCurrEvent().equals(ServerConstants.EVENT_DISCONNECT));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		OmegleService.destroy();
	}
	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testPollingEventsConnect() {
		OmegleService mockService = Mockito.spy(OmegleService.getInstance());
		ArrayList<String> list = new ArrayList<String>() ;
		list.add(ServerConstants.EVENT_CONNECTED);
		when(mockService.sendOmegleHttpRequest(ServerConstants.URL_EVENT, null)).thenReturn(list);
		when(mockService.getCurrEvent()).thenCallRealMethod();
		try {
			org.powermock.reflect.Whitebox.invokeMethod(mockService, "pollEvent");
			assertTrue(mockService.getCurrEvent().equals(ServerConstants.EVENT_CONNECTED));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		OmegleService.destroy();
	}
	@Test
	@SuppressFBWarnings(value="REC_CATCH_EXCEPTION")
	public void testPollingEventsMessage() {
		OmegleService mockService = Mockito.spy(OmegleService.getInstance());
		ArrayList<String> list = new ArrayList<String>() ;
		list.add(ServerConstants.EVENT_GOTMESSAGE);
		when(mockService.sendOmegleHttpRequest(ServerConstants.URL_EVENT, null)).thenReturn(list);
		when(mockService.getCurrEvent()).thenCallRealMethod();
		try {
			org.powermock.reflect.Whitebox.invokeMethod(mockService, "pollEvent");
			assertTrue(mockService.getCurrEvent().equals(ServerConstants.EVENT_GOTMESSAGE));
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		OmegleService.destroy();
	}
}
