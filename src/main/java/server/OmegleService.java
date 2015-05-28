package main.java.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OmegleService {
	private static OmegleService service = null;
	private String clientId = "";
	private ConcurrentLinkedQueue<String> currEvents = new ConcurrentLinkedQueue<String>();;
	private String status = ServerConstants.STATUS_OFFLINE;
	private Thread main = null;
	private ConcurrentLinkedQueue<String> msgs = new ConcurrentLinkedQueue<String>();
	private String likes = "";
	
	public static OmegleService getInstance()
	{
		if (service == null) service = new OmegleService();
		return service;
	}
	
	private OmegleService()
	{
	
	}
	private void activate()
	{
		if (main == null)
		{
			main = new Thread(new Runnable() {			
				@Override
				public void run() {
					while (status.equals(ServerConstants.STATUS_ONLINE))
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

	private void pollEvent() {	
		String event = "";
		if (currEvents.isEmpty() || !currEvents.peek().equals(ServerConstants.EVENT_DISCONNECT)) 
			if ( (event = sendOmegleHttpRequest(ServerConstants.URL_EVENT, null)) != null) 
				currEvents.add(event);
	}
	
	public String sendOmegleHttpRequest(String endPoint, String msg)
	{
			URL url = null;
			HttpURLConnection conn = null;
			OutputStream os = null;
			InputStream is = null;
			try {
				url = new URL(endPoint);
				conn = (HttpURLConnection) url.openConnection();
				setHeaders(conn,endPoint,"42");
				conn.setDoOutput(true);
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
				String res = new Scanner(is).useDelimiter("\\A").next();
				is.close();	
				if (res != null && conn.getRequestProperty("Accept").equals("application/json")) return processJson(res);
				else return processText(endPoint,res);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return null;
	}
	
	private String processText(String url, String text) 
	{
		if (text.equals(ServerConstants.SUCCESS_MSG))
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
			
			return ServerConstants.SUCCESS_MSG;
		}	
		return null;
	}

	private String processJson(String json) 
	{
		String client = "";
		if ((client = retrieveClientId(json)) != null)
		{
			status = ServerConstants.STATUS_ONLINE;
			clientId = client;
			activate();
		}
		if (retrieveConnAndLikes(json) != null)
		{
			status = ServerConstants.STATUS_ONLINE;
			return ServerConstants.SUCCESS_MSG;
		}
		else return (retrieveEvent(json));
	}

	private void setHeaders(HttpURLConnection conn, String urlSend, String contentLen) throws ProtocolException 
	{
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); 
		conn.setRequestProperty("Content-Length","42");
		conn.setRequestProperty("Accept","text/plain");   
		conn.setRequestProperty("Connection","keep-alive");
		conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36");
		conn.setRequestProperty("Origin", "http://www.omegle.com");
		
		if (urlSend.equals(ServerConstants.URL_EVENT) || urlSend.contains(ServerConstants.BASE_URL_BODY))conn.setRequestProperty("Accept","application/json");
		else if (urlSend.equals(ServerConstants.URL_SEND)) conn.setRequestProperty("Content-Length",contentLen);
	}

	private Object retrieveConnAndLikes(String json) 
	{
		JSONObject jsonobj = null;
		JSONArray event = null;
		JSONArray jsonarr = null;
		Object ret = null;
		try {
			jsonobj = new JSONObject(json);
			jsonarr = (JSONArray) jsonobj.get("events");
			for (int i=0; i<jsonarr.length(); i++)
			{
				event = (JSONArray) jsonarr.get(i);	
				if (event.get(0).equals(ServerConstants.EVENT_CONNECTED))
					ret = event.get(0);
				if (event.get(0).equals(ServerConstants.EVENT_COMMONLIKES))
				{
					JSONArray array_of_likes = (JSONArray) event.get(1);
					for (int j=0; j<array_of_likes.length()-1; j++)
						likes += array_of_likes.get(j)+",";
					likes += array_of_likes.get(array_of_likes.length()-1);
				}
			}
			return ret;
		} catch (JSONException e) {
			try {
				JSONArray array = new JSONArray(json);
				for (int i=0; i<array.length(); i++)
				{
					event = (JSONArray) array.get(i);
					if (event.get(0).equals(ServerConstants.EVENT_CONNECTED))
						ret = event.get(0);
					if (event.get(0).equals(ServerConstants.EVENT_COMMONLIKES))
					{
						likes = "";
						JSONArray array_of_likes = (JSONArray) event.get(1);
						for (int j=0; j<array_of_likes.length()-1; j++)
							likes += array_of_likes.get(j)+",";
						likes += array_of_likes.get(array_of_likes.length()-1);
					}
				}
			}
			catch (JSONException e2) {
				return null;
			}
		}
		return null;
	}

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
	
	private String retrieveEvent(String json) {
		JSONArray jsonobj = null;
		Object ret;
		try {
			jsonobj = new JSONArray(json);
			JSONArray firstObj = (JSONArray) jsonobj.get(0);
			ret = firstObj.get(0);
			if (ret.equals(ServerConstants.EVENT_GOTMESSAGE)) 
				msgs.add((String) firstObj.get(1));
		} catch (JSONException e) {
			return null;
		}
		return (String) ret;
	}
	public void destroy() {
		main.interrupt();
		service = null;
	}
}
