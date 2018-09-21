package com.foursure1;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.foursure1.api.AccessShareResponse;
import com.foursure1.api.CreateShareResponse;
import com.foursure1.api.LiveResponse;
import com.foursure1.api.SaveSettingsResponse;
import com.foursure1.api.SignupResponse;
import com.foursure1.api.StartResponse;
import com.foursure1.api.StatusResponse;
import com.foursure1.util.FactorType;
import com.foursure1.util.ShareType;
import com.foursure1.util.Status;
import com.google.gson.Gson;
import com.liveensure.util.StringHelper;

public class FourSureFSM {
	private final String TAG = FourSureFSM.class.getSimpleName();
	private transient Gson gson = new Gson();
	private String mDefaultHost = "localhost";
	private String mApiBase;
	private FourSureApplication mFSApp;
	private boolean mConnecting;
	private Handler mBusyCueHandler;
	private Runnable mBusyCueTask;
	private UIForFiniteStateMachine mUI;
	private FourSureSession mSession;
	private int mCurrentState;
	protected boolean mChaining;
	private int mStatusErrors;
	public static final int DEVICE_SIGNUP_START = 1;
	public static final int DEVICE_SIGNUP_SUCCESS = 2;
	public static final int DEVICE_SIGNUP_FAILED = 3;
	public static final int SESSION_START = 4;
	public static final int SESSION_SUCCESS = 5;
	public static final int SESSION_FAILED = 6;
	public static final int SESSION_STATUS = 7;
	public static final int SESSION_STATUS_UNDETERMINED = 8;
	public static final int SESSION_STATUS_SUCCESS = 9;
	public static final int SESSION_STATUS_FAILED = 10;
	public static final int IN_LIVEENSURE_SESSION = 11;
	public static final long SHOW_BUSY_CUES_DELAY_MS = 10;
	public static final int SHARE_UPDATE = 11;
	public static final int SHARE_UPDATE_SUCCESS = 12;
	public static final int SHARE_UPDATE_FAILED = 13;
	public static final int SHARE_ACCESS = 14;
	public static final int SHARE_ACCESS_SUCCESS = 15;
	public static final int SHARE_ACCESS_FAILED = 16;
	public static final int NO_SETTINGS_AVAILABLE = 17;
	public static final int SAVE_SETTINGS_START = 18;
	public static final int SAVE_SETTINGS_SUCCESS = 19;
	public static final int SAVE_SETTINGS_FAILED = 20;
	public static final String STATUS_SUCCESS = "SUCCESS";
	public static final String STATUS_UNDETERMINED = "SESSION_UNDETERMINED";
	public static final String STATUS_FAILED = "FAILED";

