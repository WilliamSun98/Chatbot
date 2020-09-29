package com.c01.connector;

import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.DeleteSessionOptions;
import com.ibm.watson.assistant.v2.model.MessageInput;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.SessionResponse;
import java.io.*;

public class Communicator {

	private Connector connector;
	private CreateSessionOptions cso;
	private SessionResponse session;
	private String sessionID;
	private boolean isActive;
	
	public Communicator(Connector connector) {
		this.connector = connector;
		this.isActive = false;
	}
	
	public int startChat() {
		startSession();
		// Read user input (possibly add time out restriction later)
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String userIn;
		while (true) {
			System.out.print(">> ");
			try {
				userIn = br.readLine();
				if (userIn == null) {
					break;
				};
				System.out.println(unwrapResponse(send_message(userIn)));;
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		endSession();
		return 0;
	}
	
	/*
	 * Start the conversation with Watson chatbot for user
	 */
	protected void startSession() {
		// Create a new session for current request of communication
		this.isActive = true;
		this.cso = new CreateSessionOptions.Builder(this.connector.getID()).build();
		this.session = this.connector.getAssistant().createSession(cso).execute().getResult();
		this.sessionID = session.getSessionId();
		System.out.println("Session has started");
	}
	
	/*
	 * End the conversation by terminating the session
	 */
	protected void endSession() {
		if (this.isActive) {
			DeleteSessionOptions options = new DeleteSessionOptions.Builder(this.connector.getID(), this.sessionID)
												.build();
			this.connector.getAssistant().deleteSession(options).execute();
		}
		System.out.println("Session has ended");
	}
	
	/*
	 * Wrap the string in Watson message object
	 */
	protected MessageResponse send_message(String s) {
		MessageInput inp = new MessageInput.Builder()
								.messageType("text")
								.text(s)
								.build();
		MessageOptions options = new MessageOptions.Builder(this.connector.getID(), this.sessionID)
								.input(inp)
								.build();
		MessageResponse response = this.connector.getAssistant().message(options).execute().getResult();
		return response;
	}
	
	/*
	 * Format the returned response information and print it out. (Currently not performing any formatting)
	 */
	protected String unwrapResponse(MessageResponse response) {
		return response.toString();
	}
	
}
