package com.c01.connector;

import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.cloud.sdk.core.service.security.IamOptions;

class Connector {
	private static final String BOT_VERSION = "2019-02-28"; // Current Watson assistant version
	private static final String BOT_URL = "https://gateway.watsonplatform.net/assistant/api";
	private static final String BOT_ID = "5bc2c6ab-b4e4-4daa-8464-0ee9b74566ce";
	private static final String API_KEY = "gBdrH54w_o8sy69MhKknNP3WmMWQtRvESeVx_ma2KKJZ";

	private static Connector instance = null;
	private IamOptions options;
	private Assistant assistant;
	
	private Connector() {
		options = new IamOptions.Builder()
					.apiKey(API_KEY)
					.build();
		assistant = new Assistant(BOT_VERSION, options);
		assistant.setEndPoint(BOT_URL);
	}
	
	public static Connector connect() {
		if (instance == null) {
			instance = new Connector();
		}
		return instance;
	}
	
	public String getID() {
		return BOT_ID;
	}
	
	public Assistant getAssistant() {
		return this.assistant;
	}
	
}


