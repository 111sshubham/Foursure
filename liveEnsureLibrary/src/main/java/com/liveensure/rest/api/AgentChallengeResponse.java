package com.liveensure.rest.api;

import java.io.Serializable;
import java.util.HashMap;

public class AgentChallengeResponse extends LiveResponse implements Serializable {

  private static final long serialVersionUID = -9194180609139707999L;
  private String sessionToken;
	private String challengeType;
	private HashMap<String, Object> challengeDetails;
	private String answerState;
	private String registrationState;
	private int challengeID;

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

	public HashMap<String, Object> getChallengeDetails() {
		return challengeDetails;
	}

	public void setChallengeDetails(HashMap<String, Object> challengeDetails) {
		this.challengeDetails = challengeDetails;
	}

	public String getAnswerState() {
		return answerState;
	}

	public void setAnswerState(String answerOK) {
		this.answerState = answerOK;
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

	public int getChallengeID() {
		return challengeID;
	}

	public void setChallengeID(int challengeID) {
		this.challengeID = challengeID;
	}

}
