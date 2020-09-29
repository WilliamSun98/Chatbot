package com.c01.connector;

import java.io.IOException;

public class TempBot {
	public static void main(String[] args) throws IOException {
		Connector connection = Connector.connect();
		Communicator user1 = new Communicator(connection);
		user1.startChat(); // (Enter EOF to stop the chat in console)
	}

	public static String getOneResponse(String input) throws IOException {
		Connector connection = Connector.connect();
		Communicator user1 = new Communicator(connection);
		user1.startSession();
		String answer = user1.send_message(input).getOutput().getGeneric().get(0).getText();
		System.out.println(answer);
		user1.endSession();
		return answer;
	}
}
