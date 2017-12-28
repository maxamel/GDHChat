package com.desktopomegle.client;

public class ClientConstants {

	public final static String STRANGER_STATUS_OFFLINE = "Disconnected";
	public final static String STRANGER_STATUS_IDLE = "Idle";
	public final static String STRANGER_STATUS_TYPING = "Typing...";
	
	public final static String BASE_URL_PRE = "https://front";
	public final static String BASE_URL_SUF = ".omegle.com/";
	public final static String BASE_URL_SUFFIX = "&lang=en";
	public final static String BASE_URL_BODY = "start?rcs=1&firstevents=1&spid=&randid=6F7B3TZ7";
	public final static String URL_CONNECT = BASE_URL_BODY+BASE_URL_SUFFIX;
	public final static String URL_CONNECT_INTERESTS = BASE_URL_BODY+"&%s"+BASE_URL_SUFFIX;
	public final static String URL_EVENT = "events";
	public final static String URL_TYPING = "typing";
	public final static String URL_SEND = "send";
	public final static String URL_DISCONNECT = "disconnect";
	public final static String URL_STOPSEARCH = "stoplookingforcommonlikes";
	
	public final static String EVENT_CONNECTED = "connected";
	public final static String EVENT_TYPING = "typing";
	public final static String EVENT_GOTMESSAGE = "gotMessage";
	public final static String EVENT_DISCONNECT = "strangerDisconnected";
	
	public final static String STATUS_ONLINE = "online";
	public final static String STATUS_OFFLINE = "offline";
	
	public final static String RESOURCES = "images/";
}
