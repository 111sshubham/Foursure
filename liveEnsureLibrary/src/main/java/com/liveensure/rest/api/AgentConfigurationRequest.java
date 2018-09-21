package com.liveensure.rest.api;

import java.util.HashMap;

import com.liveensure.core.DynamicChallengeType;

public class AgentConfigurationRequest {

	// required for changing a session's status
	private String fingerprint;
	private String pinToken;
	private HashMap<String, String> registrationDetails;
	private DynamicChallengeType challengeType;

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

	public HashMap<String, String> getRegistrationDetails() {
		return registrationDetails;
	}

	public void setRegistrationDetails(HashMap<String, String> registrationDetails) {
		this.registrationDetails = registrationDetails;
	}

	public DynamicChallengeType getChallengeType() {
		return challengeType;
	}

	public void setChallengeType(DynamicChallengeType challengeType) {
		this.challengeType = challengeType;
	}

}
