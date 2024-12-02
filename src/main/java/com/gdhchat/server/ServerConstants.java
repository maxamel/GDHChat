package com.gdhchat.server;

public class ServerConstants {


	public final static String URL_EVENT = "events";

	public final static String EVENT_CONNECTED = "connected";
	public final static String EVENT_GOTMESSAGE = "gotMessage";
	public final static String EVENT_DISCONNECT = "strangerDisconnected";
	public final static String STATUS_OFFLINE = "offline";

	protected final static String SUMMARY_PROMPT = "Summarize the conversation so far in maximum 1-2 sentences.";


	public enum ResponseStatus {
		SUCCESS,
		FAIL;
	}

}