	/**
	 * constructor
	 */
	public FourSureFSM(FourSureApplication app) {
		Log.w(TAG, "in constructor for FourSureFSM()");
		this.mFSApp = app;
		resetState();
		mBusyCueHandler = new Handler();
		mBusyCueTask = new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "busy cue task was started: " + isConnecting());
				mUI.showBusyCues(isConnecting());
			}
		};
	}

	public void resetState() {
		if (mSession != null && !StringHelper.isEmpty(mSession.getSessionToken())) {
			makeCloseCall();
		}
		mSession = null;
		setCurrentState(0);
		mApiBase = mFSApp.getApiHost();
	}

	private void makeCloseCall() {
		// TODO Auto-generated method stub

	}

	private void displayError(String message) {
		Log.e(TAG, message);
		mUI.showBusyCues(false);
		mUI.handleError(message);
	}

	/**
	 * general REST client method. take the URL, body (if needed) and method and perform the call. The response string is handed to the handleResponse() method to examine the state of the FSM and act
	 * accordingly
	 * 
	 * @param resourceURL
	 * @param reqBody
	 * @param httpMethod
	 */
	public void makeCallTo(String resourceURL, final String reqBody, String httpMethod) {

		if (mApiBase == null || StringHelper.isEmpty(mApiBase)) {
//			mApiBase = mDefaultHost;
			mApiBase = "https://fs.mastlabs.com/fs/api/v1";
			Log.w(TAG, "Using default API base of " + mApiBase + " because of absent mApiBase");
		}
		String fullUrl = mApiBase + resourceURL;
		Log.i(TAG, httpMethod + " call to " + fullUrl);
		if (!StringHelper.isEmpty(reqBody))
			Log.i(TAG, reqBody);
		RequestQueue queue = mFSApp.getRequestQueue();
		Request<String> req = null;
		if ("GET".equals(httpMethod)) {
			req = new StringRequest(Request.Method.GET, fullUrl, createNetworkRequestSuccessListener(), createNetworkRequestErrorListener()) {
				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					Map<String, String> params = new HashMap<String, String>();
					params.put("Accept", "application/json");
					params.put("Content-Type","application/json");
					return params;
				}
			};
		}
		if ("DELETE".equals(httpMethod)) {
			req = new StringRequest(Request.Method.DELETE, fullUrl, createNetworkRequestSuccessListener(), createNetworkRequestErrorListener()) {
				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					Map<String, String> params = new HashMap<String, String>();
					params.put("Accept", "application/json");
					params.put("Content-Type","application/json");
					return params;
				}
			};
		}
		if ("PUT".equals(httpMethod)) {
			req = new StringRequest(Request.Method.PUT, fullUrl, createNetworkRequestSuccessListener(), createNetworkRequestErrorListener()) {
				@Override
				public byte[] getBody() {
					return reqBody.getBytes();
				}

				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					Map<String, String> params = new HashMap<String, String>();
					params.put("Accept", "application/json");
					params.put("Content-Type","application/json");
					return params;
				}

				@Override
				public String getBodyContentType() {
					return "application/json";
				}
			};
		}
		if ("POST".equals(httpMethod)) {
			req = new StringRequest(Request.Method.POST, fullUrl, createNetworkRequestSuccessListener(), createNetworkRequestErrorListener()) {
				@Override
				public byte[] getBody() {
					return reqBody.getBytes();
				}

				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					Map<String, String> params = new HashMap<String, String>();
					 params.put("Content-Type", "application/json");
					params.put("Accept", "application/json");
					return params;
				}

				@Override
				public String getBodyContentType() {
					return "application/json";
				}
			};
		}
		if (req != null) {
			Log.w(TAG, "Adding volley request to request queue");
			// update the UI
			setConnecting(true);
			mBusyCueHandler.postDelayed(mBusyCueTask, SHOW_BUSY_CUES_DELAY_MS);
			queue.add(req);
		} else {
			Log.e(TAG, "Unable to determine request type from HTTP method, skipping this request");
		}
	}

	private Response.Listener<String> createNetworkRequestSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				// update the UI
				setConnecting(false);
				mBusyCueHandler.removeCallbacks(mBusyCueTask);
				if (!mChaining) {
					mUI.showBusyCues(false);
				}
				handleResponse(response);
			}
		};
	}

	private Response.ErrorListener createNetworkRequestErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				// update the UI
				setConnecting(false);
				mBusyCueHandler.removeCallbacks(mBusyCueTask);
				if (!mChaining) {
					mUI.showBusyCues(false);
				}
				NetworkResponse resp = error.networkResponse;
				String respBody = "unknown error (null server response)";
				Log.e(TAG, "in onErrorResponse(), network HTTP response: " + error.getMessage());
				if (resp != null) {
					respBody = new String(resp.data);
					Log.e(TAG, "network request failed with code: " + resp.statusCode);
					Log.e(TAG, "error body is:" + respBody);
				}
				handleError(respBody);
			}
		};
	}

	/**
	 * handler for successful (200) network requests - see @createNetworkRequestSuccessListener
	 * 
	 * @param serverResponse
	 */
	private void handleResponse(String serverResponse) {
		if (StringHelper.isEmpty(serverResponse)) {
			Log.w(TAG, "server response was null, cannot process response");
			displayError(mFSApp.getApplicationContext().getResources().getString(R.string.session_error));
			return;
		}
		// we made it this far, we should have a 200 response with a non-null
		// entity body
		// let the state determine our next step
		Log.i(TAG, "handleResponse(), mCurrentState is " + getCurrentState());
		Log.i(TAG, "handleResponse(), response body is " + serverResponse);
		Date expDate = null;
		switch (getCurrentState()) {
		case DEVICE_SIGNUP_START:
			SignupResponse signupResponse = null;
			try {
				signupResponse = gson.fromJson(serverResponse, SignupResponse.class);
			} catch (Exception e) {
				Log.e(TAG, "Execption when parsing server signup response: " + serverResponse, e);
			}
			if (signupResponse == null) {
				Log.w(TAG, "Unable to process response from server");
				displayError("Error processing server response");
				setCurrentState(DEVICE_SIGNUP_FAILED);
				return;
			}
			if (StringHelper.isEmpty(signupResponse.getDeviceUUID())) {
				Log.w(TAG, "Signup did not create a device UUID");
				displayError("Signup did not create a device UUID");
				setCurrentState(DEVICE_SIGNUP_FAILED);
				return;
			}
			if (StringHelper.isEmpty(signupResponse.getLeSessionToken())) {
				Log.w(TAG, "Signup did not create an LE session");
				displayError("Signup did not create an LE session");
				setCurrentState(DEVICE_SIGNUP_FAILED);
				return;
			}
			if (StringHelper.isEmpty(signupResponse.getIdentityServer())) {
				Log.w(TAG, "Signup did not return an LE server");
				displayError("Signup did not return an LE server");
				setCurrentState(DEVICE_SIGNUP_FAILED);
				return;
			}
			// Store the FS data
			// TODO: eventually these will go into a prefs file for persistence
			// of an active, unexpired session for pause/restart
			mFSApp.setDeviceUUID(signupResponse.getDeviceUUID());
			mFSApp.setFsSessionToken(signupResponse.getFsSessionToken());
			mFSApp.setFsSessionExpiration(System.currentTimeMillis() + signupResponse.getFsSessionTTL());
			expDate = new Date(mFSApp.getFsSessionExpiration());
			Log.w(TAG, "calculated expiration time in ms as " + expDate.toString());
			mFSApp.saveAppPrefs();
			// TODO: trap the expiration time stamp and use it to check for a
			// possibly expired session?
			setCurrentState(DEVICE_SIGNUP_SUCCESS);
			// got an LE token and server, start LE
			mFSApp.setLESessionToken(signupResponse.getLeSessionToken());
			mFSApp.setLEIdentityServer(signupResponse.getIdentityServer());
			mUI.launchLiveEnsure();
			break;
		case SESSION_START:
			StartResponse startResponse = null;
			try {
				startResponse = gson.fromJson(serverResponse, StartResponse.class);
			} catch (Exception e) {
				Log.e(TAG, "Execption when parsing server start response: " + serverResponse, e);
			}
			if (startResponse == null) {
				Log.w(TAG, "Unable to process response from server");
				displayError("Error processing server response");
				setCurrentState(SESSION_FAILED);
				return;
			}
			// Check the status returned from the server
			if (startResponse.getStartStatus() == Status.LE_AUTHENTICATED) {
				// no need to start liveensure, we have a valid session still
				// going on the server, go straight to success for the current
				// UI Activity
				Log.w(TAG, "Session start indicates LE authentication complete");
				mFSApp.setFsSessionToken(startResponse.getFsSessionToken());
				mFSApp.setFsSessionExpiration(System.currentTimeMillis() + startResponse.getFsSessionTTL());
				expDate = new Date(mFSApp.getFsSessionExpiration());
				Log.w(TAG, "calculated expiration time in ms as " + expDate.toString());
				setCurrentState(SESSION_SUCCESS);
				mUI.showSuccess("");
			} else if (startResponse.getStartStatus() == Status.ACTIVE) {
				// normal session start, move on to LE now
				Log.w(TAG, "Session start indicates LE authentication is required");
				setCurrentState(SESSION_SUCCESS);
				mFSApp.setFsSessionToken(startResponse.getFsSessionToken());
				mFSApp.setFsSessionExpiration(System.currentTimeMillis() + startResponse.getFsSessionTTL());
				expDate = new Date(mFSApp.getFsSessionExpiration());
				Log.w(TAG, "calculated expiration time in ms as " + expDate.toString());
				mFSApp.setLESessionToken(startResponse.getLeSessionToken());
				mFSApp.setLEIdentityServer(startResponse.getIdentityServer());
				mUI.launchLiveEnsure();
			} else {
				// not in a valid session, and the server is unable to start an
				// LE session for some reason. error out
				Log.w(TAG, "Session start did not return an LE session");
				displayError("Unable to start session, please try again");
				setCurrentState(SESSION_STATUS_FAILED);
			}
			break;
		case SESSION_STATUS:
			StatusResponse resp = null;
			try {
				resp = gson.fromJson(serverResponse, StatusResponse.class);
			} catch (Exception e) {
				Log.e(TAG, "Execption when parsing server status response: " + serverResponse, e);
			}
			if (resp == null) {
				Log.w(TAG, "Unable to process response from server");
				displayError("Error processing server response");
				setCurrentState(SESSION_STATUS_FAILED);
				return;
			}

			gson.fromJson(serverResponse, StatusResponse.class);
			Log.w(TAG, "processing session status response, status is " + resp.getSessionStatus());
			if (STATUS_SUCCESS.equals(resp.getSessionStatus())) {
				setCurrentState(SESSION_STATUS_SUCCESS);
			} else if (STATUS_UNDETERMINED.equals(resp.getSessionStatus())) {
				setCurrentState(SESSION_STATUS_UNDETERMINED);
			} else {
				// failed
				mFSApp.setCurrentErrorMessage(mFSApp.getString(R.string.authentication_failed));
				setCurrentState(SESSION_STATUS_FAILED);
			}
			mStatusErrors = 0;
			mUI.handleStatusResult(mCurrentState);
			break;
		case SHARE_UPDATE:
			CreateShareResponse shareResponse = null;
			try {
				shareResponse = gson.fromJson(serverResponse, CreateShareResponse.class);
			} catch (Exception e) {
				Log.e(TAG, "Execption when parsing create share response: " + serverResponse, e);
			}
			if (shareResponse == null) {
				Log.w(TAG, "Unable to process response from server");
				displayError("Error processing server response");
				setCurrentState(SHARE_UPDATE_FAILED);
				return;
			}
			if (StringHelper.isEmpty(shareResponse.getShortCode())) {
				Log.w(TAG, "Share create did not return a short code");
				displayError("Share create did not return a short code");
				setCurrentState(SHARE_UPDATE_FAILED);
				return;
			}

			if (shareResponse.getStatus() == Status.SHARE_FAILED) {
				Log.w(TAG, "Unable to create share at this time");
				displayError("Unable to create share at this time");
				setCurrentState(SHARE_UPDATE_FAILED);
				return;
			}
			setCurrentState(SHARE_UPDATE_SUCCESS);
			mFSApp.setShortCode(shareResponse.getShortCode());
			mUI.showSuccess("share created");
			break;
		case SHARE_ACCESS:
			AccessShareResponse accessResponse = null;
			try {
				accessResponse = gson.fromJson(serverResponse, AccessShareResponse.class);
			} catch (Exception e) {
				Log.e(TAG, "Execption when parsing create share response: " + serverResponse, e);
			}
			if (accessResponse == null) {
				Log.w(TAG, "Unable to process response from server");
				displayError("Error processing server response");
				setCurrentState(SHARE_ACCESS_FAILED);
				return;
			}
			if (StringHelper.isEmpty(accessResponse.getShortCode())) {
				Log.w(TAG, "Share access did not return a short code");
				displayError("Share access did not return a short code");
				setCurrentState(SHARE_ACCESS_FAILED);
				return;
			}

			if (StringHelper.isEmpty(accessResponse.getShareContent())) {
				Log.w(TAG, "Share access did not return any content");
				displayError("Share access did not return any content");
				setCurrentState(SHARE_ACCESS_FAILED);
				return;
			}
			mFSApp.setShareToAccess(accessResponse);
			mFSApp.setShortCode(accessResponse.getShortCode());
			mUI.showSuccess("");
			break;
		case SAVE_SETTINGS_START:
			SaveSettingsResponse saveResponse = null;
			try {
				saveResponse = gson.fromJson(serverResponse, SaveSettingsResponse.class);
			} catch (Exception e) {
				Log.e(TAG, "Execption when parsing server start response: " + serverResponse, e);
			}
			if (saveResponse == null) {
				Log.w(TAG, "Unable to process response from server");
				displayError("Error processing server response");
				setCurrentState(SAVE_SETTINGS_FAILED);
				return;
			}
			// Check the status returned from the server
			if (saveResponse.getStatus() == Status.LE_AUTHENTICATED) {
				// no need to start liveensure, we have a valid session still
				// going on the server, go straight to success for the current
				// UI Activity
				Log.w(TAG, "save settings indicates LE authentication complete");
				mFSApp.setFsSessionToken(saveResponse.getFsSessionToken());
				setCurrentState(SAVE_SETTINGS_SUCCESS);
				mUI.handleStatusResult(SAVE_SETTINGS_SUCCESS);
			} else if (saveResponse.getStatus() == Status.ACTIVE) {
				// normal session start, move on to LE now
				Log.w(TAG, "Session start indicates LE authentication is required");
				setCurrentState(SAVE_SETTINGS_SUCCESS);
				mFSApp.setFsSessionToken(saveResponse.getFsSessionToken());
				mFSApp.setFsSessionExpiration(System.currentTimeMillis() + saveResponse.getFsSessionTTL());
				expDate = new Date(mFSApp.getFsSessionExpiration());
				Log.w(TAG, "calculated expiration time in ms as " + expDate.toString());
				mFSApp.setLESessionToken(saveResponse.getLeSessionToken());
				mFSApp.setLEIdentityServer(saveResponse.getIdentityServer());
				mUI.launchLiveEnsure();
			} else {
				// not in a valid session, and the server is unable to start an
				// LE session for some reason. error out
				Log.w(TAG, "Session start did not return an LE session");
				displayError("Unable to start session, please try again");
				setCurrentState(SAVE_SETTINGS_FAILED);
			}
			break;
		}
	}

	/**
	 * handler for failed (non-200) network requests - see @createNetworkRequestErrorListener
	 * 
	 * @param serverResponse
	 */
	private void handleError(String serverResponse) {
		if (StringHelper.isEmpty(serverResponse)) {
			Log.w(TAG, "server error response was null, cannot process response");
			displayError(mFSApp.getApplicationContext().getResources().getString(R.string.session_error));
			return;
		}
		// let the state determine our next step
		Log.i(TAG, "handleError(), mCurrentState is " + getCurrentState() + "\n response body:\n" + serverResponse);
		LiveResponse errResp = null;
		if (serverResponse.matches("(?i).*upgrade.*")) {
			// special case for a server response that indicates an upgrade to
			// the client is required
			try {
				errResp = gson.fromJson(serverResponse, LiveResponse.class);
			} catch (Exception e) {
				Log.e(TAG, "Exception generated when parsing error response from server: " + e.getMessage());
				mUI.handleError(mFSApp.getString(R.string.comms_error));
				return;
			}
			if (errResp != null) {
				mUI.showUpgrade(errResp.getStatusMessage());
			} else {
				mUI.handleError(mFSApp.getString(R.string.comms_error));
			}
			return;
		}

		switch (getCurrentState()) {
		case DEVICE_SIGNUP_START:
			setCurrentState(DEVICE_SIGNUP_FAILED);
			try {
				errResp = gson.fromJson(serverResponse, LiveResponse.class);
			} catch (Exception e) {
				Log.e(TAG, "Exception generated when parsing error response from server: " + e.getMessage());
				mUI.handleError(mFSApp.getString(R.string.comms_error));
				return;
			}
			if (errResp == null)
				return;
			Log.w(TAG, "error response status message is: " + errResp.getStatusMessage());
			mFSApp.setCurrentErrorMessage(mFSApp.getString(R.string.unable_to_register_device));
			mUI.handleError(errResp.getStatusMessage());
			break;
		case SESSION_START:
			setCurrentState(SESSION_FAILED);
			mFSApp.resetFsSession();
			try {
				errResp = gson.fromJson(serverResponse, LiveResponse.class);
			} catch (Exception e) {
				Log.e(TAG, "Exception generated when parsing error response from server: " + e.getMessage());
				mUI.handleError(mFSApp.getString(R.string.comms_error));
				return;
			}
			if (errResp == null)
				return;
			mFSApp.setCurrentErrorMessage("Unable to start session");
			Log.w(TAG, "error response status message is: " + errResp.getStatusMessage());
			mUI.handleError(errResp.getStatusMessage());
			break;
		case SESSION_STATUS:
			mStatusErrors++;
			Log.e(TAG, "status call error (internal), consecutive errors: " + mStatusErrors);
			if (mStatusErrors < 3) {
				callStatus();
				return;
			}
			setCurrentState(SESSION_STATUS_FAILED);
			mFSApp.resetFsSession();
			try {
				errResp = gson.fromJson(serverResponse, LiveResponse.class);
			} catch (Exception e) {
				Log.e(TAG, "Exception generated when parsing error response from server: " + e.getMessage());
				mUI.handleError(mFSApp.getString(R.string.comms_error));
				return;
			}
			if (errResp == null)
				return;
			mFSApp.setCurrentErrorMessage("Unable to start session");
			Log.w(TAG, "error response status message is: " + errResp.getStatusMessage());
			mUI.handleError(errResp.getStatusMessage());
			break;
		case SHARE_UPDATE:
			setCurrentState(SHARE_UPDATE_FAILED);
			try {
				errResp = gson.fromJson(serverResponse, LiveResponse.class);
			} catch (Exception e) {
				Log.e(TAG, "Exception generated when parsing error response from server: " + e.getMessage());
				mUI.handleError(mFSApp.getString(R.string.comms_error));
				return;
			}
			if (errResp == null)
				return;
			Log.w(TAG, "error response status message is: " + errResp.getStatusMessage());
			mFSApp.setCurrentErrorMessage("Unable to edit share");
			mUI.handleError(errResp.getStatusMessage());
			break;
		case SAVE_SETTINGS_START:
			setCurrentState(SAVE_SETTINGS_FAILED);
			mFSApp.resetFsSession();
			try {
				errResp = gson.fromJson(serverResponse, LiveResponse.class);
			} catch (Exception e) {
				Log.e(TAG, "Exception generated when parsing error response from server: " + e.getMessage());
				mUI.handleError(mFSApp.getString(R.string.comms_error));
				return;
			}
			if (errResp == null)
				return;
			mFSApp.setCurrentErrorMessage(mFSApp.getString(R.string.unable_to_register_device));
			Log.w(TAG, "error response status message is: " + errResp.getStatusMessage());
			mUI.handleError(errResp.getStatusMessage());
			break;
		case SHARE_ACCESS:
			setCurrentState(SHARE_ACCESS_FAILED);
			try {
				errResp = gson.fromJson(serverResponse, LiveResponse.class);
			} catch (Exception e) {
				Log.e(TAG, "Exception generated when parsing error response from server: " + e.getMessage());
				mUI.handleError(mFSApp.getString(R.string.comms_error));
				return;
			}
			if (errResp == null)
				return;
			Log.w(TAG, "error response status message is: " + errResp.getStatusMessage());
			mFSApp.setCurrentErrorMessage("Unable to access share");
			mUI.handleError(errResp.getStatusMessage());
			break;
		}
	}

	public boolean isConnecting() {
		return mConnecting;
	}

	public void setConnecting(boolean connecting) {
		this.mConnecting = connecting;
	}

	public UIForFiniteStateMachine getUI() {
		return mUI;
	}

	public void setUI(String uiTag, UIForFiniteStateMachine ui) {
		Log.e(TAG, "in setUI(), currently setting UI to " + uiTag);
		this.mUI = ui;
	}

	public void callSignup() {
		if (isConnecting()) {
			Log.w(TAG, "currently in an active network connection, skipping");
			return;
		}
		if (StringHelper.isEmpty(mFSApp.getUserName()) || mFSApp.getUserName().equals(FourSureApplication.DEFAULT_USER_NAME)) {
			Toast.makeText(mFSApp, "Set an ID first", Toast.LENGTH_SHORT).show();
			return;
		}
		JSONObject jo = new JSONObject();
		String deviceDetails = getDeviceName();
		if (StringHelper.isEmpty(deviceDetails))
			deviceDetails = "Unknown Device";
		try {
			jo.put("handleType", mFSApp.getUserIdType().ordinal());
			jo.put("handle", mFSApp.getUserName());
			jo.put("deviceType", "ANDROID");
			jo.put("deviceDetails", deviceDetails);
			jo.put("appVersion", mFSApp.getProgramVersion());
			jo.put("factorType", mFSApp.getUserFactorType());
			jo.put("factorDetails", mFSApp.getUserFactorDetails());
			setCurrentState(DEVICE_SIGNUP_START);
			mStatusErrors = 0;
			makeCallTo("/session/signup", jo.toString(), "POST");
		} catch (JSONException e1) {
			Log.e(TAG, "Exception building JSON object: " + e1.getMessage());
		}
	}

	public void callSaveSettings() {
		if (isConnecting()) {
			Log.w(TAG, "currently in an active network connection, skipping");
			return;
		}
		if (StringHelper.isEmpty(mFSApp.getUserName()) || mFSApp.getUserName().equals(FourSureApplication.DEFAULT_USER_NAME)) {
			Toast.makeText(mFSApp, "Set an ID first", Toast.LENGTH_SHORT).show();
			return;
		}
		if (StringHelper.isEmpty(mFSApp.getFsSessionToken()) || StringHelper.isEmpty(mFSApp.getDeviceUUID())) {
			Toast.makeText(mFSApp, "Cannot update, cancel and try again", Toast.LENGTH_SHORT).show();
			return;
		}
		JSONObject jo = new JSONObject();
		try {
			jo.put("fsSessionToken", mFSApp.getFsSessionToken());
			jo.put("handleType", mFSApp.getUserIdType().ordinal());
			jo.put("handle", mFSApp.getUserName());
			jo.put("factorType", mFSApp.getUserFactorType());
			jo.put("factorDetails", mFSApp.getUserFactorDetails());
			setCurrentState(SAVE_SETTINGS_START);
			mStatusErrors = 0;
			makeCallTo("/session/saveSettings", jo.toString(), "POST");
		} catch (JSONException e1) {
			Log.e(TAG, "Exception building JSON object: " + e1.getMessage());
		}
	}

	public void callStart(String shortCode) {
		if (isConnecting()) {
			Log.w(TAG, "currently in an active network connection, skipping");
			return;
		}
		if (mFSApp.getUserName().equals(FourSureApplication.DEFAULT_USER_NAME) || StringHelper.isEmpty(mFSApp.getDeviceUUID())) {
			Toast.makeText(mFSApp, R.string.please_register_first, Toast.LENGTH_SHORT).show();
			mUI.handleStatusResult(NO_SETTINGS_AVAILABLE);
			return;
		}
		mFSApp.setActivatedShieldValue(""); // default to no shield on new share
		JSONObject jo = new JSONObject();
		try {
			jo.put("deviceUuid", mFSApp.getDeviceUUID());
			if (!StringHelper.isEmpty(shortCode)) {
				jo.put("shortCode", shortCode);
			}
			if (!StringHelper.isEmpty(mFSApp.getFsSessionToken())) {
				jo.put("fsSessionToken", mFSApp.getFsSessionToken());
			}
			setCurrentState(SESSION_START);
			mStatusErrors = 0;
			makeCallTo("/session/start", jo.toString(), "POST");
		} catch (JSONException e1) {
			Log.e(TAG, "Exception building JSON object: " + e1.getMessage());
		}
	}

	public void callStatus() {
		if (StringHelper.isEmpty(mFSApp.getFsSessionToken())) {
			Log.e(TAG, "in callStatus(), but FS session token is empty. Not calling status.");
			return;
		}
		JSONObject jo = new JSONObject();
		try {
			jo.put("fsSessionToken", mFSApp.getFsSessionToken());
			setCurrentState(SESSION_STATUS);
			makeCallTo("/session/status", jo.toString(), "POST");
		} catch (JSONException e1) {
			Log.e(TAG, "Exception building JSON object: " + e1.getMessage());
		}
	}

	public void callShareUpdate(boolean delete) {
		if (isConnecting()) {
			Log.w(TAG, "currently in an active network connection, skipping");
			return;
		}
		if (StringHelper.isEmpty(mFSApp.getActivatedShareAsset())) {
			Toast.makeText(mFSApp, "Set a share value first", Toast.LENGTH_SHORT).show();
			return;
		}
		JSONObject jo = new JSONObject();
		try {
			if (!StringHelper.isEmpty(mFSApp.getShortCode())) {
				jo.put("shortCode", mFSApp.getShortCode());
				jo.put("status", (delete) ? Status.INACTIVE.name() : Status.ACTIVE.name());
			}
			jo.put("deviceUuid", mFSApp.getDeviceUUID());
			jo.put("fsSessionToken", mFSApp.getFsSessionToken());
			jo.put("type", mFSApp.getActivatedShareAssetType());
			jo.put("content", mFSApp.getActivatedShareAsset());
			jo.put("accessMax", 0);
			jo.put("authorNotify", mFSApp.isShareOptionTracking());
			jo.put("factorType", mFSApp.getShareFactorType());
			jo.put("factorDetails", mFSApp.getShareFactorDetails());
			jo.put("shieldType", StringHelper.isEmpty(mFSApp.getActivatedShieldValue()) ? FactorType.NONE.name() : ShareType.URL.name());
			jo.put("shieldContent", mFSApp.getActivatedShieldValue());
			setCurrentState(SHARE_UPDATE);
			makeCallTo("/share", jo.toString(), "PUT");
		} catch (JSONException e1) {
			Log.e(TAG, "Exception building JSON object: " + e1.getMessage());
		}
	}

	public void callAccessShare() {
		if (isConnecting()) {
			Log.w(TAG, "currently in an active network connection, skipping");
			return;
		}
		if (StringHelper.isEmpty(mFSApp.getShortCode())) {
			Toast.makeText(mFSApp, "No asset code available", Toast.LENGTH_SHORT).show();
			return;
		}
		JSONObject jo = new JSONObject();
		try {
			jo.put("deviceUuid", mFSApp.getDeviceUUID());
			jo.put("fsSessionToken", mFSApp.getFsSessionToken());
			jo.put("shortCode", mFSApp.getShortCode());
			setCurrentState(SHARE_ACCESS);
			makeCallTo("/share/access", jo.toString(), "POST");
		} catch (JSONException e1) {
			Log.e(TAG, "Exception building JSON object: " + e1.getMessage());
		}
	}

	public String getDefaultHost() {
		return mDefaultHost;
	}

	public void setDefaultHost(String defaultHost) {
		this.mDefaultHost = defaultHost;
	}

	public int getCurrentState() {
		return mCurrentState;
	}

	private void setCurrentState(int mCurrentState) {
		this.mCurrentState = mCurrentState;
	}

	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		}
		return capitalize(manufacturer) + " " + model;
	}

	private static String capitalize(String str) {
		if (TextUtils.isEmpty(str)) {
			return str;
		}
		char[] arr = str.toCharArray();
		boolean capitalizeNext = true;
		String phrase = "";
		for (char c : arr) {
			if (capitalizeNext && Character.isLetter(c)) {
				phrase += Character.toUpperCase(c);
				capitalizeNext = false;
				continue;
			} else if (Character.isWhitespace(c)) {
				capitalizeNext = true;
			}
			phrase += c;
		}
		return phrase;
	}

}
