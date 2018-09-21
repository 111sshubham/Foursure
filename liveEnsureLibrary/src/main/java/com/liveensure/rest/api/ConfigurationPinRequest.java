package com.liveensure.rest.api;

public class ConfigurationPinRequest {

	String pin;
	String fingerprint;
	String pinToken;
	String pinFingerprint;
	
	/**
	 * @return the pin
	 */
	public String getPin() {
		return pin;
	}
	/**
	 * @param pin the pin to set
	 */
	public void setPin(String pin) {
		this.pin = pin;
	}
	/**
	 * @return the fingerprint
	 */
	public String getFingerprint() {
		return fingerprint;
	}
	/**
	 * @param fingerprint the fingerprint to set
	 */
	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}
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
	/**
	 * @return the pinTimestamp
	 */
	public String getPinFingerprint() {
		return pinFingerprint;
	}
	/**
	 * @param pinTimestamp the pinTimestamp to set
	 */
	public void setPinFingerprint(String pinFingerprint) {
		this.pinFingerprint = pinFingerprint;
	}
	
}
