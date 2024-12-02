package com.gdhchat.client;

public class ClientConstants {

	public final static String STRANGER_STATUS_OFFLINE = "Disconnected";
	public final static String STRANGER_STATUS_IDLE = "Idle";
	public final static String STRANGER_STATUS_TYPING = "Typing...";
	
	public final static String STATUS_ONLINE = "online";
	public final static String STATUS_OFFLINE = "offline";
	
	public final static String RESOURCES = "images/";

	public final static String OMEGLE_START = "OmegleStart";
	public final static String OMEGLE_STOP = "OmegleStop";

	protected final static String CONNECT_PROMPT = "Pretend you are a stranger and we are chatting online. " +
			"Choose randomly your name, sex and an age between 18-65. Randomly pick your nationality, residence and political preference." +
			"Your interests include these: %s. " +
			"Other areas are of little to no interest for you. Your character traits are: %s. " +
			"Keep answers casual and maximum 1-2 sentences each, unless requested otherwise. " +
			"80%% of your answers need to end with a statement or observation, and 20%% with a question. " +
			"Don't introduce yourself, I will speak first. When ready say " + OMEGLE_START;

	protected final static String DISCONNECT_PROMPT = "Can you stop pretending and be chatgpt again? When you are chatgpt again, say " + OMEGLE_STOP;

	protected static final String ROLE_SYSTEM = "system";
	protected static final String ROLE_USER = "user";

	final static String[] moodStates = {"excited", "anxious", "irritated", "melancholic", "philosophical", "laid-back", "ecstatic", "playful", "pessimistic"};
	final static String[] intellectStates = {"empirical", "intuitive", "spiritual", "simple-minded", "dumb", "primitive", "intellectual", "slow"};
	final static String[] styleStates = {"cynical", "impulsive", "self-centered", "rude", "scattered", "polite", "spicy", "feisty", "apologetic"};

}
