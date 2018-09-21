package com.foursure1.api;

import com.foursure1.util.Status;

public class CreateShareResponse extends LiveResponse {

	String shortCode;
	Status status;

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

}
