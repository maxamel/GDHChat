package com.gdhchat.client;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Error: Exactly one argument (ChatGPT API key) is required.");
            System.exit(1);
        }
        OmegleGPTClient.main(args);
    }
}