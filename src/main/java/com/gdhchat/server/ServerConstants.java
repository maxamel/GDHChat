package com.gdhchat.server;

public class ServerConstants {

	public final static String STATUS_OFFLINE = "offline";
	public final static String SUMMARY_PROMPT = "Summarize the conversation so far in maximum 1-2 sentences.";
	public static final String SUMMARIZED_CONTEXT = "Summarized context:";

	public enum ResponseStatus {
		SUCCESS,
		FAIL;
	}

}
