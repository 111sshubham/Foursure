/**
 * 
 */
package com.liveensure.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.liveensure.util.MessageDigester2;
import com.liveensure.util.StringHelper;

/**
 * @author marc
 *
 */
public class TemplateChallenge extends DynamicChallenge {

	private static final long serialVersionUID = 1L;
	private List<Challenge> challenges = new ArrayList<Challenge>();
	private String correctAnswer;
	
	public TemplateChallenge() {
		challengeType = DynamicChallengeType.TEMPLATE;
	}
	
	/**
	 * @return the correctAnswer
	 */
	public String getCorrectAnswer() {
		return correctAnswer;
	}

	public void addChallenge(Challenge c) {
		challenges.add(c);
		this.correctAnswer = MessageDigester2.getHash(challenges);
	}
	
	/**
	 * @see com.liveensure.core.DynamicChallenge#isAnswerOK(java.lang.Object)
	 */
	@Override
	public boolean isAnswerOK(Object answer) {
		if (! (answer instanceof String)) return false;
		if (StringHelper.isEmpty(correctAnswer)) return false;
		String a = (String)answer;
		if (StringHelper.isEmpty(a)) return false;
		return a.equals(correctAnswer);
	}

	/**
	 * @see com.liveensure.core.DynamicChallenge#toHashMap()
	 */
	@Override
	public HashMap<String, Object> toHashMap() {
		HashMap<String, Object> m = new HashMap<String, Object>();
		ArrayList<HashMap<String,String>> clist = new ArrayList<HashMap<String,String>>();
		for (Challenge c : challenges) {
			clist.add(c.toHashMap());
		}
		m.put("challengeID", challengeID);
		m.put("challenges", clist);		
		return m;
	}

}
