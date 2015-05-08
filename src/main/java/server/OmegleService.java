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
	private String clientId = "";
	public String currEvent = "";
	private String status = ServerConstants.STATUS_OFFLINE;
	private Thread main = null;
	private MyTimer timer = new MyTimer();
	private ConcurrentLinkedQueue<String> msgs = new ConcurrentLinkedQueue<String>();
	
	public ConcurrentLinkedQueue<String> getMsgs() {
		return msgs;
	}

	public String getStatus() {
		return status;
	}

	public String getCurrEvent() {
		return currEvent;
	}

	public OmegleService()
	{
		if (sendOmegleHttpRequest(ServerConstants.URL_CONNECT, null).equals("win")/*getOmegleClient()*/) 
		{
			main = new Thread(new Runnable() {			
				@Override
				public void run() {
					while (status.equals(ServerConstants.STATUS_ONLINE))
					{
						pollEvent();
						if (currEvent != null)
						{
							if (currEvent.equals(ServerConstants.EVENT_DISCONNECT)) status = ServerConstants.STATUS_OFFLINE;
							else if (currEvent.equals(ServerConstants.EVENT_CONNECTED)) status = ServerConstants.STATUS_ONLINE;
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});	
			main.start();
		}
	}

	private void pollEvent() {	
		try {
			timer.schedule(() -> {
				currEvent = sendOmegleHttpRequest(ServerConstants.URL_EVENT, null);//getOmegleEvent();
			}, 0);
		}
		catch (IllegalStateException e){};		// in case the timer has been terminated but the main thread hasn't yet
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
	
	private String processText(String url	, String text) 
	{
		if (text.equals(ServerConstants.SUCCESS_MSG))
		{
			if (url.equals(ServerConstants.URL_DISCONNECT)) status = ServerConstants.STATUS_OFFLINE;
			else if (url.equals(ServerConstants.URL_CONNECT)) status = ServerConstants.STATUS_ONLINE;
			
			return ServerConstants.SUCCESS_MSG;
		}	
		return null;
	}

	private String processJson(String json) 
	{
		String client = "";
		if ((client = retrieveClientId(json)) != null)
		{
			clientId = client;
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
		
		if (urlSend.equals(ServerConstants.URL_EVENT) || urlSend.equals(ServerConstants.URL_CONNECT))conn.setRequestProperty("Accept","application/json");
		else if (urlSend.equals(ServerConstants.URL_SEND)) conn.setRequestProperty("Content-Length",contentLen);
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
	private class MyTimer {
		  private final Timer t = new Timer();

		  private TimerTask schedule(final Runnable r, long delay) {
		     final TimerTask task = new TimerTask() { public void run() { r.run(); }};
		     t.schedule(task, delay);
		     return task;
		  }
		  
		  private void destroy() {
			  t.cancel();
		  }
	}
	public void destroy() {
		//if (main != null) main.interrupt();
		timer.destroy();
	}
}
