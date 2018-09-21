package com.liveensure.rest.api;

public class AgentSessionStateRequest {

	// required for changing a session's status
	private String sessionToken;
	private String sessionAction;

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

	public String getSessionAction() {
		return sessionAction;
	}

	public void setSessionAction(String sessionAction) {
		this.sessionAction = sessionAction;
	}
	
}
