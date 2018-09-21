package com.liveensure.rest.api;


public class ConfigurationPinResponse extends LiveResponse {

	private String pinToken;

	/**
	 * @return the pinToken
	 */
	public String getPinToken() {
		return pinToken;
	}

	/**
	 * @param pinToken the pinToken to set
	 */
	public void setPinToken(String pinToken) {
		this.pinToken = pinToken;
	}
}
