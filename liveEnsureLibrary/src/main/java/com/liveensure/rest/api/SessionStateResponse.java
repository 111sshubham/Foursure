package com.liveensure.rest.api;

public class SessionStateResponse extends LiveResponse {

	// required for changing a session's status
	private String sessionToken;

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
