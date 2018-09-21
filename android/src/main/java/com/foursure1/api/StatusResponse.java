package com.foursure1.api;

public class StatusResponse extends LiveResponse {
	private String fsSessionToken;
	private String sessionStatus;

	public StatusResponse() {
	}

	public String getFsSessionToken() {
		return fsSessionToken;
	}

	public void setFsSessionToken(String fsSessionToken) {
		this.fsSessionToken = fsSessionToken;
	}

	public String getSessionStatus() {
		return sessionStatus;
	}

	public void setSessionStatus(String sessionStatus) {
		this.sessionStatus = sessionStatus;
	}

}
