package com.liveensure.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import com.liveensure.util.Log;

/**
 * A basic wrapper for the state information available in an authentication session run against an ID Insure or LiveEnsure server. This session object can be
 * retrieved after the user has obtained a valid session token and can be used to determine the appropriate next steps.
 * <p/>
 * For example, the primary decision to make after retrieving a session is whether the user needs to register or authenticate. This decision is represented in
 * the combination of the registered and deviceRegistered properties.
 * <p/>
 * <ul>
 * <li>registered:false ==> initial registration of a new user
 * <li>registered:true, deviceRegistered:false ==> existing user registering a new device
 * <li>registered:true, deviceRegistered:true ==> existing user on known, good hardware (authentication only)
 * </ul>
 */
public class IDSession implements Serializable
{

  private static final long   serialVersionUID = 443333982016280780L;
  private static final String TAG              = "IDSession";
  private boolean             registered;
  private boolean             deviceRegistered;
  private boolean             tokenRequired;
  private boolean             tokenExists;
  private boolean             hardwareOnly;
  private boolean             morph;
  private boolean             morphAllowed;
  private int                 userDeviceCount;
  private int                 maxTries;
  private int                 maxDevicesAllowed;
  private int                 sessionTimeLeftToLive;
  private String              liveEnsureUrl;
  private String              sessionToken;
  private String              message;
  private String              userLocale;
  private String              statusURL;
  private String              serverKeyStatus;
  private String              serverKeyParam;
  private String              deviceName;
  private String              tokenChallenge;
  private String              authSuccessMessage;
  private String              initialStatus;
  private String              initialMessage;
  private List<?>             questions;
  private HashMap<?, ?>       properties;

  private boolean             error;
  private boolean             networkError;
  private String              errorMessage;
  private boolean             canManageDevices;
  private boolean             completed;
  private boolean             success;
  private String              launchType;

  public IDSession(String liveEnsureUrl, String sessionToken, String agentLaunchType)
  {
    this.liveEnsureUrl = liveEnsureUrl;
    this.sessionToken = sessionToken;
    this.launchType = agentLaunchType;
  }

  public IDSession(String liveEnsureUrl, String sessionToken, String agentLaunchType, int secsLeft)
  {
    this.liveEnsureUrl = liveEnsureUrl;
    this.sessionToken = sessionToken;
    this.launchType = agentLaunchType;
    this.sessionTimeLeftToLive = secsLeft;
  }

  public boolean isRegistered()
  {
    return registered;
  }

  public void setRegistered(boolean registered)
  {
    this.registered = registered;
  }

  public boolean isDeviceRegistered()
  {
    return deviceRegistered;
  }

  public void setDeviceRegistered(boolean deviceRegistered)
  {
    this.deviceRegistered = deviceRegistered;
  }

  public boolean isTokenRequired()
  {
    return tokenRequired;
  }

  public void setTokenRequired(boolean tokenRequired)
  {
    this.tokenRequired = tokenRequired;
  }

  public boolean isTokenExists()
  {
    return tokenExists;
  }

  public void setTokenExists(boolean tokenExists)
  {
    this.tokenExists = tokenExists;
  }

  public String getTokenChallenge()
  {
    return tokenChallenge;
  }

  public void setTokenChallenge(String tokenChallenge)
  {
    this.tokenChallenge = tokenChallenge;
  }

  public boolean isHardwareOnlyOK()
  {
    return hardwareOnly;
  }

  public void setHardwareOnlyOK(boolean hardwareOnly)
  {
    this.hardwareOnly = hardwareOnly;
  }

  public boolean isMorph()
  {
    return morph;
  }

  public void setMorph(boolean morph)
  {
    this.morph = morph;
  }

  public boolean isMorphAllowed()
  {
    return morphAllowed;
  }

  public void setMorphAllowed(boolean morphAllowed)
  {
    this.morphAllowed = morphAllowed;
  }

  public int getUserDeviceCount()
  {
    return userDeviceCount;
  }

  public void setUserDeviceCount(int userDeviceCount)
  {
    this.userDeviceCount = userDeviceCount;
  }

  public int getMaxTries()
  {
    return maxTries;
  }

