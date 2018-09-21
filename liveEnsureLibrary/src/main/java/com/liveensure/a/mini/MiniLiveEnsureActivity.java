package com.liveensure.a.mini;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.liveensure.a.mini.fragments.BaseChallengeFragment;
import com.liveensure.a.mini.fragments.BehaviorChallengeFragment;
import com.liveensure.a.mini.fragments.EmptyFragment;
import com.liveensure.a.mini.fragments.ErrorFragment;
import com.liveensure.a.mini.fragments.HelpFragment;
import com.liveensure.a.mini.fragments.MapChallengeFragment;
import com.liveensure.a.mini.fragments.PinChallengeFragment;
import com.liveensure.a.mini.fragments.PromptChallengeFragment;
import com.liveensure.a.mini.fragments.SuccessFragment;
import com.liveensure.a.mini.fragments.TransparentFragment;
import com.liveensure.a.mini.fragments.WearableChallangeFragment;
import com.liveensure.core.AgentFSM;
import com.liveensure.rest.api.AgentChallengeResponse;
import com.liveensure.util.Log;
import com.liveensure.util.StringHelper;

/**
 * Main LiveEnsure activity. Most UI components come from the various fragment classes
 * 
 */
public class MiniLiveEnsureActivity extends BaseActivity implements UIForFiniteStateMachine {

	private static final String DEFAULT_HOST = "localhost";
	protected final static String TAG = MiniLiveEnsureActivity.class.getSimpleName();

	private FragmentManager mFragmentManager;
	private RelativeLayout mMainLayout;
	private FrameLayout mFragmentContainer;
	private boolean mDestroyed;
	private BaseChallengeFragment mCurrentChallengeFragment;
	public static final int NUM_TOUCH_REGIONS = 6;
	private int googlePlayRequestID = 1;
	private static final long EXIT_DELAY_MS = 400;
	private static final String FRAGMENT_NAME_EMPTY = "fragment_empty";
	// private static final String FRAGMENT_NAME_TRANSPARENT = "fragment_transparent";
	private static final String FRAGMENT_NAME_SUCCESS = "fragment_success";
	private static final String FRAGMENT_NAME_FAILURE = "fragment_failure";
	private static final String FRAGMENT_NAME_ERROR = "fragment_error";
	private static final String FRAGMENT_NAME_HELP = "fragment_help";
	private static final String FRAGMENT_NAME_PROMPT_CHALLENGE = "fragment_prompt";
	private static final String FRAGMENT_NAME_PIN_CHALLENGE = "fragment_pin";
	private static final String FRAGMENT_NAME_MAP_CHALLENGE = "fragment_map";
	private static final String FRAGMENT_NAME_BEHAVIOR_CHALLENGE = "fragment_behavior_challenge";
	private static final String FRAGMENT_NAME_WEARABLE_CHALLENGE = "fragment_wearable_challenge";
	private Handler mExitHandler = new Handler();
	private boolean mExitPending;
	IInAppBillingService mGoogleBillingService;
	private ServiceConnection mBillingServiceConnection;
	private int mCurrentOrientationQuadrant = 0; // default to portrait
	private int mCurrentOrientationDegrees = 0;
	private OrientationEventListener orientationEventListener;
	// private SoundPool mSoundPool;
	// private int mBeepID;
	private boolean mDeviceNaturalLandscape;
	private boolean mDevHostScanned;
	private boolean mGooglePlayRequired = true;
	// private Animation mAnimSpinnerRotate;
	private boolean mShowChallengeIndicators;
	private int mCurrentLayoutID;
	private int mFragmentContainerID;
	private boolean mContinueSpinning;
	private boolean mInSpinnerAnimation;
	private ImageView mCircle1;
	private ImageView mCircle2;
	private ImageView mCircle3;
	private ImageView mCircle4;
	private AnimatorSet mCircleAnimationSet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e(TAG, "in onCreate()");
		super.onCreate(savedInstanceState);
		mDestroyed = false;

