package com.liveensure.a.mini;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import com.liveensure.core.AgentFSM;
import com.liveensure.core.IDSession;
import com.liveensure.rest.api.AgentChallengeResponse;
import com.liveensure.util.Log;
import com.liveensure.util.StringHelper;

public class BaseActivity extends Activity
{

  protected final static String IDENTIFIER_CHAL                   = "CHALLENGE.ser";
  protected final static String TAG                               = BaseActivity.class.getSimpleName();

  protected ProgressBar         pBarAuth;

  // Create a Handler so that we can send messages to the UI thread. In Android,
  // the Handler is associated with the thread that created it. Right now we're in the UI thread,
  // so later in our other threads we can call stuff using this Handler
  final Handler                 uiThreadHandler                   = new Handler();

  private static final String   IDENTIFIER_API_BASE_KEY           = "le.api.base";
  private static final String   IDENTIFIER_SESSION_TOKEN_KEY      = "le.session.token";
  private static final String   IDENTIFIER_SESSION_EXPIRATION_KEY = "le.session.expiration";
  private static final String   IDENTIFIER_CURRENT_STATE_KEY      = "le.current.state";
  private static final String   IDENTIFIER_LAUNCH_TYPE_KEY        = "le.launch.type";
  private static final String   IDENTIFIER_IN_PURCHASE_KEY        = "le.in.purchase";
  private LEApplication         mLEApp;

