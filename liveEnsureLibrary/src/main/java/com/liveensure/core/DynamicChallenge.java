package com.liveensure.core;

import java.io.Serializable;
import java.util.HashMap;

public abstract class DynamicChallenge implements Serializable {
	private static final long serialVersionUID = 2713895609032949070L;

	DynamicChallengeType challengeType;
	int challengeID;
	int attempts;
	String answerState;
	
	String successMessage;
	String failureMessage;
	
	int maximumAttempts;
	boolean required;
	DynamicChallenge fallbackChallenge;

	public DynamicChallengeType getChallengeType() {
		return challengeType;
	}

	public void setChallengeID(int i) {
		challengeID = i;
	}

	public int getChallengeID() {
		return challengeID;
	}
	
	public abstract boolean isAnswerOK(Object answer);

	public int getAttempts() {
		return attempts;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

	public int getMaximumAttempts() {
		return maximumAttempts;
	}

	public void setMaximumAttempts(int maximumAttempts) {
		if (maximumAttempts < 1) maximumAttempts = 1;  // every challenge should have at least 1 attempt
		if (maximumAttempts > 9) maximumAttempts = 9;  // every challenge should have at most 9 attempts
		this.maximumAttempts = maximumAttempts;
	}

	public abstract HashMap<String, Object> toHashMap();

	public String getAnswerState() {
		return answerState;
	}

	public void setAnswerState(String answerState) {
		this.answerState = answerState;
	}
	
	/**
	 * @return the successMessage
	 */
	public String getSuccessMessage() {
		return successMessage;
	}

	/**
	 * @param successMessage the successMessage to set
	 */
	public void setSuccessMessage(String successMessage) {
		this.successMessage = successMessage;
	}

	/**
	 * @return the failureMessage
	 */
	public String getFailureMessage() {
		return failureMessage;
	}

	/**
	 * @param failureMessage the failureMessage to set
	 */
	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}

	/**
	 * @return the required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param required the required to set
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * @return the fallbackChallenge
	 */
	public DynamicChallenge getFallbackChallenge() {
		return fallbackChallenge;
	}

	/**
	 * @param fallbackChallenge the fallbackChallenge to set
	 */
	public void setFallbackChallenge(DynamicChallenge fallbackChallenge) {
		this.fallbackChallenge = fallbackChallenge;
	}

