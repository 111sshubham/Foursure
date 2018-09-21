package com.liveensure.core;

import java.util.HashMap;

import com.liveensure.util.StringHelper;

public class PinChallenge extends DynamicChallenge {

	private static final long serialVersionUID = -8011015519300704996L;
	private String prompt;
	private String correctAnswer;
	
	public PinChallenge() {
		challengeType = DynamicChallengeType.PIN;
	}
	
	public String getPrompt() {
		return prompt;
	}
	
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
	
	public String getCorrectAnswer() {
		return correctAnswer;
	}
	
	public void setCorrectAnswer(String correctAnswer) {
		this.correctAnswer = correctAnswer;
	}

	@Override
	public HashMap<String, Object> toHashMap() {
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put("prompt", prompt);
		m.put("challengeID", challengeID);
		if (!StringHelper.isEmpty(successMessage)) m.put("successMessage", successMessage);
		if (!StringHelper.isEmpty(failureMessage)) m.put("failureMessage", failureMessage);
		return m;
	}

	@Override
	public boolean isAnswerOK(Object answer) {
		// A prompt challenge should be receiving a String as an answer
		if (! (answer instanceof String)) return false;
		if (StringHelper.isEmpty(correctAnswer)) return false;
		String a = (String)answer;
		if (StringHelper.isEmpty(a)) return false;
		return a.equals(correctAnswer);
	}
}