  /**
   * Method which is executed when the user clicks the Menu button on the Android device.
   * 
   * @param menu
   * @return
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    //		getMenuInflater().inflate(R.menu.main, menu);
    return Boolean.TRUE;
  }

  /**
   * Method which is called when the Android Menu is activated and clicked to move to another tab in the application.
   * 
   * @param item
   * @return
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {

    //		switch (item.getItemId()) {
    //		case (R.id.menu_item_qr): {
    //			Intent scanIntent = new Intent(Intents.Scan.ACTION);
    //			scanIntent.putExtra(Intents.Scan.MODE, Intents.Scan.QR_CODE_MODE);
    //			startActivityForResult(scanIntent, 0);
    //			break;
    //		}
    //		}

    return Boolean.TRUE;
  }

  protected void setLaunchedBy3rdParty(boolean launchedBy3rdParty)
  {
    getApp().setLaunchedBy3rdParty(launchedBy3rdParty);
  }

  protected boolean wasLaunchedBy3rdParty()
  {
    return getApp().wasLaunchedBy3rdParty();
  }

  protected void determineLaunchMechanism()
  {
    boolean appToApp = isAppToApp();
    boolean webToApp = isWebToApp();
    Log.e(TAG, "app to app is " + appToApp + ", web to app is " + webToApp);
    if (appToApp || webToApp) {
      setLaunchedBy3rdParty(Boolean.TRUE);
      clearSavedSessionState();
    }
    else {
      setLaunchedBy3rdParty(Boolean.FALSE);
      loadSessionState();
    }
  }

  protected void clearSavedSessionState()
  {
    deleteStoredChallenge();
    SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.session_credentials), MODE_PRIVATE).edit();
    editor.clear();
    editor.commit();
  }

  protected void loadSessionState()
  {
    SharedPreferences prefs = getSharedPreferences(getString(R.string.session_credentials), MODE_PRIVATE);
    String sessionToken = prefs.getString(IDENTIFIER_SESSION_TOKEN_KEY, null);
    String apiBase = prefs.getString(IDENTIFIER_API_BASE_KEY, null);
    String launchType = prefs.getString(IDENTIFIER_LAUNCH_TYPE_KEY, null);
    int currentState = prefs.getInt(IDENTIFIER_CURRENT_STATE_KEY, 0);
    long sessionExpiration = prefs.getLong(IDENTIFIER_SESSION_EXPIRATION_KEY, 0);
    boolean inPurchase = ("true".equals(prefs.getString(IDENTIFIER_IN_PURCHASE_KEY, "false")));
    AgentChallengeResponse chal = deserializeChallenge();
    Log.i(TAG, "Restoring session state");
    Log.i(TAG, "sessionToken: " + sessionToken);
    Log.i(TAG, "apiBase: " + apiBase);
    Log.i(TAG, "launchType: " + launchType);
    Log.i(TAG, "currentState: " + currentState);
    Log.i(TAG, "sessionExpiration: " + sessionExpiration);
    Log.i(TAG, "inPurchase: " + inPurchase);
    Log.i(TAG, "chal: " + chal);
    getApp().setInPurchase(inPurchase);
    if (sessionExpiration > 0 && sessionExpiration < System.currentTimeMillis()) {
      Log.w(TAG, "loaded a saved session, but it has expired.  Clearing saved state and starting a new session");
      clearSavedSessionState();
    }
    else {
      if (sessionToken != null && apiBase != null) {
        Log.w(TAG, "Found a valid saved session, restoring state to AgentFSM and resuming session");
        AgentFSM fsm = new AgentFSM(getApp());
        fsm.restoreState(sessionToken, apiBase, currentState, chal, launchType, sessionExpiration);
        fsm.setUI((UIForFiniteStateMachine) this);
        setAgentFSM(fsm);
      }
      else {
        clearSavedSessionState();
      }
    }
  }

  protected void saveSessionState()
  {
    if (!getAgentFSM().isActive()) {
      return;
    }
    Log.w(TAG, "in saveSessionState()");
    SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.session_credentials), MODE_PRIVATE).edit();
    editor.putString(IDENTIFIER_SESSION_TOKEN_KEY, getAgentFSM().getSession().getSessionToken());
    editor.putString(IDENTIFIER_API_BASE_KEY, getAgentFSM().getApiBase());
    editor.putString(IDENTIFIER_LAUNCH_TYPE_KEY, getAgentFSM().getSession().getLaunchType());
    editor.putInt(IDENTIFIER_CURRENT_STATE_KEY, getAgentFSM().getCurrentState());
    editor.putString(IDENTIFIER_IN_PURCHASE_KEY, getApp().isInPurchase() ? "true" : "false");
    int timeLeftInSeconds = getIDSession().getSessionTimeLeftToLive();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.SECOND, timeLeftInSeconds);
    long sessionExpiresInMillis = cal.getTimeInMillis();
    Log.i(TAG, "Session expires timestamp [" + sessionExpiresInMillis + "]");
    editor.putLong(IDENTIFIER_SESSION_EXPIRATION_KEY, sessionExpiresInMillis);
    editor.commit();

    serializeChallenge();
  }

  protected boolean isSessionValid()
  {
    SharedPreferences credentials = getSharedPreferences(getString(R.string.session_credentials), MODE_PRIVATE);
    long sessionExpiresInMillis = credentials.getLong(getString(R.string.session_expiration), 0);
    long currentTime = System.currentTimeMillis();
    return (sessionExpiresInMillis > 0 && currentTime < sessionExpiresInMillis);
  }

  protected void showDeviceKeyboard()
  {
    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    if (mgr != null) {
      mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
  }

  protected void hideDeviceKeyboard(View view)
  {
    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    if (mgr != null) {
      mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  protected void setProgressBarVisible(boolean showit)
  {
    if (pBarAuth != null) pBarAuth.setVisibility(showit ? View.VISIBLE : View.INVISIBLE);
  }

  public LEApplication getApp()
  {
    return mLEApp;
  }

  public void setApp(LEApplication leApp)
  {
    mLEApp = leApp;
  }

  public IDSession getIDSession()
  {
    return getApp().getSession();
  }

  public void resetLiveEnsureApplication()
  {
    if (getAgentFSM() != null) {
      getAgentFSM().resetSession();
    }
  }

  public boolean isAppToApp()
  {
    String sessionToken = null;
    String identityServer = null;
    Intent startupIntent = getApp().getStartupIntent();
    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      sessionToken = extras.getString("sessionToken");
      identityServer = extras.getString("identityServer");
      Log.w(TAG, "in isAppToApp(), extras.getString(sessionToken) is " + sessionToken);
    }
    if (sessionToken == null) {
      sessionToken = getIntent().getStringExtra("sessionToken");
      Log.w(TAG, "in isAppToApp(), getIntent().getStringExtra(sessionToken) is " + sessionToken);
    }
    if (identityServer == null) {
      identityServer = getIntent().getStringExtra("identityServer");
    }
    if (sessionToken == null) {
      // check the intent from the startupactivity (stored in the app object)
      if (startupIntent != null) {
        sessionToken = startupIntent.getStringExtra("sessionToken");
        Log.w(TAG, "in isAppToApp(), getApp().getStartupIntent(sessionToken) is " + sessionToken);
      }
    }
    if (identityServer == null) {
      // check the intent from the startupactivity (stored in the app object)
      if (startupIntent != null) {
        identityServer = startupIntent.getStringExtra("identityServer");
      }
    }

    if (!StringHelper.isEmpty(sessionToken) && !StringHelper.isEmpty(identityServer)) {
      getAgentFSM().setupSession(sessionToken, identityServer, "rollover");
      return Boolean.TRUE;
    }
    else {
      // one other possible roll scenario, where the startup intent does not specify a token, but wants to deliberately roll into camera scan
      if (isRollToScan()) return Boolean.TRUE;
      Log.e(TAG, "appToApp is false");
      return Boolean.FALSE;
    }
  }

  public boolean isWebToApp()
  {
    Log.i(TAG, "in isWebToApp()");
    Uri data = getIntent().getData();
    if (data == null) return Boolean.FALSE;
    Log.i(TAG, "data Uri object is not null, full Uri is: " + data.getEncodedQuery());
    String scheme = data.getScheme(); // "liveensure", "http"
    if (scheme == null) {
      Log.i(TAG, "data.getScheme() is null, trying getApp().getStartupIntent()");
      // check the intent from the startupactivity (stored in the app object)
      Intent i = getApp().getStartupIntent();
      data = i != null ? i.getData() : null;
      scheme = data != null ? data.getScheme() : null;
    }
    // if we were unable to get a launch scheme
    if (scheme == null) {
      Log.i(TAG, "getApp().getStartupIntent() is null, returning false");
      return Boolean.FALSE;
    }
    String path = data.getPath();
    Log.i(TAG, "path is " + path);
    Log.i(TAG, "scheme is " + scheme);
    if (scheme.toLowerCase(Locale.US).equals("http")) {
      Log.i(TAG, "scheme is http");
      Set<String> p = data.getQueryParameterNames();
      for (String s : p) {
        Log.i(TAG, "got param " + s + ", value: " + data.getQueryParameter(s));
      }
      String sessionToken = data.getQueryParameter("sessionToken");
      String identityServer = data.getQueryParameter("status");
      String bgColor = data.getQueryParameter("backgroundColorARGB");
      Log.i(TAG, "sessionToken is " + sessionToken + ", status URL is " + identityServer + ", color is " + bgColor);
      if (!StringHelper.isEmpty(sessionToken) && !StringHelper.isEmpty(identityServer)) {
        if (identityServer.endsWith("/idr")) {
          // V3 API server URL, replace idr with rest
          identityServer = identityServer.substring(0, identityServer.length() - 4) + "/rest";
        }
        getAgentFSM().setupSession(sessionToken, identityServer, "rollover");
        return Boolean.TRUE;
      }
    }
    if (scheme.toLowerCase(Locale.US).equals("miniliveensure")) {
      Log.i(TAG, "scheme is miniliveensure");
      String sessionToken = data.getQueryParameter("sessionToken");
      String identityServer = data.getQueryParameter("status");
      if (StringHelper.isEmpty(sessionToken)) {
        sessionToken = getIntent().getExtras().getString("sessionToken");
      }
      if (StringHelper.isEmpty(identityServer)) {
        identityServer = getIntent().getExtras().getString("status");
      }
      Log.i(TAG, "sessionToken is " + sessionToken + ", status URL is " + identityServer);
      if (!StringHelper.isEmpty(sessionToken) && !StringHelper.isEmpty(identityServer)) {
        if (identityServer.endsWith("/idr")) {
          // V3 API server URL, replace idr with rest
          identityServer = identityServer.substring(0, identityServer.length() - 4) + "/rest";
        }
        getAgentFSM().setupSession(sessionToken, identityServer, "rollover");
        return Boolean.TRUE;
      }
    }
    // rollover to full screen
    if (scheme.toLowerCase(Locale.US).equals("liveensure")) {
      Log.i(TAG, "scheme is liveensure");
      String sessionToken = data.getQueryParameter("sessionToken");
      String identityServer = data.getQueryParameter("status");
      Log.i(TAG, "sessionToken is " + sessionToken + ", status URL is " + identityServer);
      if (!StringHelper.isEmpty(sessionToken) && !StringHelper.isEmpty(identityServer)) {
        if (identityServer.endsWith("/idr")) {
          // V3 API server URL, replace idr with rest
          identityServer = identityServer.substring(0, identityServer.length() - 4) + "/rest";
        }
        getAgentFSM().setupSession(sessionToken, identityServer, "rollover");
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }

  public boolean isRollToScan()
  {
    Intent intent = getIntent();
    if (intent == null) intent = getApp().getStartupIntent();
    if (intent != null) {
      Uri data = intent.getData();
      if (data != null) {
        String path = data.getEncodedPath();
        // path should look like http://liveensure.com/roll/ or http://liveensure.com/scan/  
        if (path != null && path.startsWith("/scan")) return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }

  private void deleteStoredChallenge()
  {
    getApplicationContext().deleteFile(IDENTIFIER_CHAL);
  }

  /**
   * Method which serializes the IDAgent object to disk for later use.
   */
  private void serializeChallenge()
  {
    if (getAgentFSM() == null || getAgentFSM().getCurrentChallenge() == null) return;
    AgentChallengeResponse chal = getAgentFSM().getCurrentChallenge();
    if (!getAgentFSM().isChallengeRestorable(chal)) return;
    Log.e(TAG, "serializing challenge, ID: " + chal.getChallengeID());
    FileOutputStream fos = null;
    ObjectOutputStream oos = null;
    try {
      fos = getApplicationContext().openFileOutput(IDENTIFIER_CHAL, Context.MODE_PRIVATE);
      oos = new ObjectOutputStream(fos);
      oos.writeObject(chal);
    }
    catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace();
    }
    catch (IOException io) {
      io.printStackTrace();
    }
    finally {
      try {
        if (fos != null) fos.close();
      }
      catch (IOException io) {
      }
      try {
        if (oos != null) oos.close();
      }
      catch (IOException io) {
      }
    }
  }

  private AgentChallengeResponse deserializeChallenge()
  {
    AgentChallengeResponse chal = null;
    FileInputStream fis = null;
    ObjectInputStream ois = null;
    try {
      fis = getApplicationContext().openFileInput(IDENTIFIER_CHAL);
      ois = new ObjectInputStream(fis);
      chal = (AgentChallengeResponse) ois.readObject();
    }
    catch (FileNotFoundException e) {
      // e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    finally {
      try {
        if (fis != null) fis.close();
      }
      catch (IOException io) {
      }
      try {
        if (ois != null) ois.close();
      }
      catch (IOException io) {
      }
    }
    if (chal != null) {
      // Log.e(TAG, "deserialized challenge, ID: " + chal.getChallengeID());
    }
    return chal;
  }

  public AgentFSM getAgentFSM()
  {
    return getApp().getAgentFSM();
  }

  public void setAgentFSM(AgentFSM a)
  {
    getApp().setAgentFSM(a);
  }
}
