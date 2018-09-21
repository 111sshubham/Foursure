package com.liveensure.rest.api;

public class AgentChallengeAnswerRequest {

	// required for changing a session's status
	private String sessionToken;
	private String agentAnswer;
	private int challengeID;
	private int managementDelay;

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

	public String getAgentAnswer() {
		return agentAnswer;
	}

	public void setAgentAnswer(String answer) {
		this.agentAnswer = answer;
	}

	public int getChallengeID() {
		return challengeID;
	}

	public void setChallengeID(int challengeID) {
		this.challengeID = challengeID;
	}

	public int getManagementDelay() {
		return managementDelay;
	}

	public void setManagementDelay(int managementDelay) {
		this.managementDelay = managementDelay;
	}

}
