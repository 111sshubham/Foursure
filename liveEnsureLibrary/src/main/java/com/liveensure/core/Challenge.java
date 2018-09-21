package com.liveensure.core;

import java.io.Serializable;
import java.util.HashMap;

import com.liveensure.util.Hashable;
import com.liveensure.util.StringHelper;

public class Challenge implements Hashable, Serializable {

	private static final long serialVersionUID = 1L;
	private String question;
    private String answer;
    private int begin;
    private int end;

    /**
     * Empty constructor used when deserializing.
     */
    public Challenge() {
    }

    /**
     * Constructs the Challenge with the given question and the beginning and
     * ending of the substring.
     *
     * @param question
     * @param begin
     * @param end
     */
    public Challenge(String question, String answer, int begin, int end) {
        this.question = question;
        this.answer = answer;
        this.begin = begin;
        this.end = end;
    }

    /**
     * @return Returns the beginning of the substring.
     */
    public int getBegin() {
        synchronized (this) {
            return begin;
        }
    }

    /**
     * @param begin The beginning of the substring to set.
     */
    public void setBegin(int begin) {
        synchronized (this) {
            this.begin = begin;
        }
    }

    /**
     * @return Returns the ending of the substring.
     */
    public int getEnd() {
        synchronized (this) {
            return end;
        }
    }

    /**
     * @param end The ending of the substring to set.
     */
    public void setEnd(int end) {
        synchronized (this) {
            this.end = end;
        }
    }

    /**
     * @return Returns the question of the challenge.
     */
    public String getQuestion() {
        synchronized (this) {
            return question;
        }
    }

    /**
     * @param question The question of the challenge to set.
     */
    public void setQuestion(String question) {
        synchronized (this) {
            this.question = question;
        }
    }

    /**
     * @param question The question of the challenge to set.
     */
    public void setAnswer(String answer) {
        synchronized (this) {
            this.answer = answer;
        }
    }

    /**
     * @return Returns the question of the challenge.
     */
    public String getAnswer() {
        synchronized (this) {
            return answer;
        }
    }

    /**
     * Returns the hashable answer
     */
    public String getHashable() {
        synchronized (this) {
            return StringHelper.guaranteedSubstring(answer.toLowerCase().trim(), begin, end);
        }
    }
    
    public HashMap<String, String> toHashMap() {
    	HashMap<String, String> map = new HashMap<String,String>();
    	map.put("question", question);
    	map.put("begin", String.valueOf(begin));
    	map.put("end", String.valueOf(end));
    	return map;
    }

    @Override
    public Object clone() {
        synchronized (this) {
            return new Challenge(question, answer, begin, end);
        }
    }

}