  public void setMaxTries(int maxTries)
  {
    this.maxTries = maxTries;
  }

  public int getMaxDevicesAllowed()
  {
    return maxDevicesAllowed;
  }

  public void setMaxDevicesAllowed(int maxDevicesAllowed)
  {
    this.maxDevicesAllowed = maxDevicesAllowed;
  }

  public int getSessionTimeLeftToLive()
  {
    if (sessionTimeLeftToLive == 0) sessionTimeLeftToLive = 600; // default is 10 minutes; value is in seconds.
    return sessionTimeLeftToLive;
  }

  public void setSessionTimeLeftToLive(int sessionTimeLeftToLive)
  {
    this.sessionTimeLeftToLive = sessionTimeLeftToLive;
  }

  public String getMessage()
  {
    return message;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getUserLocale()
  {
    return userLocale;
  }

  public void setUserLocale(String userLocale)
  {
    this.userLocale = userLocale;
  }

  public String getStatusURL()
  {
    return statusURL;
  }

  public void setStatusURL(String statusURL)
  {
    this.statusURL = statusURL;
  }

  public String getServerKeyStatus()
  {
    return serverKeyStatus;
  }

  public void setServerKeyStatus(String serverKeyStatus)
  {
    this.serverKeyStatus = serverKeyStatus;
  }

  public String getServerKeyParam()
  {
    return serverKeyParam;
  }

  public void setServerKeyParam(String serverKeyParam)
  {
    this.serverKeyParam = serverKeyParam;
  }

  public String getDeviceName()
  {
    return deviceName;
  }

  public void setDeviceName(String deviceName)
  {
    this.deviceName = deviceName;
  }

  public List<?> getQuestions()
  {
    return questions;
  }

  public void setQuestions(List<?> questions)
  {
    this.questions = questions;
  }

  public boolean hasError()
  {
    return error;
  }

  public void setError(boolean error)
  {
    this.error = error;
  }

  public String getErrorMessage()
  {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage)
  {
    this.errorMessage = errorMessage;
  }

  public String getProperty(String key)
  {
    if (properties != null) {
      return (String) properties.get(key);
    }
    return null;
  }

  public HashMap<?, ?> getProperties()
  {
    return properties;
  }

  public void setProperties(HashMap<?, ?> properties)
  {
    this.properties = properties;
  }

  public String getAuthSuccessMessage()
  {
    return authSuccessMessage;
  }

  public void setAuthSuccessMessage(String authSuccessMessage)
  {
    this.authSuccessMessage = authSuccessMessage;
  }

  public String getLiveEnsureUrl()
  {
    return liveEnsureUrl;
  }

  public void setLiveEnsureUrl(String liveEnsureUrl)
  {
    this.liveEnsureUrl = liveEnsureUrl;
  }

  public String getSessionToken()
  {
    return sessionToken;
  }

  public void setSessionToken(String sessionToken)
  {
    this.sessionToken = sessionToken;
  }

  public void setNetworkError(boolean error)
  {
    this.networkError = error;
  }

  public boolean hasNetworkError()
  {
    return this.networkError;
  }

  public void setCanManageDevices(boolean b)
  {
    Log.i(TAG, "Setting canManageDevices to " + b);
    this.canManageDevices = b;
  }

  public boolean canManageDevices()
  {
    Log.i(TAG, "returning canManageDevices as " + this.canManageDevices);
    return this.canManageDevices;
  }

  public String getInitialMessage()
  {
    return initialMessage;
  }

  public void setInitialMessage(String initialMessage)
  {
    this.initialMessage = initialMessage;
  }

  public String getInitialStatus()
  {
    return initialStatus;
  }

  public void setInitialStatus(String initialStatus)
  {
    this.initialStatus = initialStatus;
  }

  public void setCompleted(boolean b)
  {
    this.completed = b;
  }

  public boolean isCompleted()
  {
    return completed;
  }

  public boolean isSuccess()
  {
    return success;
  }

  public void setSuccess(boolean success)
  {
    this.success = success;
  }

  public String getLaunchType()
  {
    return launchType;
  }

  public void setLaunchType(String launchType)
  {
    this.launchType = launchType;
  }

}
