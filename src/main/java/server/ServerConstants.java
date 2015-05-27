package main.java.server;

public class ServerConstants {

	public final static String BASE_URL = "http://front1.omegle.com/";
	public final static String BASE_URL_SUFFIX = "&lang=en";
	public final static String BASE_URL_BODY = "start?rcs=1&firstevents=1&spid=&randid=3EV6ABEE";
	public final static String URL_CONNECT = BASE_URL+BASE_URL_BODY+BASE_URL_SUFFIX;
	public final static String URL_CONNECT_INTERESTS = BASE_URL+BASE_URL_BODY+"&%s"+BASE_URL_SUFFIX;
	public final static String URL_EVENT = BASE_URL+"events";
	public final static String URL_TYPING = BASE_URL+"typing";
	public final static String URL_SEND = BASE_URL+"send";
	public final static String URL_DISCONNECT = BASE_URL+"disconnect";
	public final static String URL_STOPSEARCH = BASE_URL+"stoplookingforcommonlikes";
	
	public final static String EVENT_CONNECTED = "connected";
	public final static String EVENT_TYPING = "typing";
	public final static String EVENT_GOTMESSAGE = "gotMessage";
	public final static String EVENT_DISCONNECT = "strangerDisconnected";
	public final static String EVENT_COMMONLIKES = "commonLikes";
	
	public final static String STATUS_ONLINE = "online";
	public final static String STATUS_OFFLINE = "offline";
	
	public final static String SUCCESS_MSG = "win";
}
