package com.foursure1;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.liveensure.a.mini.MiniLiveEnsureActivity;
import com.liveensure.util.StringHelper;

public class FrontPageActivity extends BaseActivity {

	public static final String TAG = FrontPageActivity.class.getSimpleName();
	private static final int SESSION_START = 1;
	private static final int SESSION_STATUS = 3;
	private static final int SESSION_COMPLETE = 4;
	private static final int IN_LIVEENSURE_SESSION = 5;
	private int mCurrentState;
	private FourSureApplication myApp;
	protected String mUserName;
	private ImageView mBtnShowSettings;
	private ImageView mBtnStart;
	private FourSureFSM mFSM;
	private RelativeLayout mLytWindowShade;
	private boolean mEnteringSettings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myApp = (FourSureApplication) getApplication();
		setContentView(R.layout.activity_front_page);
		mBtnShowSettings = (ImageView) findViewById(R.id.button1);

		// Make sure the windows shade is gone
		mLytWindowShade = (RelativeLayout) findViewById(R.id.lyt_front_page_windowshade);
		mLytWindowShade.setVisibility(View.GONE);

		mBtnStart = (ImageView) findViewById(R.id.img_lock);
		mBtnShowSettings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (StringHelper.isEmpty(myApp.getDeviceUUID())) {
					// first time through, user has not called signup before, skip LE
					Log.i(TAG, "entering settings for the first time, skipping LE");
					mCurrentState = SESSION_STATUS;
					Intent intent = new Intent(myApp, SettingsActivity.class);
					startActivity(intent);
				} else {
					// user has already registered this device, go through LE first
					Log.i(TAG, "entering settings on a registered device, starting LE");
					mEnteringSettings = true;
					// make sure we always start with a clean slate
					myApp.resetFsSession();
					myApp.clearCurrentShareDetails();
					myApp.setSettingsSuccess(false);
					mCurrentState = SESSION_START;
					mFSM.callStart(null);
				}
			}
		});

		mBtnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentState = SESSION_START;
				myApp.clearCurrentShareDetails();
				mFSM.callStart(null);
			}
		});
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "in onResume()");
		super.onResume();
		mFSM = myApp.getFSM();
		mFSM.setUI(TAG, this);
		if (mCurrentState == IN_LIVEENSURE_SESSION) {
			mCurrentState = SESSION_STATUS;
			mFSM.callStatus();
		} else {
			resetUI();
		}
	}

	private void resetUI() {
		mBtnStart.setVisibility(View.VISIBLE);
		mBtnStart.setAlpha(1.0f);
		mLytWindowShade.setVisibility(View.GONE);
		mEnteringSettings = false;
	}

	public void launchLiveEnsure() {

		Log.w(TAG, "launching LiveEnsure");
		Intent miniLiveEnsureIntent = new Intent(this, MiniLiveEnsureActivity.class);
		// miniLiveEnsureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		miniLiveEnsureIntent.putExtra("sessionToken", myApp.getLESessionToken());
		miniLiveEnsureIntent.putExtra("identityServer", myApp.getLEIdentityServer());
		miniLiveEnsureIntent.putExtra("showChallengeIndicators", myApp.isShowChallengeIndicators());
		miniLiveEnsureIntent.putExtra("source", "com.foursure");

		// Check to see if the LiveEnsure app is installed on the device.
		if (miniLiveEnsureIntent.resolveActivity(getPackageManager()) != null) {
			mLytWindowShade.setAlpha(0.0f);
			mLytWindowShade.setVisibility(View.VISIBLE);
			mLytWindowShade.animate().alpha(1.0f).setListener(null).start();
			mCurrentState = IN_LIVEENSURE_SESSION;
			startActivity(miniLiveEnsureIntent);
		} else {
			Toast.makeText(getApplicationContext(), "Unable to launch LiveEnsure.", Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void handleError(String msg) {
		Log.i(TAG, "in handleError()");
		mLytWindowShade.setVisibility(View.GONE);
		// check to see if we're resuming from a signup call
		if (mFSM != null) {
			if (mFSM.getCurrentState() == FourSureFSM.DEVICE_SIGNUP_FAILED) {
				Log.w(TAG, "signup failed, allowing user to try again");
				Toast.makeText(getApplicationContext(), "Unable to signup, please try again", Toast.LENGTH_LONG).show();
			} else if (mFSM.getCurrentState() == FourSureFSM.SESSION_FAILED) {
				Log.w(TAG, "session failed, allowing user to try again");
				Toast.makeText(getApplicationContext(), "Unable to authenticate, please try again", Toast.LENGTH_LONG).show();
			} else if (mFSM.getCurrentState() == FourSureFSM.SESSION_STATUS_FAILED) {
				Log.w(TAG, "session status failed, allowing user to try again");
				Toast.makeText(getApplicationContext(), "Unable to authenticate, please try again", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void showSuccess(String msg) {
		Log.i(TAG, "in showSuccess()");
		mLytWindowShade.setVisibility(View.GONE);
		if (mCurrentState == SESSION_START) {
			Intent intent = null;
			if (mEnteringSettings) {
				mEnteringSettings = false;
				// show settings page
				intent = new Intent(myApp, SettingsActivity.class);
			} else {
				// move on to sharing screen
				intent = new Intent(myApp, ShareCreateActivity.class);
			}
			myApp.setLiveEnsureAuthenticated(true);
			mCurrentState = SESSION_COMPLETE;
			startActivity(intent);
		} else {
			resetUI();
		}
	}

	public void handleStatusResult(int status) {
		if (status == FourSureFSM.SESSION_STATUS_UNDETERMINED) {
			mCurrentState = SESSION_STATUS;
			mFSM.callStatus();
		} else if (status == FourSureFSM.NO_SETTINGS_AVAILABLE) {
			// bounce the settings icon
			float y = mBtnShowSettings.getTranslationY();
			float distance = 40F;
			AnimatorSet as = new AnimatorSet();
			as.playSequentially(ObjectAnimator.ofFloat(mBtnShowSettings, "translationY", y - distance), ObjectAnimator.ofFloat(mBtnShowSettings, "translationY", y),
					ObjectAnimator.ofFloat(mBtnShowSettings, "translationY", y - (distance / 2)), ObjectAnimator.ofFloat(mBtnShowSettings, "translationY", y),
					ObjectAnimator.ofFloat(mBtnShowSettings, "translationY", y - (distance / 4)), ObjectAnimator.ofFloat(mBtnShowSettings, "translationY", y));
			as.setDuration(300);
			as.start();
		} else if (status == FourSureFSM.SESSION_STATUS_SUCCESS) {
			Intent intent = null;
			if (mEnteringSettings) {
				mEnteringSettings = false;
				// show settings page
				intent = new Intent(myApp, SettingsActivity.class);
			} else {
				// move on to sharing screen
				intent = new Intent(myApp, ShareCreateActivity.class);
			}
			myApp.setLiveEnsureAuthenticated(true);
			mCurrentState = SESSION_COMPLETE;
			startActivity(intent);
		} else {
			mLytWindowShade.setVisibility(View.GONE);
			// some error condition
			Toast.makeText(getApplicationContext(), "Authentication failed, please try again", Toast.LENGTH_LONG).show();
		}
	}
}
