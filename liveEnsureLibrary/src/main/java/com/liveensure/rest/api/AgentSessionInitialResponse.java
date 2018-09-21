package com.liveensure.rest.api;

public class AgentSessionInitialResponse extends LiveResponse {

	private String sessionToken;
	private int sessionTimeLeftToLive;
	private String locale;
	private String initialStatus;
	private String initialMessage;
	private String message;

	public int getSessionTimeLeftToLive() {
		return sessionTimeLeftToLive;
	}

	public void setSessionTimeLeftToLive(int sessionTimeLeftToLive) {
		this.sessionTimeLeftToLive = sessionTimeLeftToLive;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getInitialStatus() {
		return initialStatus;
	}

	public void setInitialStatus(String initialStatus) {
		this.initialStatus = initialStatus;
	}

	public String getInitialMessage() {
		return initialMessage;
	}

	public void setInitialMessage(String initialMessage) {
		this.initialMessage = initialMessage;
	}

	/**
	 * @return the sessionToken
	 */
	public String getSessionToken() {
		return sessionToken;
	}

	/**
	 * @param sessionToken the sessionToken to set
	 */
	public void setSessionToken(String sessionToken) {
		this.sessionToken = sessionToken;
	}

}
