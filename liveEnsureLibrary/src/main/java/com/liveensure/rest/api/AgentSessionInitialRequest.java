package com.liveensure.rest.api;

public class AgentSessionInitialRequest
{

  // required for changing a session's status
  private String sessionToken;
  private String deviceFingerprint;
  private String agentVersion;
  private int    agentType;
  private String agentLaunch;
  private String country;

  /**
   * @return the sessionToken
   */
  public String getSessionToken()
  {
    return sessionToken;
  }

  /**
   * @param sessionToken
   *          the sessionToken to set
   */
  public void setSessionToken(String sessionToken)
  {
    this.sessionToken = sessionToken;
  }

  public String getDeviceFingerprint()
  {
    return deviceFingerprint;
  }

  public void setDeviceFingerprint(String deviceFingerprint)
  {
    this.deviceFingerprint = deviceFingerprint;
  }

  public String getAgentVersion()
  {
    return agentVersion;
  }

  public void setAgentVersion(String agentVersion)
  {
    this.agentVersion = agentVersion;
  }

  public int getAgentType()
  {
    return agentType;
  }

  public void setAgentType(int agentType)
  {
    this.agentType = agentType;
  }

  public String getCountry()
  {
    return country;
  }

  public void setCountry(String country)
  {
    this.country = country;
  }

  public String getAgentLaunch()
  {
    return agentLaunch;
  }

  public void setAgentLaunch(String agentLaunch)
  {
    this.agentLaunch = agentLaunch;
  }

}
