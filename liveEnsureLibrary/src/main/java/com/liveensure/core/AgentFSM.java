package com.liveensure.core;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.os.Handler;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liveensure.a.mini.LEApplication;
import com.liveensure.a.mini.R;
import com.liveensure.a.mini.UIForFiniteStateMachine;
import com.liveensure.intrinsic.IntrinsicClient;
import com.liveensure.pebble.PebbleClient;
import com.liveensure.rest.api.AgentChallengeResponse;
import com.liveensure.rest.api.AgentRegistrationResponse;
import com.liveensure.rest.api.AgentSessionInitialResponse;
import com.liveensure.shuffler.AndroidShuffler;
import com.liveensure.shuffler.HardwareUIDBase;
import com.liveensure.util.Log;
import com.liveensure.util.MessageDigester;
import com.liveensure.util.StringHelper;

/**
 * 
 * @author aapel
 * 
 *         Finite State Machine representation of the LiveEnsure session state. Allows a simple view of the possible branches of logic and behavior based on the current state of the authentication
 *         session.
 */
public class AgentFSM implements Serializable {

	private static final long serialVersionUID = 883299968382331651L;
	private IDSession mSession;
	private int currentState;
	private boolean connecting;
	private boolean ready;
	private boolean chaining; // used for a series of network connection that have no UI
	private String apiBase;
	private HashMap<String, String> responseData;
	private int responseCode;

	private boolean firstLaunch;
	private boolean active;
	private boolean succeeded;
	private boolean failed;

	private boolean manageAllowed;
	private String manageToken;

	private String thirdPartyInfo;
	private final String TAG = AgentFSM.class.getSimpleName();
	private transient Gson gson = new Gson();
	private String mDefaultHost = "localhost";
	private LEApplication mLeApp;
	private HardwareUIDBase mShuffler;
	private UIForFiniteStateMachine ui;
	private AgentChallengeResponse mCurrentChallenge;

	private static final int RETRIEVE_DECISION = 1;
	private static final int RETRIEVE_CHALLENGE = 2;
	private static final int ANSWER_CHALLENGE = 3;
	private static final int RETRIEVE_REGISTRATION = 4;
	private static final int ANSWER_REGISTRATION = 5;

	private static final int DELETE_DEVICES = 12;
	private static final int CLOSE_AGENT = 13;

	private static final int CHALLENGE_DONE = 1;
	private static final int CHALLENGE_RETRY = 2;
	private static final int CHALLENGE_MORE = 3;
	private static final int CHALLENGE_FAILED = 4;
	private static final String ERROR_CHALLENGE = "Invalid Challenge";
	private static final String ERROR_NETWORK = "Network Error";
	public static final String CHALLENGE_NAME_AGENT_BEHAVIOR = "AGENT_BEHAVIOR";
	public static final String CHALLENGE_NAME_HOST_BEHAVIOR = "HOST_BEHAVIOR";
	public static final String CHALLENGE_NAME_PIN = "PIN";
	public static final String CHALLENGE_NAME_PROMPT = "PROMPT";
	public static final String CHALLENGE_NAME_LAT_LONG = "LAT_LONG";
	public static final String CHALLENGE_NAME_HOME = "HOME";
	public static final String CHALLENGE_NAME_INTRINSIC = "INTRINSIC";
	public static final String CHALLENGE_NAME_PEBBLE = "PEBBLE";
	public static final String CHALLENGE_ANSWER_UNAVAILABLE = "unavailable";
	public static final long SHOW_BUSY_CUES_DELAY_MS = 10;

	private int mOrientationAtCapture;
	private boolean[] mTouchRegionsAtCapture;
	private boolean mPebbleChallengeAnswered;
	private Handler mBusyCueHandler;
	private Runnable mBusyCueTask;

