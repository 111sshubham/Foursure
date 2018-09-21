package com.foursure1.api;

public class SignupResponse extends LiveResponse {
	private String fsSessionToken;
	private String leSessionToken;
	private String identityServer;
	private String deviceUUID;
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

	public String getDeviceUUID() {
		return deviceUUID;
	}

	public void setDeviceUUID(String deviceUUID) {
		this.deviceUUID = deviceUUID;
	}

	public long getFsSessionTTL() {
		return fsSessionTTL;
	}

	public void setFsSessionTTL(long fsSessionTTL) {
		this.fsSessionTTL = fsSessionTTL;
	}

}
