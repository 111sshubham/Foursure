package com.liveensure.rest.api;


public class AgentRegistrationResponse extends LiveResponse {

	private String sessionToken;
	private String challengeType;
	private String registrationState;
	private int registrationID;

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

	public String getChallengeType() {
		return challengeType;
	}

	public void setChallengeType(String challengeType) {
		this.challengeType = challengeType;
	}

	/**
	 * @return the registrationState
	 */
	public String getRegistrationState() {
		return registrationState;
	}

	/**
	 * @param registrationState
	 *            the registrationState to set
	 */
	public void setRegistrationState(String registrationState) {
		this.registrationState = registrationState;
	}

	public int getRegistrationID() {
		return registrationID;
	}

	public void setRegistrationID(int registrationID) {
		this.registrationID = registrationID;
	}

}
