package com.foursure1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.foursure1.api.AccessShareResponse;
import com.foursure1.util.ShareType;
import com.liveensure.a.mini.MiniLiveEnsureActivity;
import com.liveensure.util.StringHelper;

public class ReceiveAssetActivity extends BaseActivity {

	public static final String TAG = ReceiveAssetActivity.class.getSimpleName();
	private static final int SESSION_START = 1;
	private static final int SESSION_STATUS = 2;
	private static final int SESSION_COMPLETE = 3;
	private static final int IN_LIVEENSURE_SESSION = 5;
	private int mCurrentState;
	private FourSureApplication myApp;
	protected String mUserName;
	private FourSureFSM mFSM;
	private RelativeLayout mLytWindowShade;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "in onCreate()");
		super.onCreate(savedInstanceState);
		myApp = (FourSureApplication) getApplication();
		myApp.getAppPrefs();
		mFSM = myApp.getFSM();
		setContentView(R.layout.activity_receive_asset);

		// Make sure the windows shade is gone
		mLytWindowShade = (RelativeLayout) findViewById(R.id.lyt_receive_windowshade);
		mLytWindowShade.setVisibility(View.GONE);

		Intent intent = getIntent();
		String shortCode = null;
		if (intent != null) {
			Uri data = intent.getData();
			if (data != null) {
				String path = data.getEncodedPath();
				Log.e(TAG, "intent URI path: " + path);
				// path should look like http://4shr.co/abc123
				if (path != null && path.startsWith("/"))
					shortCode = path.substring(1);
			}
			if (shortCode == null || shortCode.length() == 0) {
				// check the extras in case it was bundled in an intent
				Bundle extras = getIntent().getExtras();
				if (extras != null) {
					shortCode = extras.getString("shortCode");
				}
			}
			if (shortCode != null && shortCode.length() > 0) {
				Log.w(TAG, "got a short code of: " + shortCode);
				myApp.resetFsSession(); // clear out any existing session token to ensure we go through LE
				myApp.setShortCode(shortCode);
				mCurrentState = SESSION_START;
				mFSM.callStart(shortCode);
			}
		}
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "in onResume()");
		super.onResume();
		mFSM = myApp.getFSM();
		if (mFSM != null) {
			mFSM.setUI(TAG, this);
		} else {
			mLytWindowShade.setVisibility(View.GONE);
			Log.e(TAG, "uh oh, FSM is currently null, should not be like this.");
			Toast.makeText(getApplicationContext(), "Application error, please contact support", Toast.LENGTH_LONG).show();
		}
		if (mCurrentState == IN_LIVEENSURE_SESSION) {
			Log.i(TAG, "Checking status of LE session");
			mCurrentState = SESSION_STATUS;
			mFSM.callStatus();
		} else {
			resetUI();
		}
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "in onPause()");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.e(TAG, "in onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.e(TAG, "in onDestroy()");
		super.onDestroy();
	}

	private void resetUI() {
		mLytWindowShade.setVisibility(View.GONE);
	}

	public void launchLiveEnsure() {

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
	public void handleStatusResult(int status) {
		if (status == FourSureFSM.SESSION_STATUS_UNDETERMINED) {
			mCurrentState = SESSION_STATUS;
			mFSM.callStatus();
		} else if (status == FourSureFSM.SESSION_STATUS_SUCCESS) {
			// retrieve actual asset
			mFSM.callAccessShare();
		} else if (status == FourSureFSM.SHARE_ACCESS_FAILED) {
			Log.e(TAG, "accessing share failed");
			Intent intent = new Intent(myApp, FailureActivity.class);
			startActivity(intent);
			mCurrentState = SESSION_COMPLETE;
			finish();
		} else {
			// some error condition
			// Toast.makeText(getApplicationContext(), "Authentication failed, please try again", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(myApp, FailureActivity.class);
			startActivity(intent);
			mCurrentState = SESSION_COMPLETE;
			finish();
		}
	}

	@Override
	public void showSuccess(String msg) {
		Log.e(TAG, "in showSuccess()");
		// move on to asset screen
		AccessShareResponse resp = myApp.getShareToAccess();
		if (resp == null || StringHelper.isEmpty(resp.getShortCode()) || StringHelper.isEmpty(resp.getShareContent())) {
			Log.e(TAG, "called to show share, but response object is not fully populated");
			Intent intent = new Intent(myApp, FailureActivity.class);
			startActivity(intent);
			mCurrentState = SESSION_COMPLETE;
			finish();
		}
		Log.e(TAG, "in showSuccess() author flag is " + resp.isAuthor());
		if (resp.isAuthor()) {
			// author accessed share. show share create/edit screen
			myApp.setShareFactorType(resp.getShareFactorType());
			myApp.setShareOptionTracking(resp.isTrackingActive());
			myApp.setShareOptionShield(resp.getShieldType() != ShareType.NONE);
			myApp.setActivatedShareAssetType(resp.getShareType());
			myApp.setActivatedShareAsset(resp.getShareContent());
			myApp.setShortCode(resp.getShortCode());
			Intent intent = new Intent(myApp, ShareCreateActivity.class);
			startActivity(intent);
			finish();
		} else {
			Intent intent = new Intent(myApp, FileDisplayActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public void handleError(String msg) {
		Log.i(TAG, "in handleError()");
		if (mFSM != null) {
			if (mFSM.getCurrentState() == FourSureFSM.SESSION_FAILED) {
				myApp.setCurrentErrorMessage(getString(R.string.unable_to_access_share));
				Intent intent = new Intent(myApp, FailureActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear any activities
				startActivity(intent);
				finish();
			} else if (mFSM.getCurrentState() == FourSureFSM.SHARE_ACCESS_FAILED) {
				Log.e(TAG, "accessing share failed");
				Intent intent = new Intent(myApp, FailureActivity.class);
				startActivity(intent);
				mCurrentState = SESSION_COMPLETE;
				finish();
			} else {
				mLytWindowShade.setVisibility(View.GONE);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			}
		}
	}

}
