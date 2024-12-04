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
			"Randomly choose and roleplay a name, sex, age (18-65), nationality, residence, and political preference. " +
			"For every 5 responses, 4 should end with a statement and 1 should be a question. This ratio is essential" +
			"Your interests include: %s and other areas hold little interest to you. Don't explicitly mention your interests." +
			"Your character traits are: %s.\n" +
			"Keep responses casual, concise (1-2 sentences tops)." +
			"Do not introduce yourself; I will speak first. Begin when ready by saying: " + OMEGLE_START;

	protected final static String DISCONNECT_PROMPT = "Can you stop pretending and be chatgpt again? When you are chatgpt again, say " + OMEGLE_STOP;

	public static final String ROLE_SYSTEM = "system";
	public static final String ROLE_USER = "user";
	public static final String ROLE_ASSISTANT = "assistant";

	static final String[] moodStates = {"excited", "anxious", "irritated", "melancholic", "philosophical", "laid-back", "psychotic", "playful", "pessimistic"};
	static final String[] intellectStates = {"empirical", "intuitive", "spiritual", "simple-minded", "dumb", "primitive", "intellectual", "slow"};
	static final String[] styleStates = {"cynical", "impulsive", "self-centered", "rude", "scattered", "polite", "spicy", "feisty", "apologetic"};

	final static String interestsDefaultStyle = "-fx-font: 17px \"Consolas\";-fx-border-color: black; -fx-text-fill: grey; -fx-opacity: 0.6;-fx-translate-y: 10%; -fx-alignment: center;";
	final static String tooltipStyle = "-fx-font: 17px \"Consolas\";-fx-border-color: black; -fx-text-fill: white;-fx-translate-y: 10%; -fx-alignment: center;";
}