	public static DynamicChallenge createChallengeFromDetails(DynamicChallengeType type, HashMap<String, String> details, DynamicChallenge fallback) {
		boolean validType = (type == DynamicChallengeType.PROMPT || 
				type == DynamicChallengeType.PIN || 
				type == DynamicChallengeType.HOME || 
				type == DynamicChallengeType.LAT_LONG);
		if (!validType) return null;
		if (details == null || details.isEmpty()) return null;
		if (!details.containsKey("required")) return null;
		if (type == DynamicChallengeType.PROMPT) {
			// validate details for this type of challenge
			if (!details.containsKey("question") || !details.containsKey("answer")) return null;
			PromptChallenge pc = new PromptChallenge();
			pc.setPrompt(details.get("question"));
			pc.setCorrectAnswer(details.get("answer"));
			pc.setRequired(Boolean.valueOf(details.get("required")));
			
			// Optional success and failure messages to be given to the user
			if (details.containsKey("successMessage")) pc.setSuccessMessage(details.get("successMessage"));
			if (details.containsKey("failureMessage")) pc.setFailureMessage(details.get("failureMessage"));

			// Check for maximum attempts override, default is "3"
			int maxAttempts = 3;
			if (details.containsKey("maximumAttempts")) maxAttempts=Integer.parseInt(details.get("maximumAttempts"));
			pc.setMaximumAttempts(maxAttempts);
			// Check for existence of fallback challenge, default is "0" (e.g. one does not exist)
			pc.setFallbackChallenge(fallback);
			if (fallback != null) fallback.setRequired(false); // even if the consumer asked for that challenge to be required, if it's a fallback
			return pc;
		} else if (type == DynamicChallengeType.PIN) {
			// validate details for this type of challenge
			if (!details.containsKey("question") || !details.containsKey("answer")) return null;
			PinChallenge pc = new PinChallenge();
			pc.setPrompt(details.get("question"));
			pc.setCorrectAnswer(details.get("answer"));
			pc.setRequired(Boolean.valueOf(details.get("required")));

			// Optional success and failure messages to be given to the user
			if (details.containsKey("successMessage")) pc.setSuccessMessage(details.get("successMessage"));
			if (details.containsKey("failureMessage")) pc.setFailureMessage(details.get("failureMessage"));

			// Check for maximum attempts override, default is "3"
			int maxAttempts = 3;
			if (details.containsKey("maximumAttempts")) maxAttempts=Integer.parseInt(details.get("maximumAttempts"));
			pc.setMaximumAttempts(maxAttempts);
			// Check for existence of fallback challenge, default is "0" (e.g. one does not exist)
			pc.setFallbackChallenge(fallback);
			if (fallback != null) fallback.setRequired(false); // even if the consumer asked for that challenge to be required, if it's a fallback
			return pc;
		} else if (type == DynamicChallengeType.LAT_LONG) {
			// validate details for this type of challenge
			if (!details.containsKey("latitude") || !details.containsKey("longitude") || !details.containsKey("radius")) return null;
			LocationChallenge pc = new LocationChallenge();
			pc.setLatitude(Double.parseDouble(details.get("latitude")));
			pc.setLongitude(Double.parseDouble(details.get("longitude")));
			pc.setRadius(Double.parseDouble(details.get("radius")));
			pc.setRequired(Boolean.valueOf(details.get("required")));

			// Optional success and failure messages to be given to the user
			if (details.containsKey("successMessage")) pc.setSuccessMessage(details.get("successMessage"));
			if (details.containsKey("failureMessage")) pc.setFailureMessage(details.get("failureMessage"));

			// Check for maximum attempts override, default is "3"
			int maxAttempts = 1;
			if (details.containsKey("maximumAttempts")) maxAttempts=Integer.parseInt(details.get("maximumAttempts"));
			pc.setMaximumAttempts(maxAttempts);
			// Check for existence of fallback challenge, default is "0" (e.g. one does not exist)
			pc.setFallbackChallenge(fallback);
			if (fallback != null) fallback.setRequired(false); // even if the consumer asked for that challenge to be required, if it's a fallback
			return pc;
		} else if (type == DynamicChallengeType.HOME) {
			// validate details for this type of challenge
			if (!details.containsKey("latitude") || !details.containsKey("longitude") || !details.containsKey("radius")) return null;
			HomeChallenge pc = new HomeChallenge();
			pc.setLatitude(Double.parseDouble(details.get("latitude")));
			pc.setLongitude(Double.parseDouble(details.get("longitude")));
			pc.setRadius(Double.parseDouble(details.get("radius")));
			pc.setRequired(Boolean.valueOf(details.get("required")));

			// Optional success and failure messages to be given to the user
			if (details.containsKey("successMessage")) pc.setSuccessMessage(details.get("successMessage"));
			if (details.containsKey("failureMessage")) pc.setFailureMessage(details.get("failureMessage"));

			// Check for maximum attempts override, default is "3"
			int maxAttempts = 1;
			if (details.containsKey("maximumAttempts")) maxAttempts=Integer.parseInt(details.get("maximumAttempts"));
			pc.setMaximumAttempts(maxAttempts);
			// Check for existence of fallback challenge, default is "0" (e.g. one does not exist)
			pc.setFallbackChallenge(fallback);
			if (fallback != null) fallback.setRequired(false); // even if the consumer asked for that challenge to be required, if it's a fallback
			return pc;
		}
		return null;
	}
}
