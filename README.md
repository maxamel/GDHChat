![Alt text](src/main/resources/images/omegle.png)<br/>
[![CircleCI](https://dl.circleci.com/status-badge/img/circleci/VrxAa5tSns3x9pseRDihbe/FqbAYJEgkBbN83wuFnkS97/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/circleci/VrxAa5tSns3x9pseRDihbe/FqbAYJEgkBbN83wuFnkS97/tree/master)<br/>
# OmegleGPT

A JavaFX application recreating the Omegle chat service experience with ChatGPT, letting you chat with AI 'strangers' based on mutual interests. 
Requires an API key from [OpenAI](https://platform.openai.com/), which you can get for free.

<img src="https://github.com/maxamel/DesktopOmegle/blob/master/src/main/resources/images/screen.png" align="center" />

# Introduction

This repository first started as a Desktop client for the popular online chat service [Omegle](https://en.wikipedia.org/wiki/Omegle). The service allowed connecting with random strangers based on similar interests and anonymously chatting with them one on one. No rooms, no photos, no registrations, etc. As simple as possible.
No official documentation existed for the API of Omegle, and it was mostly discovered using open source network analyzers like WireShark and Fiddler.

Original Omegle layout:

![image](https://github.com/user-attachments/assets/6dd45af3-8d2b-46cd-ae83-75070f5222a7)

After some time, the service started experiencing difficulties with allegations of sexual harassment by users, internet misuse and lawsuits, which eventually led to its shutdown in 2023. This led to the reincarnation of this project as a simulation of the Omegle experience using ChatGPT. Like in Omegle, a user connects to someone with similar interests on the other end, not knowing who he/she is, where they're from, etc. 
Only this time the random stranger is actually ChatGPT, with different personality traits each time. Like in real life, not every converation partner will be nice, and not every conversation will feel interesting and engaging to you. If it's not - hit the disconnect button and reconnect to talk to someone new 😄

# Overview

The project's goal is to explore how a ChatGPT-based application can replace existing chat services. Similar to Omegle, many of today’s online chat platforms face significant challenges, primarily stemming from user misconduct. As AI models grow more advanced and their outputs increasingly resemble human communication, it's only a matter of time before AI bots in chat rooms become as capable of engaging in conversation as any human.

Among future works that can be done in this area, is the hybrid approach where users can get paired with either real participants or ChatGPT-based applications.
At the end of the conversation, participants must vote on whether they believe they were interacting with a real person or an AI. This serves as a form of [Turing test]((https://en.wikipedia.org/wiki/Turing_test)), providing a genuine evaluation of the current capabilities of AI.

# Testing and Code Quality

The application was mostly tested manually, there are a few unit tests for the Service module. 
Currently writing further tests is the top priority. 
Code quality is maintained by using static analysis tools: SpotBugs and Coverity.

# Prerequisites

Written in Java 21, UI is implemented in JavaFX 21.
Uses the *gpt-4o-mini* model of ChatGPT for efficiency and cost reasons.

Built with Gradle.

# Installation

Get the code and build it:
```
git clone https://github.com/maxamel/OmegleGPT
cd OmegleGPT
./gradlew clean build
```
After a successful build, run the Jar in the build directory:
```
java -jar build/libs/OmegleGPT-all.jar <YOUR_API_KEY>
```

# Contributing

Any contributions are welcome. 

Possibilities for enhancement:

1) Add tests.

2) Add a tab with a mini dashboard to track token usage and configure token based limits. Roughly speaking - give users ability to choose their own limits, balancing between more contextful conversations and token consumption.

3) Output metrics from the application to understand how the model is behaving in different applications.

4) Provide configuration capabilities to give users the ability to fine-tune the personality traits their chat counterpart can exhibit.

5) Handle responses blocked by OpenAI moderation.
   

# Limitations

One of the difficulties faced when developing ChatGPT-based applications is token consumption and limits. If you want to build a chat oriented app that needs to retain context throughout a conversation, you need to send this context with every API request. As the conversation gets longer, every call requires more tokens than before. In other words, the token cunsumption **per-call** grows linearly. There is also a technical hard limit for the amount of tokens that can be sent in a request, and that depends on the specific model used. In light of these reasons, there needs to be a mechanism for cutting down the token consumption, at the expense of reducing the context available for the model. 

OmegleGPT uses conversation summarization as a first line of mitigation for these issues. That means chunks of the conversation are summarized into short 1-2 sentence bits. That's usually enough to keep the token usage stable over time.
The second line of mitigation, for really long conversations, is throwing away old summarizations once enough time passes. That means the model will lose the information in those summarizations, but typically that's OK in very long conversations.

 
# Fun Facts

- The first commit to this repository was made on April 2015. That is over six months before the foundation of OpenAI, the developer of ChatGPT (upon which this application is based).
  
- Over 25% of the code for this ChatGPT-based application was written by... ChatGPT. So thank you ChatGPT, for contributing to the integration with you.


