package com.foursure1;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.foursure1.util.FactorType;
import com.liveensure.util.StringHelper;

public class ShareSuccessActivity extends BaseActivity {

	public static final String TAG = ShareSuccessActivity.class.getSimpleName();
	private static final String FOURSURE_SHARE_PREFIX = "http://4shr.co/";
	private FourSureApplication myApp;
	private ImageView mBtnFinish;
	private ImageView mBtnShare;
	private ImageView mBtnEdit;
	private TextView mTxtAssetValue;
	protected boolean mKnowledgeActive;
	protected boolean mLocationActive;
	protected boolean mTimeActive;
	protected boolean mBehaviorActive;
	protected boolean mShieldActive;
	protected boolean mTrackActive;
	private ImageView mBtnOptionShield;
	private ImageView mBtnOptionTrack;
	private ImageView mBtnFactorKnowledge;
	private ImageView mBtnFactorLocation;
	private ImageView mBtnFactorTime;
	private ImageView mBtnFactorBehavior;
	private FourSureFSM mFSM;
	private String mCurrentShortCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myApp = (FourSureApplication) getApplication();
		myApp.getAppPrefs();
		mFSM = myApp.getFSM();
		mFSM.setUI(TAG, this);

		setContentView(R.layout.activity_share_success);

		// Short code label
		mTxtAssetValue = (TextView) findViewById(R.id.short_code_value);

		// Factor buttons
		mBtnFactorKnowledge = (ImageView) findViewById(R.id.share_factor_knowledge);
		mBtnFactorLocation = (ImageView) findViewById(R.id.share_factor_location);
		mBtnFactorTime = (ImageView) findViewById(R.id.share_factor_time);
		mBtnFactorBehavior = (ImageView) findViewById(R.id.share_factor_behavior);

		// Share options
		mBtnOptionShield = (ImageView) findViewById(R.id.share_option_shield);
		mBtnOptionTrack = (ImageView) findViewById(R.id.share_option_track);

		// Navigation buttons
		mBtnFinish = (ImageView) findViewById(R.id.btn_share_finish);
		mBtnEdit = (ImageView) findViewById(R.id.btn_share_edit);
		mBtnShare = (ImageView) findViewById(R.id.share_create);
		mBtnFinish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				myApp.resetFsSession();
				myApp.clearCurrentShareDetails();
				finish();
			}
		});
		mBtnShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goToShare();
			}
		});
		mBtnEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goToEdit();
			}
		});
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "in onResume()");
		mFSM = myApp.getFSM();
		mFSM.setUI(TAG, this);
		super.onResume();
		resetUI();
	}

	private void resetUI() {
		Log.e(TAG, "in resetUI(), factor type is " + myApp.getShareFactorType().name());
		mTxtAssetValue.setText(mCurrentShortCode);
		Resources res = myApp.getResources();
		if (myApp.getShareFactorType() == FactorType.KNOWLEDGE)
			mBtnFactorKnowledge.setImageDrawable(res.getDrawable(R.drawable.icon_knowledge_orange));
		if (myApp.getShareFactorType() == FactorType.LOCATION)
			mBtnFactorLocation.setImageDrawable(res.getDrawable(R.drawable.icon_location_orange));
		if (myApp.getShareFactorType() == FactorType.TIME)
			mBtnFactorTime.setImageDrawable(res.getDrawable(R.drawable.icon_time_orange));
		if (myApp.getShareFactorType() == FactorType.BEHAVIOR)
			mBtnFactorBehavior.setImageDrawable(res.getDrawable(R.drawable.icon_behavior_orange));
		if (!StringHelper.isEmpty(myApp.getActivatedShieldValue()))
			mBtnOptionShield.setImageDrawable(res.getDrawable(R.drawable.icon_shield_orange));
		if (myApp.isShareOptionTracking())
			mBtnOptionTrack.setImageDrawable(res.getDrawable(R.drawable.icon_track_orange));
		// copy text to clipboard
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("", mCurrentShortCode);
		clipboard.setPrimaryClip(clip);
		mCurrentShortCode = FOURSURE_SHARE_PREFIX + myApp.getShortCode();
		String shortShortCode = "4shr.co/" + myApp.getShortCode();
		mTxtAssetValue.setText(shortShortCode);
	}

	private void goToShare() {
		// Toast.makeText(this, "Android share dialog here", Toast.LENGTH_SHORT).show();
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		String shareBody = "  " + mCurrentShortCode; // leading spaces hack to FB messenger will show the link
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Foursure Asset");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		startActivity(Intent.createChooser(sharingIntent, "Share via"));
	}

	private void goToEdit() {
		// check for an expired session (actually being paranoid with 10 extra seconds)
		if (myApp.getFsSessionExpiration() + 10000 < System.currentTimeMillis()) {
			myApp.resetFsSession();
			myApp.clearCurrentShareDetails();
			Toast.makeText(myApp, "Unable to edit, session expired", Toast.LENGTH_SHORT).show();
		} else {
			Intent intent = new Intent(myApp, ShareCreateActivity.class);
			startActivity(intent);
		}
		finish();
	}

}
