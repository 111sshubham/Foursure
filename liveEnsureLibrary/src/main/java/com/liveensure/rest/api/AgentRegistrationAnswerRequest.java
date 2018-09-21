package com.liveensure.rest.api;

import java.util.HashMap;

import com.liveensure.core.DynamicChallengeType;

public class AgentRegistrationAnswerRequest {

	// required for changing a session's status
	private String sessionToken;
	private int registrationID;
	private HashMap<String, String> registrationDetails;
	private DynamicChallengeType challengeType;

	/**
	 * @return the sessionToken
	 */
	public String getSessionToken() {
		return sessionToken;
	}

	/**
	 * @param sessionToken
	 *            the sessionToken to set
	 */
	public void setSessionToken(String sessionToken) {
		this.sessionToken = sessionToken;
	}

	public int getRegistrationID() {
		return registrationID;
	}

	public void setRegistrationID(int registrationID) {
		this.registrationID = registrationID;
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
