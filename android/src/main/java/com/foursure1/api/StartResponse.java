package com.foursure1.api;

import com.foursure1.util.Status;

public class StartResponse extends LiveResponse {
	private String fsSessionToken;
	private String leSessionToken;
	private String identityServer;
	private Status startStatus;
	private long fsSessionTTL;

	public String getFsSessionToken() {
		return fsSessionToken;
	}

	public void setFsSessionToken(String sessionToken) {
		this.fsSessionToken = sessionToken;
	}

	public String getLeSessionToken() {
		return leSessionToken;
	}

	public void setLeSessionToken(String sessionToken) {
		this.leSessionToken = sessionToken;
	}

	public String getIdentityServer() {
		return identityServer;
	}

	public void setIdentityServer(String identityServer) {
		this.identityServer = identityServer;
	}

	public Status getStartStatus() {
		return startStatus;
	}

	public void setStartStatus(Status startStatus) {
		this.startStatus = startStatus;
	}

	public long getFsSessionTTL() {
		return fsSessionTTL;
	}

	public void setFsSessionTTL(long fsSessionTTL) {
		this.fsSessionTTL = fsSessionTTL;
	}

}