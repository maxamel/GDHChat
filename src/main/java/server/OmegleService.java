package main.java.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class OmegleService {
	private static OmegleService service = null;
	private String clientId = "";
	private ConcurrentLinkedQueue<String> currEvents = new ConcurrentLinkedQueue<String>();
	private String status = ServerConstants.STATUS_OFFLINE;
	private Thread main = null;
	private ConcurrentLinkedQueue<String> msgs = new ConcurrentLinkedQueue<String>();
	private String likes = "";
	private int timeouts = -1;
	
	/**
	 * Get the service instance currently used. If it's null create it. Note - activation is possible only if the service exists
	 * @return the service itself
	 */
	public static synchronized OmegleService getInstance()
	{
		if (service == null) service = new OmegleService();
		return service;
	}
	
	private OmegleService()
	{
	
	}
	/**
	 * 	Activation of the service itself. Polls Omegle every second for an event.  
	 */
	private void activate()
	{
		if (main == null && service != null)
		{
			main = new Thread(new Runnable() {			
				@Override
				public void run() {
					timeouts++;
					while (status.equals(ServerConstants.STATUS_ONLINE) && !Thread.interrupted())
					{
						pollEvent();
						String event = currEvents.peek();
						if (event != null)
						{						
							if (event.equals(ServerConstants.EVENT_DISCONNECT)) status = ServerConstants.STATUS_OFFLINE;
							else if (event.equals(ServerConstants.EVENT_CONNECTED)) status = ServerConstants.STATUS_ONLINE;
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// interruption can occur when destroying the service - it's fine
						}
					}
				}
			});	
			main.start();
		}
	}
	public ConcurrentLinkedQueue<String> getMsgs() {
		return msgs;
	}
	
	public String getLikes() {
		return likes;
	}

	public String getStatus() {
		return status;
	}

	public String getCurrEvent() {
		return currEvents.poll();
	}

	public int getTimeouts() {
		return timeouts;
	}
	/**
	 *  Poll for an event. Done by performing POST request to Omegle. 
	 * 	If there's an active event it'll be inserted into the queue, otherwise nothing happens
	 */
	private void pollEvent() {	
		if (currEvents.isEmpty() || (currEvents.peek() != null && !currEvents.peek().equals(ServerConstants.EVENT_DISCONNECT))) 
		{
			ArrayList<String> events = (ArrayList<String>) sendOmegleHttpRequest(ServerConstants.URL_EVENT, null);
			if (events != null) 
			{
				for (String s : events)
					currEvents.add(s);
			}
		}
	}
	/**
	 * Send an HTTP request to an Omegle endpoint. Use clientID and attach message if exists. Forward the response to methods ProcessJson or ProcessText accordingly
	 * 
	 * @param endPoint - where to send the request to
	 * @param msg - the contents of the message
	 * @return the possible return values are as described in ProcessJson and ProcessText
	 */
	public Object sendOmegleHttpRequest(String endPoint, String msg)
	{
			URL url = null;
			HttpURLConnection conn = null;
			OutputStream os = null;
			InputStream is = null;
			try {
				url = new URL(endPoint);
				conn = (HttpURLConnection) url.openConnection();
				setHeaders(conn,endPoint,"42");
				//conn.connect();
				conn.setDoOutput(true);
				conn.setReadTimeout(6000);
				os = conn.getOutputStream();
				String toSend = String.join("=", "id",clientId);
				if (msg != null)
				{
					String message = String.join("=", "msg",msg);
					toSend = String.join("&", message,toSend);
				}
				byte[] outputInBytes = toSend.getBytes("UTF-8");
				os.write( outputInBytes );    
				os.close();
				is = conn.getInputStream();
				@SuppressWarnings("resource")
				String res = new Scanner(is,"UTF-8").useDelimiter("\\A").next();
				is.close();	
				conn.disconnect();
				if (res != null && conn.getRequestProperty("Accept").equals("application/json")) return processJson(res);
				else return processText(endPoint,res);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (SocketTimeoutException e) {
				timeouts++;
				if (timeouts > 10) currEvents.add(ServerConstants.EVENT_DISCONNECT);		// auto disconnection upon bad connectivity
			} catch (IOException e) {
				e.printStackTrace();
			} 
		return null;
	}
	
	/**
	 * 	If the request was successful - we destroy or activate the service as needed and return success.
	 * 	@param url - the endpoint from which we got the response
	 * 	@param text - the response text
	 * 	@return Success message if the request was successful, otherwise null
	 */
	private Object processText(String url, String text) 
	{
		if (text != null && text.equals(ServerConstants.SUCCESS_MSG))
		{
			if (url.equals(ServerConstants.URL_DISCONNECT)) 
			{
				status = ServerConstants.STATUS_OFFLINE;
				destroy();
			}
			else if (url.contains(ServerConstants.BASE_URL_BODY) || url.equals(ServerConstants.URL_STOPSEARCH)) 
			{
				status = ServerConstants.STATUS_ONLINE;
				activate();
			}		
			ArrayList<String> list = new ArrayList<String>() {{
			    add(ServerConstants.SUCCESS_MSG);
			}};
			return list;
		}	
		return null;
	}

	/**
	 * Try to look for clientID, connection keyword and common interests in the response message. If none found, try to retrieve the event in it
	 * @param the response in json format
	 * @return Success message or as described in retrieveEvent
	 */
	private Object processJson(String json) 
	{
		String client = "";
		if ((client = retrieveClientId(json)) != null)
		{
			//status = ServerConstants.STATUS_ONLINE;
			clientId = client;
			//activate();
		}
		return retrieveConnAndLikes(json);
	}

	/**
	 * 	Set the headers for the connection. Special cases like POSTing an event are treated a bit differently
	 * 
	 * 	@param conn - an opened connection 
	 * 	@param urlSend - the endpoint of this connection
	 * 	@param contentLen - content length. Could be null for certain occasion in which the default value of 42 is used
	 * 	@throws ProtocolException
	 */
	private void setHeaders(HttpURLConnection conn, String urlSend, String contentLen) throws ProtocolException 
	{
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); 
		conn.setRequestProperty("Content-Length","42");
		conn.setRequestProperty("Accept","text/plain");   
		conn.setRequestProperty("Connection","close");
		conn.setRequestProperty("Origin", "http://www.omegle.com");
		
		if (urlSend.equals(ServerConstants.URL_EVENT) || urlSend.contains(ServerConstants.BASE_URL_BODY))conn.setRequestProperty("Accept","application/json");
		else if (urlSend.equals(ServerConstants.URL_SEND)) conn.setRequestProperty("Content-Length",contentLen);
	}

	/**
	 * 	Try to look for the connect keyword signifying a successful connection and the common likes with the current stranger
	 * 
	 * 	@param json
	 * 	@return null if unsuccessful or the connection keyword as received in the json
	 */
	private Object retrieveConnAndLikes(String json) 
	{
		JSONObject jsonobj = null;
		JSONArray jsonarr = null;
		try {
			jsonobj = new JSONObject(json);
			jsonarr = (JSONArray) jsonobj.get("events");
			return handleJsonEvents(jsonarr);
		} catch (JSONException e) {
			try {
				JSONArray array = new JSONArray(json);
				return handleJsonEvents(array);
			}
			catch (JSONException e2) {
				return null;
			}
		}
	}
	private Object handleJsonEvents(JSONArray array) throws JSONException {
		JSONArray event;
		List<Object> events = new ArrayList<>();
		for (int i=0; i<array.length(); i++)
		{
			event = (JSONArray) array.get(i);
			events.add(event.get(0));
			if (event.get(0).equals(ServerConstants.EVENT_CONNECTED))
			{
				status = ServerConstants.STATUS_ONLINE;
				activate();
			}
			if (event.get(0).equals(ServerConstants.EVENT_COMMONLIKES))
				retrieveLikes(event);
			if (event.get(0).equals(ServerConstants.EVENT_GOTMESSAGE))
			{
				msgs.add(event.getString(1));
			}
			if (event.get(0).equals(ServerConstants.EVENT_DISCONNECT))
			{
				status = ServerConstants.STATUS_OFFLINE;
				destroy();
			}
			if (!events.isEmpty() && timeouts > 0) timeouts--;
		}
		return events;
	}
	private void retrieveLikes(JSONArray event) throws JSONException {
		likes = "";
		JSONArray array_of_likes = (JSONArray) event.get(1);
		for (int j=0; j<array_of_likes.length()-1; j++)
			likes += array_of_likes.get(j)+",";
		likes += array_of_likes.get(array_of_likes.length()-1);
	}

	/**
	 * 	Try to look for the clientID in the json and return it
	 * 
	 * 	@param json
	 * 	@return null if unsuccessful or the clientID 
	 */
	private String retrieveClientId(String json) {
		JSONObject jsonobj = null;
		Object ret;
		try {
			jsonobj = new JSONObject(json);
			ret = jsonobj.get("clientID");
		} catch (JSONException e) {
			return null;
		}
		return (String) ret;
	}
	/**
	 * 	Return the event in the json. If it's a message received add it to the messages queue
	 * 
	 * 	@param json
	 * 	@return null if unsuccessful or the event
	 
	private String retrieveEvent(String json) {
		JSONArray jsonobj = null;
		Object ret = null;
		try {
			jsonobj = new JSONArray(json);
			for (int i=0; i<jsonobj.length(); i++)
			{
				JSONArray firstObj = (JSONArray) jsonobj.get(i);
				ret = firstObj.get(0);
				if (ret.equals(ServerConstants.EVENT_GOTMESSAGE)) 
					msgs.add((String) firstObj.get(1));
			}
		} catch (JSONException e) {
			return null;
		}
		if (timeouts > 0) timeouts--;
		return (String) ret;
	}*/
	/**
	 * 	Destroying this service -
	 * 		Interrupt the polling mechanism and nullify the main and service components.
	 * 		After this the constructor needs to be called again to reactivate the service
	 */
	@SuppressFBWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	public void destroy() {
		if (main != null) 
		{
			main.interrupt();
			main = null;
		}
		timeouts = -1;
		OmegleService.service = null;
		msgs.clear();
		currEvents.clear();
		likes = "";
		status = ServerConstants.STATUS_OFFLINE;
	}
}