	/**
	 * constructor
	 */
	public AgentFSM(LEApplication leApp) {
		// String intrinsicKey = INTRINSIC_KEY_PART_1 + INTRINSIC_KEY_PART_2;
		firstLaunch = true;
		manageAllowed = false;
		this.mLeApp = leApp;
		resetAgent();
		mShuffler = new AndroidShuffler(leApp.getApplicationContext());
		// collect data points during construction - then we don't need to do it later
		mShuffler.getAllHardwareIDs();

		Log.w(TAG, "now creating Pebble objects");
		mPebbleChallengeAnswered = false;

		mTouchRegionsAtCapture = new boolean[LEApplication.NUM_TOUCH_REGIONS];
		chaining = false;
		mBusyCueHandler = new Handler();
		mBusyCueTask = new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "busy cue task was started: " + isConnecting());
				ui.showBusyCues(isConnecting());
			}
		};
	}

	public void resetAgent() {
		if (mSession != null && !StringHelper.isEmpty(mSession.getSessionToken())) {
			makeCloseAgentCall();
		} else {
			currentState = 0;
			active = false;
		}
		mSession = null;
		mCurrentChallenge = null;
		connecting = false;
		ready = false;
		succeeded = false;
		failed = false;
		manageAllowed = false;
		mPebbleChallengeAnswered = false;
		chaining = false;
	}

	private void displayError(String message) {
		Log.e(TAG, message);
		ui.showBusyCues(false);
		ui.showFailure();
	}

	public IDSession getSession() {
		return mSession;
	}

	public void setupSession(String rawToken) {
		String stoken = rawToken;
		String url = rawToken;
		mSession = new IDSession(url, stoken, "unknown");
	}

	public void setupSession(String token, String host, String agentLaunchType) {
		mSession = new IDSession(host, token, agentLaunchType);
		apiBase = host;
	}

	public void buildSession(AgentSessionInitialResponse resp) {
		mSession.setSessionToken(resp.getSessionToken());
		mSession.setSuccess(false);
		mSession.setSessionTimeLeftToLive(resp.getSessionTimeLeftToLive());
		mSession.setInitialMessage(resp.getInitialMessage());
		mSession.setInitialStatus(resp.getInitialStatus());
		if ("pass".equalsIgnoreCase(mSession.getInitialStatus())) {
			mSession.setCompleted(false);
			succeeded = false;
			failed = false;
			active = true;
		} else {
			// Yikes! Initial request was rejected
			Log.w(TAG, "Failed initial devcie check in: " + mSession.getInitialMessage());
			mSession.setCompleted(true);
			// ui.clearSavedSessionState();
			succeeded = false;
			failed = true;
			active = false;
			if (StringHelper.isEmpty(mSession.getInitialMessage())) {
				mSession.setInitialMessage(mLeApp.getApplicationContext().getString(R.string.session_initial_message));
			}
		}
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

		if (apiBase == null || StringHelper.isEmpty(apiBase)) {
			apiBase = mDefaultHost;
			Log.w(TAG, "Using default API base of " + apiBase + " because of absent apiBase");
		}
		String fullUrl = apiBase + resourceURL;
		Log.e(TAG, httpMethod + " call to " + fullUrl);
		RequestQueue queue = mLeApp.getRequestQueue();
		Request<String> req = null;
		if ("GET".equals(httpMethod)) {
			req = new StringRequest(Request.Method.GET, fullUrl, createNetworkRequestSuccessListener(), createNetworkRequestErrorListener()) {
				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					Map<String, String> params = new HashMap<String, String>();
					params.put("Accept", "application/json");
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
					return params;
				}
			};
		}
		if ("PUT".equals(httpMethod)) {
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
			};
		}
		if ("POST".equals(httpMethod)) {
			Log.e(TAG, "Request body: " + reqBody);
			req = new StringRequest(Request.Method.POST, fullUrl, createNetworkRequestSuccessListener(), createNetworkRequestErrorListener()) {
				@Override
				public byte[] getBody() {
					return reqBody.getBytes();
				}

				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					Map<String, String> params = new HashMap<String, String>();
					// params.put("Content-Type", "application/json");
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
				if (!chaining) {
					ui.showBusyCues(false);
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
				if (!chaining) {
					ui.showBusyCues(false);
				}
				// TODO: gracefully handle error;
				Log.e("The Volley ErrorListener callback was fired, something went wrong with a network request", error.toString());
			}
		};
	}

	private void handleResponse(String serverResponse) {
		if (StringHelper.isEmpty(serverResponse)) {
			Log.w(TAG, "server response was null, cannot process response");
			displayError(mLeApp.getApplicationContext().getResources().getString(R.string.session_error));
			return;
		}
		if (ERROR_NETWORK.equals(serverResponse)) {
			Log.w(TAG, "network call returned an error");
			// special case for agent close network call - we can ignore this one and start a new session
			if (currentState == CLOSE_AGENT) {
				Log.w(TAG, "network error ignored in the case of CLOSE_AGENT");
				// ui.startNewSession();
				return;
			}
			displayError(mLeApp.getApplicationContext().getResources().getString(R.string.connect_error));
			return;
		}
		// we made it this far, we should have a 200 response with a non-null entity body
		// let the state determine our next step
		Log.d(TAG, "handleResponse(), currentState is " + currentState);
		switch (currentState) {
		case RETRIEVE_DECISION:
			// JSON response body should be an AgentSessionInitialResponse
			// Sanity check here, make sure beginAuth succeeded and we can
			// continue with the session.
			/*
			 * LELogger.i(TAG, "SessionTimeLeftToLive: " + getIDSession().getSessionTimeLeftToLive());
			 * 
			 * // save credentials to device in case user leaves app to multi-task. // We can restore their session later. saveSessionCredentials();
			 */
			AgentSessionInitialResponse intialResponse = gson.fromJson(serverResponse, AgentSessionInitialResponse.class);
			buildSession(intialResponse);
			if (active) {
				// OK, session setup succeeded so go find any incoming challenges.
				makeRetrieveChallengeCall();
			} else {
				// If it failed, forward the ui to a failure screen.
				displayError(mSession.getInitialMessage());
			}
			break;
		case RETRIEVE_CHALLENGE:
			AgentChallengeResponse retrieveChallengeResponse = gson.fromJson(serverResponse, AgentChallengeResponse.class);
			handleChallenge(retrieveChallengeResponse);
			break;
		case ANSWER_CHALLENGE:
			AgentChallengeResponse answerChallengeResponse = gson.fromJson(serverResponse, AgentChallengeResponse.class);
			int finishResult = finishChallenge(answerChallengeResponse);
			if (finishResult == CHALLENGE_RETRY) {
				ui.showBusyCues(false);
			} else if (finishResult == CHALLENGE_MORE) {
				Log.d(TAG, "challenge complete, at least one more challenge to handle");
				handleChallenge(answerChallengeResponse);
			} else if (finishResult == CHALLENGE_DONE) {
				Log.d(TAG, "challenge complete, checking for registrations");
				if (!StringHelper.isEmpty(answerChallengeResponse.getRegistrationState()) && "REQUIRED".equals(answerChallengeResponse.getRegistrationState())) {
					makeRetrieveRegistrationCall();
				} else {
					// only show the actual success screen (and potentially leave the app) if there are no pending registrations
					String message = "Authenticated"; // default
					if (!StringHelper.isEmpty(answerChallengeResponse.getStatusMessage())) {
						message = answerChallengeResponse.getStatusMessage();
					}
					ui.showBusyCues(false);
					ui.showSuccess(message);
					Log.d(TAG, "registration state " + answerChallengeResponse.getRegistrationState() + ", no need to get registration: ");
				}
			} else if (finishResult == CHALLENGE_FAILED) {
				displayError("challenge failed");
			} else {
				displayError("Unexpected finishChallenge result, cannot continue: " + finishResult);
			}
			break;
		case RETRIEVE_REGISTRATION:
			AgentRegistrationResponse retrieveRegistrationResponse = gson.fromJson(serverResponse, AgentRegistrationResponse.class);
			int regID = retrieveRegistrationResponse.getRegistrationID();
			String cType = retrieveRegistrationResponse.getChallengeType();
			if (!StringHelper.isEmpty(cType) && "TEMPLATE".equals(cType)) {
				HashMap<String, String> details = mShuffler.getAnswersAsHashMap();
				makeAnswerRegistrationCall(regID, cType, details);
			}
			if (!StringHelper.isEmpty(cType) && CHALLENGE_NAME_PEBBLE.equals(cType)) {
				HashMap<String, String> details = new HashMap<String, String>();
				PebbleClient pebbleClient = new PebbleClient(mLeApp);
				details = pebbleClient.getRegistrationInfoAsHashMap();
				makeAnswerRegistrationCall(regID, cType, details);
			}
			break;
		case ANSWER_REGISTRATION:
			AgentRegistrationResponse answerRegistrationResponse = gson.fromJson(serverResponse, AgentRegistrationResponse.class);
			if (finishRegistration(answerRegistrationResponse)) {
				makeRetrieveRegistrationCall();
			} else {
				String message = "Authenticated"; // default
				ui.showBusyCues(false);
				ui.showSuccess(message);
			}
			break;
		case DELETE_DEVICES:
			Toast.makeText(mLeApp.getApplicationContext(), mLeApp.getApplicationContext().getResources().getString(R.string.reset_dev_button_text), Toast.LENGTH_SHORT).show();
			break;
		case CLOSE_AGENT:
			Log.w(TAG, "handling response of close agent, done with this session");
			currentState = 0;
			active = false;
			break;
		default:
			Log.w(TAG, "Unknown currentState: " + currentState);
		}

	}

	private void handleChallenge(AgentChallengeResponse resp) {
		if (resp == null) {
			Log.e(TAG, "Received a null challenge response, cannot continue");
			displayError(ERROR_CHALLENGE);
			return;
		}
		String challengeType = resp.getChallengeType();
		if (StringHelper.isEmpty(challengeType)) {
			Log.e(TAG, "Received a challenge response with no challenge Type, cannot continue");
			displayError(ERROR_CHALLENGE);
			return;
		}
		Log.d(TAG, "Working with challenge type " + challengeType);

		// Get the challenge ID, lovingly packed in the challengeDetails hashmap
		int challengeID = getChallengeIDFromDetails(resp);
		if (challengeID == 0) {
			Log.e(TAG, "challengeDetails had no challengeID");
			displayError(ERROR_CHALLENGE);
			return;
		}
		Log.d(TAG, "Setting challenge ID to " + challengeID);
		resp.setChallengeID(challengeID);

		if (CHALLENGE_NAME_PROMPT.equals(challengeType) || CHALLENGE_NAME_PIN.equals(challengeType) || CHALLENGE_NAME_LAT_LONG.equals(challengeType)
				|| CHALLENGE_NAME_HOME.equals(challengeType)) {
			// we have to do a bit of extraction here since the shape of challengeDetails is not consistent,
			// and gson's deserializer kind of gets in the way.
			// the "challengeDetails" field contains an array of Challenge objects
			HashMap<String, Object> chalDetails = resp.getChallengeDetails();
			String prompt = null;
			for (Object k : chalDetails.keySet()) {
				Log.d(TAG, "key: " + k + " => " + chalDetails.get(k));
				if (k instanceof String) {
					String s = (String) k;
					if ("prompt".equals(s)) {
						prompt = String.valueOf(chalDetails.get(k));
					}
					if ("answerState".equals(s)) {
						// answerState = String.valueOf(chalDetails.get(k));
					}
					if ("registrationState".equals(s)) {
						// registrationState = String.valueOf(chalDetails.get(k));
					}
				}
			}
			// after that extraction, we hopefully have a prompt
			if (CHALLENGE_NAME_PROMPT.equals(challengeType) || CHALLENGE_NAME_PIN.equals(challengeType)) {
				if (StringHelper.isEmpty(prompt)) {
					Log.e(TAG, "challengeDetails had no prompt");
					displayError(ERROR_CHALLENGE);
					return;
				}
				Log.d(TAG, "Got a prompt challenge: " + prompt);
			}
			// Strip any chars from decimal point to end (thanks gson)
			mCurrentChallenge = resp;
			ui.showBusyCues(false);
			ui.showChallenge(resp);
		} else if ("TEMPLATE".equals(challengeType)) {
			// no UI for this challenge
			// the "challengeDetails" field contains an array of Challenge objects
			HashMap<String, Object> chalDetails = resp.getChallengeDetails();
			if (chalDetails == null) {
				Log.e(TAG, "Challenge had no details");
				displayError(ERROR_CHALLENGE);
				return;
			}
			// we have to do a bit of extraction here since the shape of challengeDetails is not consistent,
			// and gson's deserializer kind of gets in the way.
			List<Question> questions = null;
			for (Object k : chalDetails.keySet()) {
				Log.d(TAG, "key: " + k + " => " + chalDetails.get(k));
				if (k instanceof String) {
					String s = (String) k;
					if ("challenges".equals(s)) {
						String challengesJson = String.valueOf(chalDetails.get(k));
						if (challengesJson != null) {
							Type listType = new TypeToken<List<Question>>() {
							}.getType();
							questions = gson.fromJson(challengesJson, listType);
						}
					}
				}
			}
			// after that extraction, we hopefully have a list of questions
			if (questions == null || questions.size() == 0) {
				Log.e(TAG, "challengeDetails had no questions");
				displayError(ERROR_CHALLENGE);
				return;
			}
			String templateAnswer = "";
			for (Question q : questions) {
				Log.d(TAG, "question: " + q.getQuestion() + ", begin: " + q.getBegin() + ", end: " + q.getEnd());
				templateAnswer += StringHelper.guaranteedSubstring(mShuffler.getHardwareID(q.getQuestion()).trim().toLowerCase(Locale.US), q.getBegin(), q.getEnd());
			}
			Log.d(TAG, "Final answer from shuffler: " + templateAnswer);
			String finalAnswer = MessageDigester.getHash(templateAnswer);
			mCurrentChallenge = resp;
			makeAnswerChallengeCallWithID(challengeID, finalAnswer, "1.0");
		} else if (CHALLENGE_NAME_AGENT_BEHAVIOR.equals(challengeType) || AgentFSM.CHALLENGE_NAME_HOST_BEHAVIOR.equals(challengeType)) {
			HashMap<String, Object> chalDetails = resp.getChallengeDetails();
			String regionCount = null;
			for (Object k : chalDetails.keySet()) {
				Log.d(TAG, "key: " + k + " => " + chalDetails.get(k));
				if (k instanceof String) {
					String s = (String) k;
					if ("regionCount".equals(s)) {
						regionCount = String.valueOf(chalDetails.get(k));
					}
				}
			}
			Log.i(TAG, "using a region count of " + regionCount);
			// Answer challenge. for behavior, there are two possible ways to get an answer:
			// 1 - QR was scanned, and the behavior at capture time is relevant, or
			// 2 - rollover, in which case we present the behavior challenge sweep timer
			if (mSession.getLaunchType().equals("QR")) {
				String orientation = String.valueOf(mOrientationAtCapture);
				String touches = "";
				for (int x = 0; x < mTouchRegionsAtCapture.length; x++) {
					if (mTouchRegionsAtCapture[x]) {
						if (touches.length() > 0)
							touches = touches + "," + String.valueOf(x + 1);
						else
							touches = String.valueOf(x + 1);
					}
				}
				if (touches.length() == 0)
					touches = "0"; // no touches at all == "0"

				String plainAnswer = orientation + "::" + touches;
				Log.w(TAG, "plaintext answer for behavior challenge: " + plainAnswer);
				String hashedAnswer = StringHelper.obscure(getSession().getSessionToken(), plainAnswer);
				mCurrentChallenge = resp;
				makeAnswerChallengeCallWithID(challengeID, hashedAnswer, "2.0");
			} else {
				// show sweep timer challenge
				ui.showChallenge(resp);
			}
		} else if (CHALLENGE_NAME_PEBBLE.equals(challengeType)) {
			Log.i(TAG, "pebble auth challenge presented by stack");
			mCurrentChallenge = resp;
			ui.showChallenge(resp);
		} else {
			// the stack presented us with a challenge type that we don't know about. response with "unavailable" and let the stack decide what to do next
			Log.e(TAG, "Unknown challenge type: " + challengeType);
			makeAnswerChallengeCallWithID(challengeID, CHALLENGE_ANSWER_UNAVAILABLE, "1.0");
			displayError(ERROR_CHALLENGE);
			return;
		}
	}

	private int getChallengeIDFromDetails(AgentChallengeResponse resp) {
		HashMap<String, Object> chalDetails = resp.getChallengeDetails();
		if (chalDetails == null)
			return 0;
		String challengeID = null;
		for (Object k : chalDetails.keySet()) {
			if (k instanceof String) {
				String s = (String) k;
				if ("challengeID".equals(s))
					challengeID = String.valueOf(chalDetails.get(k));
			}
		}
		// after that extraction, we hopefully have a challengeID
		if (StringHelper.isEmpty(challengeID)) {
			Log.e(TAG, "challengeDetails had no challengeID");
			return 0;
		}
		// Strip any chars from decimal point to end (thanks gson)
		challengeID = challengeID.replaceAll("\\..*$", "");
		int cid = Integer.valueOf(challengeID);
		// challenge types requiring UI
		return cid;
	}

	public int getCurrentState() {
		return currentState;
	}

	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}

	public boolean isConnecting() {
		return connecting;
	}

	public void setConnecting(boolean connecting) {
		this.connecting = connecting;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public String getApiBase() {
		return apiBase;
	}

	public void setApiBase(String apiBase) {
		this.apiBase = apiBase;
	}

	public HashMap<String, String> getResponseData() {
		return responseData;
	}

	public void setResponseData(HashMap<String, String> responseData) {
		this.responseData = responseData;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public boolean isFirstLaunch() {
		return firstLaunch;
	}

	public void setFirstLaunch(boolean firstLaunch) {
		this.firstLaunch = firstLaunch;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isSucceeded() {
		return succeeded;
	}

	public void setSucceeded(boolean succeeded) {
		this.succeeded = succeeded;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}

	public boolean isManageAllowed() {
		return manageAllowed;
	}

	public void setManageAllowed(boolean manageAllowed) {
		this.manageAllowed = manageAllowed;
	}

	public String getManageToken() {
		return manageToken;
	}

	public void setManageToken(String manageToken) {
		this.manageToken = manageToken;
	}

	public String getThirdPartyInfo() {
		return thirdPartyInfo;
	}

	public void setThirdPartyInfo(String thirdPartyInfo) {
		this.thirdPartyInfo = thirdPartyInfo;
	}

	public void fakeStartCall() {
	}

	public void makeRetrieveDecisionCall() {
		Log.e(TAG, "in makeRetrieveDecisionCall()");
		HashMap<String, String> reqBody = new HashMap<String, String>();
		reqBody.put("sessionToken", mSession.getSessionToken());
		reqBody.put("deviceFingerprint", mShuffler.getFullUID());
		reqBody.put("agentVersion", mLeApp.getProgramVersion());
		reqBody.put("agentType", "5");
		reqBody.put("agentLaunch", mSession.getLaunchType());
		String countryFromLocale = Locale.getDefault().getCountry();
		if (StringHelper.isEmpty(countryFromLocale))
			countryFromLocale = "us";
		reqBody.put("country", countryFromLocale);
		String jsonBody = gson.toJson(reqBody);
		Log.e(TAG, "retrieve decision body: " + jsonBody);
		currentState = RETRIEVE_DECISION;
		chaining = true;
		makeCallTo("/agent/session/device", jsonBody, "POST");
	}

	public void makeRetrieveChallengeCall() {
		if (mSession != null) {
			currentState = RETRIEVE_CHALLENGE;
			chaining = true;
			Log.d(TAG, "ready to GET from /agent/challenge/" + mSession.getSessionToken());
			makeCallTo("/agent/challenge/" + mSession.getSessionToken(), null, "GET");
		} else {
			if (ui != null)
				ui.showError("");
			Log.e(TAG, "in makeRetrieveChallengeCall() but session is null");
		}
	}

	public void makeRetrieveRegistrationCall() {
		if (mSession == null) {
			Log.e(TAG, "makeRetrieveRegistrationCall() invoked, but session object is null");
			if (ui != null)
				ui.showError("");
			return;
		}
		currentState = RETRIEVE_REGISTRATION;
		makeCallTo("/agent/registration/" + mSession.getSessionToken(), null, "GET");
	}

	public void makeAnswerChallengeCallWithID(int challengeID, String answer, String version) {
		if (challengeID == 0) {
			Log.e(TAG, "Attempting to answer a challenge but no challengeID provided");
			if (ui != null)
				ui.showError("");
			return;
		}
		if (StringHelper.isEmpty(answer)) {
			Log.e(TAG, "Attempting to answer challengeID " + challengeID + " but no answer provided");
			if (ui != null)
				ui.showError("");
			return;
		}
		if (StringHelper.isEmpty(version)) {
			Log.i(TAG, "no version supplied, setting challenge vesion to 1.0");
			version = "1.0";
		}
		HashMap<String, String> reqBody = new HashMap<String, String>();
		reqBody.put("sessionToken", mSession.getSessionToken());
		reqBody.put("agentAnswer", answer);
		reqBody.put("challengeID", String.valueOf(challengeID));
		reqBody.put("managementDelay", "0");
		reqBody.put("version", version);
		String jsonBody = gson.toJson(reqBody);
		Log.d(TAG, "answer challenge body: " + jsonBody);
		currentState = ANSWER_CHALLENGE;
		// ui.showBusyCues(true);
		makeCallTo("/agent/challenge", jsonBody, "POST");
	}

	private int finishChallenge(AgentChallengeResponse resp) {
		// extract the challenge ID from the resp
		int challengeID = getChallengeIDFromDetails(resp);
		if (challengeID > 0) {
			resp.setChallengeID(challengeID);
		}
		chaining = false;
		mCurrentChallenge = resp;
		String moreChallenges = resp.getChallengeType();
		String answerState = resp.getAnswerState();
		if (!StringHelper.isEmpty(answerState) && "RETRY".equals(answerState)) {
			ui.retryCurrentChallenge();
			return CHALLENGE_RETRY;
		}
		if (!StringHelper.isEmpty(answerState) && "SUCCESS".equals(answerState)) {
			Log.d(TAG, "Challenge result is SUCCESS");
		}
		if (!StringHelper.isEmpty(moreChallenges) && "NONE".equals(moreChallenges)) {
			Log.d(TAG, "No more challenges.");
			mCurrentChallenge = null;
			if (!StringHelper.isEmpty(answerState) && "SUCCESS".equals(answerState)) {
				Log.i(TAG, "final session state is SUCCESS");
				mSession.setSuccess(true);
				succeeded = true;
				failed = false;
				manageAllowed = true;
				mSession.setCompleted(true);
				return CHALLENGE_DONE;
			} else {
				Log.w(TAG, "final session state is FAILED");
				failed = true;
				succeeded = false;
				manageAllowed = false;
				mSession.setCompleted(true);
				// ui.clearSavedSessionState();
				return CHALLENGE_FAILED;
			}

		}
		return CHALLENGE_MORE;
	}

	private boolean finishRegistration(AgentRegistrationResponse resp) {
		String moreRegistrations = resp.getChallengeType();
		chaining = false;
		if (!StringHelper.isEmpty(moreRegistrations) && "NONE".equals(moreRegistrations)) {
			Log.d(TAG, "No more registrations.");
			return false;
		}
		return true;
	}

	public void makeAnswerRegistrationCall(int regID, String regType, HashMap<String, String> details) {
		if (details == null || details.size() == 0) {
			Log.e(TAG, "in makeAnswerRegistrationCall(), no registration details provided, cannot continue.");
			return;
		}
		HashMap<String, Object> reqBody = new HashMap<String, Object>();
		reqBody.put("sessionToken", mSession.getSessionToken());
		reqBody.put("registrationID", String.valueOf(regID));
		reqBody.put("challengeType", regType);
		reqBody.put("registrationDetails", details);
		String jsonBody = gson.toJson(reqBody);
		currentState = ANSWER_REGISTRATION;
		makeCallTo("/agent/registration", jsonBody, "POST");
	}

	public void makeDeleteDevicesCall() {
		if (mSession != null) {
			currentState = DELETE_DEVICES;
			makeCallTo("/user/device/" + mSession.getSessionToken(), null, "DELETE");
		} else {
			Log.e(TAG, "attemping to make delete device call but session is null");
			if (ui != null)
				ui.showError("");
		}
	}

	public void makeCloseAgentCall() {
		Log.e(TAG, "in makeCloseAgentCall()");
		if (mSession != null && active) {
			HashMap<String, String> reqBody = new HashMap<String, String>();
			reqBody.put("sessionToken", mSession.getSessionToken());
			reqBody.put("sessionAction", "Agent_Closed");
			String jsonBody = gson.toJson(reqBody);
			currentState = CLOSE_AGENT;
			makeCallTo("/agent/session/state", jsonBody, "POST");
		}
	}

	public void setIDSession(IDSession s) {
		mSession = s;
	}

	public void resetSession() {
		// noop
	}

	public String requestQuickReset() {
		return null;
	}

	public String getDefaultHost() {
		return mDefaultHost;
	}

	public void setDefaultHost(String defaultHost) {
		this.mDefaultHost = defaultHost;
	}

	public void setUI(UIForFiniteStateMachine miniLiveEnsureActivity) {
		this.ui = miniLiveEnsureActivity;
	}

	/*
	 * private class NetworkIOTask extends AsyncTask<HttpRequestBase, Integer, String> {
	 * 
	 * final Handler handler = new Handler(); final Runnable busyCue = new Runnable() {
	 * 
	 * @Override public void run() { Log.d(TAG, "busy cue task was started: " + isConnecting()); ui.showBusyCues(isConnecting()); } };
	 * 
	 * @Override protected void onPreExecute() { Log.e(TAG, "in onPreExecute()"); if (ui == null) { Log.e(TAG, "ui is null"); return; } // update the UI setConnecting(true);
	 * handler.postDelayed(busyCue, SHOW_BUSY_CUES_DELAY_MS); }
	 * 
	 * @Override protected String doInBackground(HttpRequestBase... requests) { if (android.os.Debug.isDebuggerConnected()) android.os.Debug.waitForDebugger(); Log.e(TAG, "in doInBackground()"); if
	 * (requests == null || requests.length != 1) { return null; } HttpParams httpParameters = new BasicHttpParams(); // Set the timeout in milliseconds until a connection is established. // The
	 * default value is zero, that means the timeout is not used. int timeoutConnection = 30000; // 30 sec HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection); // Set the
	 * default socket timeout (SO_TIMEOUT) // in milliseconds which is the timeout for waiting for data. int timeoutSocket = 20000; // 20 sec HttpConnectionParams.setSoTimeout(httpParameters,
	 * timeoutSocket); DefaultHttpClient client = new DefaultHttpClient(httpParameters); HttpRequestBase req = requests[0]; HttpResponse response = null; String responseString = ""; try { response =
	 * client.execute(req); // check HTTP status of response StatusLine sLine = response.getStatusLine(); if (sLine == null) { Log.w(TAG,
	 * "server response had no status line, cannot process response"); return ERROR_NETWORK; } int httpStatusCode = sLine.getStatusCode(); if (httpStatusCode != 200) { Log.w(TAG,
	 * "server response HTTP status code was " + httpStatusCode + ", cannot process response"); return ERROR_NETWORK; } HttpEntity entity = response.getEntity(); if (entity != null) { InputStream is;
	 * try { is = entity.getContent(); BufferedReader reader = new BufferedReader(new InputStreamReader(is)); StringBuilder str = new StringBuilder(); String line = null; while ((line =
	 * reader.readLine()) != null) { str.append(line + "\n"); } is.close(); responseString = str.toString(); Log.d(TAG, "handling server response " + responseString); } catch (IllegalStateException e)
	 * { Log.e(TAG, "IllegalStateException when processing HTTP response body, cannot continue"); return ERROR_NETWORK; } catch (IOException e) { Log.e(TAG,
	 * "IOException when processing HTTP response body, cannot continue"); return ERROR_NETWORK; } } else { Log.w(TAG, "Hmm.  call returned a null entity response, cannot continue"); return
	 * ERROR_NETWORK; } } catch (ClientProtocolException e1) { Log.e(TAG, "ClientProtocolException during network call to " + req.getURI().toASCIIString()); return ERROR_NETWORK; } catch (IOException
	 * e1) { Log.e(TAG, "IOException during network call to " + req.getURI().toASCIIString()); return ERROR_NETWORK; } return responseString; }
	 * 
	 * @Override protected void onPostExecute(final String serverResponse) { Log.d(TAG, "in onPostExecute() of NetworkIOTask"); // update the UI setConnecting(false); handler.removeCallbacks(busyCue);
	 * if (!chaining) { ui.showBusyCues(false); } // send a message to the mBusyCueHandler defined in this class (AgentFSM) // this allows our asyncTask thread to complete and exit, and subsequent
	 * network calls // will be threads created off the main thread instead of stacking Message msg = Message.obtain(); msg.obj = serverResponse; networkResultHandler.sendMessage(msg); } }
	 * 
	 * @SuppressLint("HandlerLeak") private Handler networkResultHandler = new Handler() {
	 * 
	 * @Override public void handleMessage(Message msg) { if (msg.obj != null && msg.obj instanceof String) { Log.d(TAG, "networkResultHandler was passed a String."); handleResponse((String) msg.obj);
	 * } else { Log.w(TAG, "Strange, networkResultHandler was passed a message that was not a String.  ignoring"); } } };
	 */

	public AgentChallengeResponse getCurrentChallenge() {
		return mCurrentChallenge;
	}

	public void setCurrentChallenge(AgentChallengeResponse currentChallenge) {
		this.mCurrentChallenge = currentChallenge;
	}

	public void restoreState(String sessionToken, String apiBase, int currentState, AgentChallengeResponse currentChallenge, String launchType, long sessionExpirationDateMS) {
		resetAgent();
		int secsLeft = (sessionExpirationDateMS > System.currentTimeMillis()) ? (int) ((sessionExpirationDateMS - System.currentTimeMillis()) / 1000) : 0;
		Log.w(TAG, "in restoreState(), setting session left to live value to " + secsLeft + " seconds");
		this.mSession = new IDSession(apiBase, sessionToken, launchType, secsLeft);
		this.currentState = currentState;
		this.apiBase = apiBase;
		this.mCurrentChallenge = currentChallenge;
		this.active = true;
	}

	public void clearSession() {
		mSession = null;
		this.manageAllowed = false;
		this.active = false;
	}

	public int getOrientationAtCapture() {
		return mOrientationAtCapture;
	}

	public void setOrientationAtCapture(int orientationAtCapture) {
		this.mOrientationAtCapture = orientationAtCapture;
	}

	public boolean[] getTouchRegionsAtCapture() {
		return mTouchRegionsAtCapture;
	}

	public void setTouchRegionsAtCapture(boolean[] mTouchRegionsAtCapture) {
		this.mTouchRegionsAtCapture = mTouchRegionsAtCapture;
	}

	public void resetDeviceNonce() {
		this.mShuffler.resetSoftwareInstallNonce();
	}

	/**
	 * determine if the supplied challenge is appropriate for serialization
	 * 
	 * @param chal
	 * @return
	 */
	public boolean isChallengeRestorable(AgentChallengeResponse chal) {
		if (chal == null)
			return false;
		if (CHALLENGE_NAME_PIN.equals(chal.getChallengeType()))
			return true;
		if (CHALLENGE_NAME_PROMPT.equals(chal.getChallengeType()))
			return true;
		return false;
	}

	public boolean isPebbleChallengeAnswered() {
		return mPebbleChallengeAnswered;
	}

	public void setPebbleChallengeAnswered(boolean mPebbleChallengeAnswered) {
		this.mPebbleChallengeAnswered = mPebbleChallengeAnswered;
	}

}
