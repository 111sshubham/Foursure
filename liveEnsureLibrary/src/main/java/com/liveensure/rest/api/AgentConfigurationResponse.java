package com.liveensure.rest.api;

import java.util.HashMap;

public class AgentConfigurationResponse extends LiveResponse {
	
	private HashMap<String, String> registrations;

	/**
	 * @return the registrations
	 */
	public HashMap<String, String> getRegistrations() {
		return registrations;
	}

	/**
	 * @param registrations the registrations to set
	 */
	public void setRegistrations(HashMap<String, String> registrations) {
		this.registrations = registrations;
	}

}
