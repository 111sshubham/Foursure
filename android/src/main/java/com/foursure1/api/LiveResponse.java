package com.foursure1.api;

public class LiveResponse {
	private String statusMessage;
	
	public LiveResponse() {}
	
	public void setStatusMessage(String message) {
		this.statusMessage = message;
	}
	
	public String getStatusMessage() {
		return statusMessage;
	}
	
}