		LEApplication leApp = new LEApplication(this, getApplicationContext()); // NEW code for library version, LEApplication is no longer a subclass of Application
		setApp(leApp);
		getAgentFSM().setUI(this);
		getApp().setStartupIntent(getIntent());

		Log.i(TAG, "deviceNaturalLandscape is: " + mDeviceNaturalLandscape);
		// load the sound pool for our beep playback
		// initializeSoundPool();

		if (getApp().isDebugMode()) {
			getApp().setStoreOverride(true);
		}

		// set up a listener for Android's orientation sensor. This works even when the UI orientation is locked (e.g. portrait or landscape)
		orientationEventListener = new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int orientation) {
				MiniLiveEnsureActivity.this.onOrientationChanged(orientation);
			}
		};
	}

	private void setUpLayoutAndElements(int layoutID) {
		setContentView(layoutID);
		mCurrentLayoutID = layoutID;
		mFragmentManager = getFragmentManager();
		int circle1ID = R.id.circle1;
		int circle2ID = R.id.circle2;
		int circle3ID = R.id.circle3;
		int circle4ID = R.id.circle4;
		mMainLayout = (RelativeLayout) findViewById(R.id.main_layout);
		mFragmentContainerID = R.id.fragment_container;
		mFragmentContainer = (FrameLayout) findViewById(mFragmentContainerID);
		if (mFragmentContainer != null && getIntent() != null) {
			setShowChallengeIndicators(getIntent().getBooleanExtra("showChallengeIndicators", false));
			int customBackgroundColor = getIntent().getIntExtra("backgroundColor", 0);
			String customBackgroundColorARGB = getIntent().getStringExtra("backgroundColorARGB");
			if (customBackgroundColor != 0 || !StringHelper.isEmpty(customBackgroundColorARGB)) {
				try {
					if (!StringHelper.isEmpty(customBackgroundColorARGB) && customBackgroundColor == 0) {
						customBackgroundColor = Color.parseColor(customBackgroundColorARGB);
					}
					mMainLayout.setBackgroundColor(customBackgroundColor);
					Log.i(TAG, "setting UI background to custom color " + customBackgroundColor);
				} catch (Exception e) {
					Log.w(TAG, "unable to parse custom color string '" + customBackgroundColor + "'");
				}
			}
		}

		// mAnimSpinnerRotate = AnimationUtils.loadAnimation(this, R.anim.spinner_rotate);
		// mAnimSpinnerRotate.setInterpolator(new AccelerateDecelerateInterpolator());
		// mAnimSpinnerRotate.setAnimationListener(this);
		mCircle1 = (ImageView) findViewById(circle1ID);
		mCircle2 = (ImageView) findViewById(circle2ID);
		mCircle3 = (ImageView) findViewById(circle3ID);
		mCircle4 = (ImageView) findViewById(circle4ID);
		// set up progress animation
		float largeCircleScale = 1.45f;
		long duration = 800;

		PropertyValuesHolder scalex = PropertyValuesHolder.ofFloat("scaleX", 1.0f, largeCircleScale, 1.0f);
		PropertyValuesHolder scaley = PropertyValuesHolder.ofFloat("scaleY", 1.0f, largeCircleScale, 1.0f);
		ObjectAnimator circle1Up = ObjectAnimator.ofPropertyValuesHolder(mCircle1, scalex, scaley);
		ObjectAnimator circle2Up = ObjectAnimator.ofPropertyValuesHolder(mCircle2, scalex, scaley);
		ObjectAnimator circle3Up = ObjectAnimator.ofPropertyValuesHolder(mCircle3, scalex, scaley);
		ObjectAnimator circle4Up = ObjectAnimator.ofPropertyValuesHolder(mCircle4, scalex, scaley);
		circle1Up.setDuration(duration);
		circle2Up.setDuration(duration);
		circle3Up.setDuration(duration);
		circle4Up.setDuration(duration);
		circle2Up.setStartDelay(duration / 2);
		circle3Up.setStartDelay(duration);
		circle4Up.setStartDelay(duration + duration / 2);
		mCircleAnimationSet = new AnimatorSet();
		mCircleAnimationSet.playTogether(circle1Up, circle2Up, circle3Up, circle4Up);
		mCircleAnimationSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				int bci = R.id.busy_content;
				final LinearLayout bc = (LinearLayout) findViewById(bci);
				if (mContinueSpinning) {
					mCircleAnimationSet.start();
					mInSpinnerAnimation = true;
				} else {
					bc.animate().alpha(0.0f).setDuration(150).setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							bc.setVisibility(View.INVISIBLE);
							mInSpinnerAnimation = false;
						}
					});
				}
			}
		});

	}

	@Override
	protected void onRestart() {
		Log.i(TAG, "in onRestart()");
		super.onRestart();
	}

	@Override
	protected void onStart() {
		Log.i(TAG, "in onStart()");
		super.onStart();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.i(TAG, "in onNewIntent()");
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "in onResume()");
		super.onResume();

		// see if we were started from a browser or another android app (rollover), or by user direct launch (show camera acquisition)
		// in the rollover case, this will extract the session token and host values from the Bundle handed to us by android
		determineLaunchMechanism();
		if (wasLaunchedBy3rdParty()) {
			// Set up main UI pieces
			Log.e(TAG, "rollover detected, showing mini UI");
			// remove any fragments on the back stack
			setUpLayoutAndElements(R.layout.main_mini);
		} else {
			// we were not launched by app to app or other mechanism, user likely tapped our icon to start. initialize to show camera scan and full screen UI
			Log.w(TAG, "abnormal launch detected, cannot show full screen UI");
			setUpLayoutAndElements(R.layout.main_mini);
			// Set up the pull-out drawer (nav menu)
		}

		if (!checkGooglePlayServices() && mGooglePlayRequired) {
			// Google play services are not available. This means no inapp purchasing and likely no GPS location updates. show an error dialog and exit the app
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle(getResources().getString(R.string.msg_unable_to_launch));
			alertDialogBuilder.setMessage(getResources().getString(R.string.msg_google_play_services_reqd)).setCancelable(false)
					.setPositiveButton(getResources().getString(R.string.btn_label_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					}).setNegativeButton(getResources().getString(R.string.btn_label_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});

			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.show();
			finish();
		}

		// start listening for orientation sensor events
		orientationEventListener.enable();

		// hide the keyboard, sometimes it is shown after returning from a google play in-app purchase
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// make sure the network connection busy cues are not showing
		showBusyCues(false);

		setDeviceNaturalLandscape(checkForNaturalLandscapeDevice());
		mDevHostScanned = false;

		AgentFSM fsm = getAgentFSM();

		// fade in our main view
		mFragmentContainer.setAlpha(0f);
		mFragmentContainer.setVisibility(View.VISIBLE);
		mFragmentContainer.animate().alpha(1.0f).setDuration(200).setListener(null);

		// if the session was restored and we have an active session and a current challenge, show that challenge
		if (fsm != null && fsm.getSession() != null && fsm.isChallengeRestorable(fsm.getCurrentChallenge())) {
			showChallenge(getAgentFSM().getCurrentChallenge());
		} else {
			// load initial fragment
			if (findViewById(mFragmentContainerID) != null) {
				Fragment f = new TransparentFragment();
				saveSessionState(); // in case app is backgrounded or suspended, we can reconstitute it later
				fsm.makeRetrieveDecisionCall(); // makes initial retrieveDecision call on background thread
				if (f != null)
					f.setArguments(getIntent().getExtras());
				Fragment existingFragment = mFragmentManager.findFragmentById(mFragmentContainerID);
				if (existingFragment == null) {
					// no existing fragments yet, add our first one
					mFragmentManager.beginTransaction().add(mFragmentContainerID, f).commit();
				} else {
					// there is already a fragment in the container, replace it
					mFragmentManager.beginTransaction().replace(mFragmentContainerID, f).commit();
				}
			}
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		Log.i(TAG, "in onPostCreate()");
		super.onPostCreate(savedInstanceState);
	}

	/**
	 * called when the app is backgrounded (but not ended)
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "in onPause()");

		// stop listening to the orientation sensor events
		orientationEventListener.disable();

		if (mBillingServiceConnection != null) {
			unbindService(mBillingServiceConnection);
		}

		// if the session is ongoing (or the user is in the middle of a google play purchase flow) then save the session state
		AgentFSM fsm = getAgentFSM();
		if ((!fsm.isFailed() && !fsm.isSucceeded() && fsm.isChallengeRestorable(fsm.getCurrentChallenge())) || getApp().isInPurchase()) {
			saveSessionState();
		} else {
			String exitMessage = " ";
			if (fsm.isFailed())
				exitMessage = "session is complete and failed ";
			if (fsm.isSucceeded())
				exitMessage = "session is complete and succeeded ";
			if (!fsm.isChallengeRestorable(fsm.getCurrentChallenge())) {
				exitMessage += ", current challenge is not restorable ";
			}
			exitMessage += ", making stack Agent_Closed call and clearing local state.";
			Log.i(TAG, exitMessage);
			showEmpty(0);
			// session is complete, user is leaving our app (~ 98% of the time). Clear session state for a fresh start next time we resume
			fsm.resetAgent();
			clearSavedSessionState();
		}
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "in onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "in onDestroy()");
		super.onDestroy();
		mDestroyed = true;
	}

	/*
	 * helper methods (not Activity lifecycle methods) below
	 */
	// private void initializeSoundPool()
	// {
	// mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
	// mBeepID = mSoundPool.load(this, R.raw.beep, 1);
	// }

	/**
	 * event that is fired each time android detects an orientation change. On the Samsung Galaxy Nexus, that is at 1 degree increments. we are currently
	 * interested in the 4 cardinal orientations and so this method only updates the current orientation member variable when the device is oriented more than
	 * 60 degrees away from the old orientation.
	 */
	private void onOrientationChanged(int orientation) {
		if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN)
			return;
		int diff = Math.abs(orientation - mCurrentOrientationDegrees);
		if (diff > 180)
			diff = 360 - diff;
		if (diff > 60) {
			orientation = (orientation + 45) / 90 * 90;
			orientation = orientation % 360;
			if (orientation != mCurrentOrientationDegrees) {
				this.mCurrentOrientationDegrees = orientation;
				switch (mCurrentOrientationDegrees) {
				case 0:
					mCurrentOrientationQuadrant = (mDeviceNaturalLandscape) ? 3 : 0; // portrait
					break;
				case 90:
					mCurrentOrientationQuadrant = (mDeviceNaturalLandscape) ? 0 : 1; // landscape left (home button on left)
					break;
				case 180:
					mCurrentOrientationQuadrant = (mDeviceNaturalLandscape) ? 1 : 2; // inverted portrait
					break;
				case 270:
					mCurrentOrientationQuadrant = (mDeviceNaturalLandscape) ? 2 : 3; // landscape right
					break;
				default:
					mCurrentOrientationQuadrant = -1;
					break;
				}
			}
		}
	}

	/*
	 * All the showXXX() methods below will transition out the currently showed fragment for one of the others. typically called by the AgentFSM class
	 */

	public void showEmpty(int statusIconID) {
		if (mDestroyed)
			return;
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
		EmptyFragment f = EmptyFragment.newInstance(statusIconID);
		ft.replace(mFragmentContainerID, f, FRAGMENT_NAME_EMPTY);
		ft.commit();
	}

	public void showError(String msg) {
		if (mDestroyed)
			return;
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
		ErrorFragment f = ErrorFragment.newInstance(msg);
		ft.replace(mFragmentContainerID, f, FRAGMENT_NAME_ERROR);
		ft.commit();
	}

	public void showFailure() {
		if (mDestroyed)
			return;
		clearSavedSessionState();
		if (wasLaunchedBy3rdParty()) {
			mMainLayout.animate().alpha(0f).setStartDelay(EXIT_DELAY_MS - 250).setDuration(200).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mMainLayout.setVisibility(View.GONE);
				}
			});
			setExitPending(true);
			setLaunchedBy3rdParty(false);
			getApp().setStartupIntent(null);
			mExitHandler.postDelayed(new Runnable() {
				public void run() {
					if (isExitPending())
						finish();
				}
			}, EXIT_DELAY_MS);
		} else {
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
			EmptyFragment f = EmptyFragment.newInstance(R.drawable.status_failure_512);
			ft.replace(mFragmentContainerID, f, FRAGMENT_NAME_FAILURE);
			ft.commit();
			Log.w(TAG, "in showFailure(), should be showing failure fragment");
		}
	}

	public void showSuccess(String msg) {
		Log.w(TAG, "in showSuccess()");
		if (mDestroyed)
			return;
		clearSavedSessionState();

//		if (getAgentFSM().isPebbleChallengeAnswered()) {
//			sendSucessAlertToPebble();
//		}
		if (wasLaunchedBy3rdParty()) {
			Log.w(TAG, "in wasLaunchedBy3rdParty()");
			mMainLayout.animate().alpha(0f).setStartDelay(EXIT_DELAY_MS - 250).setDuration(200).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mMainLayout.setVisibility(View.GONE);
				}

			});
			setExitPending(true);
			setLaunchedBy3rdParty(false);
			getApp().setStartupIntent(null);
			mExitHandler.postDelayed(new Runnable() {
				public void run() {
					if (isExitPending()) {
						finish();
					}
				}
			}, EXIT_DELAY_MS);
		} else {
			Log.w(TAG, "NOT in wasLaunchedBy3rdParty()");
			showBusyCues(false);
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			SuccessFragment f = SuccessFragment.newInstance(msg);
			ft.replace(mFragmentContainerID, f, FRAGMENT_NAME_SUCCESS);
			ft.commit();
		}
	}

	public void showHelp() {
		if (mDestroyed)
			return;
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
		HelpFragment f = HelpFragment.newInstance();
		ft.replace(mFragmentContainerID, f, FRAGMENT_NAME_HELP);
		ft.commit();
	}

	public void showChallenge(AgentChallengeResponse chal) {
		BaseChallengeFragment newFragment = null;
		String newFragmentName = "";
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
		boolean fullScreenChallenge = false;

		if (AgentFSM.CHALLENGE_NAME_PROMPT.equals(chal.getChallengeType())) {
			newFragment = PromptChallengeFragment.newInstance(chal);
			newFragmentName = FRAGMENT_NAME_PROMPT_CHALLENGE;
		}
		if (AgentFSM.CHALLENGE_NAME_PIN.equals(chal.getChallengeType())) {
			newFragment = PinChallengeFragment.newInstance(chal);
			newFragmentName = FRAGMENT_NAME_PIN_CHALLENGE;
		}
		if (AgentFSM.CHALLENGE_NAME_LAT_LONG.equals(chal.getChallengeType()) || AgentFSM.CHALLENGE_NAME_HOME.equals(chal.getChallengeType())) {
			LocationManager lm = null;
			boolean gps_enabled = false, network_enabled = false;
			if (lm == null)
				lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			try {
				gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			} catch (Exception ex) {
				Log.w(TAG, "exception during test for gps enabled: " + ex.getMessage());
			}
			try {
				network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			} catch (Exception ex) {
				Log.w(TAG, "exception during test for network location enabled: " + ex.getMessage());
			}
			Log.i(TAG, "LAT_LONG challenge, gps_enabled is " + gps_enabled + ", network_enabled is " + network_enabled);
			if ((!gps_enabled && !network_enabled) || !getApp().isGoogleServicesAvailable()) {
				Log.w(TAG, "failed prerequisite tests for showing Google Map, challenge unanswered");
				getAgentFSM().makeAnswerChallengeCallWithID(chal.getChallengeID(), AgentFSM.CHALLENGE_ANSWER_UNAVAILABLE, "1.0");
				return;
			}
			newFragment = MapChallengeFragment.newInstance(chal);
			newFragmentName = FRAGMENT_NAME_MAP_CHALLENGE;
			fullScreenChallenge = true;
		}
		if (AgentFSM.CHALLENGE_NAME_AGENT_BEHAVIOR.equals(chal.getChallengeType()) || AgentFSM.CHALLENGE_NAME_HOST_BEHAVIOR.equals(chal.getChallengeType())) {
			// this should only be called if we did not capture behavior during QR scan (see handleChallenge() in AgentFSM)
			newFragment = BehaviorChallengeFragment.newInstance(chal);
			newFragmentName = FRAGMENT_NAME_BEHAVIOR_CHALLENGE;
			// fullScreenChallenge = true;
		}
		if (AgentFSM.CHALLENGE_NAME_PEBBLE.equals(chal.getChallengeType())) {
			newFragment = WearableChallangeFragment.newInstance(chal);
			newFragmentName = FRAGMENT_NAME_WEARABLE_CHALLENGE;
			fullScreenChallenge = true;
		}
		if (fullScreenChallenge) {
			Log.w(TAG, "full screen challenge");
			showBusyCues(true); // make sure we continue to show the busy spinner until the challenge is answered
		} else {
			Log.w(TAG, "small fragment challenge");
			// Fade out the busy spinner if it is visible. We do this directly here to force an immediate fade, not waiting for the usual
			// onAnimationEnd() check
			if (mInSpinnerAnimation) {
				int bci = R.id.busy_content;
				final LinearLayout bc = (LinearLayout) findViewById(bci);
				bc.animate().alpha(0.0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						bc.setVisibility(View.INVISIBLE);
						mInSpinnerAnimation = false;
					}
				});
			}

		}

		ft.replace(mFragmentContainerID, newFragment, newFragmentName);
		ft.commit();
		mCurrentChallengeFragment = newFragment;
	}

	/**
	 * called by AgentFSM if there are any retries left on the current challenge. Implemented in the underlying challenge fragments
	 */
	public void retryCurrentChallenge() {
		mCurrentChallengeFragment.retryChallenge();
	}

	/**
	 * agent FSM and UI resets necessary to start a new auth session. called at beginning of app or when user starts a new session from UI
	 */
	public void startNewSession() {
		if (mDestroyed)
			return;
		// reset the Agent
		getAgentFSM().resetAgent();
		clearSavedSessionState();
		mDevHostScanned = false;
		// show a new session fragment
		// FragmentTransaction ft = mFragmentManager.beginTransaction();
		// ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
		// CameraFragment f = new CameraFragment();
		// mFragmentManager.beginTransaction().replace(mFragmentContainerID, f).commit();
	}

	/**
	 * event that is fired from the when a barcode has been decoded
	 */
	public void onBarcodeDecodeComplete(String textFromBarcode) {
		mDevHostScanned = false;
		String possibleQR = getValidStringFromQR(textFromBarcode);
		if (possibleQR == null) {
			// we did not get a well formed LiveEnsure string from the QR. fail here
			getApp().setInvalidQRCode(true);
			showError(getResources().getString(R.string.bad_scan));
			return;
		}

		// we got this far, set the invalid flag to false
		getApp().setInvalidQRCode(false);
		String sessionToken = null;
		StringBuffer liveURL = null;
		if (possibleQR.length() > 1) {
			String hostChar = possibleQR.substring(0, 1);
			sessionToken = possibleQR.substring(1); // all but first char
			// Check for dev host code
			if (hostChar.equals("*")) {
				liveURL = new StringBuffer(getApp().getDefaultHost());
				Log.i(TAG, "Host char is *, using url of " + liveURL.toString());
				setDevHostScanned(true);
			} else {
				String host = getHostFromScanCode(possibleQR);
				liveURL = new StringBuffer("https://");
				liveURL.append(host);
				String portNum = "443";
				liveURL.append(":" + portNum);
				liveURL.append("/live-identity/rest");
				setDevHostScanned(false);
			}
		}
		if (sessionToken == null || liveURL == null || liveURL.length() == 0) {
			getApp().setInvalidQRCode(true);
			showError(getResources().getString(R.string.bad_scan));
			return;
		}
		getAgentFSM().setOrientationAtCapture(mCurrentOrientationQuadrant);
		getAgentFSM().setupSession(sessionToken, liveURL.toString(), "QR");
		saveSessionState(); // in case app is backgrounded or suspended, we can reconstitute it later
		getAgentFSM().makeRetrieveDecisionCall(); // makes initial retrieveDecision call on background thread

	}

	private String getHostFromScanCode(String contents) {
		String response = null;
		if (contents.length() > 1) {
			String hostChar = contents.substring(0, 1);
			if (hostChar.equals("*"))
				response = DEFAULT_HOST;
			else {
				char hc = contents.charAt(0);
				int hcVal = (int) hc;
				if (hcVal >= 65 && hcVal <= 90) {
					// upper case A-Z, liveensure hosts
					response = String.format(Locale.US, "app%02d.liveensure.com", hcVal - 64);
				}
				if (hcVal >= 97 && hcVal <= 122) {
					// lower case a-z, mastlabs hosts
					response = String.format(Locale.US, "ec2app%02d.mastlabs.com", hcVal - 96);
				}
			}
		}
		return response;
	}

	private String getValidStringFromQR(String str) {
		if (str == null)
			return str;
		if (str.length() == 25) {
			// v3 minimal token valid start char and basic content make sure
			// there is not a dot
			if (str.indexOf('.') < 0)
				return str;
		} else {
			// v4 check for bit.ly prefix and sufficient length
			if (str.startsWith("https://bit.ly/CZLiU?s=")) {
				if (str.length() >= 48) {
					return str.substring(23, 48);
				}
			}
			if (str.startsWith("http://bit.ly/CZLiU?s=")) {
				if (str.length() >= 47) {
					return str.substring(22, 47);
				}
			}
		}
		return null;
	}

	public int getCurrentOrientation() {
		return mCurrentOrientationQuadrant;
	}

	public boolean isDeviceNaturalLandscape() {
		return mDeviceNaturalLandscape;
	}

	public void setDeviceNaturalLandscape(boolean isDeviceNaturalLandscape) {
		this.mDeviceNaturalLandscape = isDeviceNaturalLandscape;
	}

	public boolean isDevHostScanned() {
		return mDevHostScanned;
	}

	public void setDevHostScanned(boolean devHostScanned) {
		this.mDevHostScanned = devHostScanned;
	}

	public boolean isExitPending() {
		return mExitPending;
	}

	public void setExitPending(boolean exitPending) {
		this.mExitPending = exitPending;
	}

	// public SoundPool getSoundPool()
	// {
	// return mSoundPool;
	// }
	//
	// public int getBeepID()
	// {
	// return mBeepID;
	// }

	public void showBusyCues(boolean areWeBusy) {
		Log.w(TAG, "in showBusyCues(), flag is " + areWeBusy + ", mContinueSpinning is: " + mContinueSpinning + ", mInSpinnerAnimation is: "
				+ mInSpinnerAnimation);
		if (mCurrentLayoutID == R.layout.main_mini) {
			Log.e(TAG, "layout is mini");
		} else {
			Log.e(TAG, "layout is drawer");
		}
		int bci = R.id.busy_content;
		final LinearLayout bc = (LinearLayout) findViewById(bci);
		if (areWeBusy) {
			if (mInSpinnerAnimation) {
				Log.e(TAG, "in showBusyCues(), mInSpinnerAnimation is currently true, so not starting a new animation");
			} else {
				Log.e(TAG, "in showBusyCues(), mInSpinnerAnimation is currently false, so starting a new animation");
				// we aren't currently showing the spinner, so show it and start the animation. each time the animation ends,
				// the onAnimationEnd() method in this class will be called
				bc.setAlpha(0f);
				bc.setVisibility(View.VISIBLE);
				bc.animate().alpha(1.0f).setDuration(150).setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mCircleAnimationSet.start();
						mInSpinnerAnimation = true;
					}
				});
			}
			mContinueSpinning = true;
		} else {
			mContinueSpinning = false;
		}
	}

	private boolean checkGooglePlayServices() {
		try {
			int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
			if (status == ConnectionResult.SUCCESS) {
				Log.i(TAG, "Google Play Services is installed and up to date");
				getApp().setGoogleServicesAvailable(true);
				return true;
			} else {
				Log.w(TAG, "GooglePlayServicesUtil Connection Result: " + status);
				getApp().setGoogleServicesAvailable(false);
				if (GooglePlayServicesUtil.isUserRecoverableError(status) && (status != ConnectionResult.SERVICE_INVALID)) {
					Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, googlePlayRequestID);
					dialog.show();
					Log.w(TAG, "We think we launched the Google Play updatey thing");
					return false;
				} else {
					Log.e(TAG, "We think we have an unrecoverable Google Play Services error");
					Toast.makeText(this, getResources().getString(R.string.msg_loc_services_unsupported), Toast.LENGTH_LONG).show();
					return false;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Unexpected exception thrown during check for google play services", e);
			Toast.makeText(this, getResources().getString(R.string.msg_loc_services_nocheck), Toast.LENGTH_LONG).show();
			return false;
		}
	}

	public boolean checkForNaturalLandscapeDevice() {

		WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Configuration config = getResources().getConfiguration();
		int rotation = windowManager.getDefaultDisplay().getRotation();

		if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
				|| ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
			// device is a natural landscape device (e.g. a tablet)
			return true;
		} else {
			// device is a natural portrait device (e.g. phone)
			return false;
		}
	}

	// TODO: possibly implement this technique for dealing with device orientation changes? From Google I/O protips video
	public void remapCoordinateSystemForOrientation() {
		int screenRotation = getWindowManager().getDefaultDisplay().getRotation();
		int axisX = 0;
		int axisY = 0;
		switch (screenRotation) {
		case Surface.ROTATION_0:
			axisX = SensorManager.AXIS_X;
			axisY = SensorManager.AXIS_Y;
			break;

		case Surface.ROTATION_90:
			axisX = SensorManager.AXIS_Y;
			axisY = SensorManager.AXIS_MINUS_X;
			break;

		case Surface.ROTATION_180:
			axisX = SensorManager.AXIS_MINUS_X;
			axisY = SensorManager.AXIS_MINUS_Y;
			break;

		case Surface.ROTATION_270:
			axisX = SensorManager.AXIS_MINUS_Y;
			axisY = SensorManager.AXIS_X;
			break;

		default:
			break;
		}
		float[] inR = new float[2];
		float[] outR = new float[2];
		SensorManager.remapCoordinateSystem(inR, axisX, axisY, outR);
	}

	public void sendSucessAlertToPebble() {
		final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

		final Map<String, String> data = new HashMap<String, String>();
		// data.put("title", "\u00f4 LiveEnsure");
		data.put("title", "LiveEnsure");
		data.put("body", "Your Pebble was used for authentication");
		final JSONObject jsonData = new JSONObject(data);
		final String notificationData = new JSONArray().put(jsonData).toString();

		i.putExtra("messageType", "PEBBLE_ALERT");
		i.putExtra("sender", "LiveEnsure");
		i.putExtra("notificationData", notificationData);

		Log.d(TAG, "About to send a modal alert to Pebble: " + notificationData);
		sendBroadcast(i);
	}

	public boolean isShowChallengeIndicators() {
		return mShowChallengeIndicators;
	}

	public void setShowChallengeIndicators(boolean showChallengeIndicators) {
		Log.e(TAG, "setting showchallengindicators to " + showChallengeIndicators);
		this.mShowChallengeIndicators = showChallengeIndicators;
	}

}
