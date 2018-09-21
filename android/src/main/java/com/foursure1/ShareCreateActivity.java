package com.foursure1;

import java.util.ArrayList;
import java.util.Random;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.foursure1.util.FactorType;
import com.foursure1.util.ShareType;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.MapsInitializer;
import com.liveensure.util.StringHelper;

public class ShareCreateActivity extends BaseActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	public static final String TAG = ShareCreateActivity.class.getSimpleName();
	private static final String FOURSURE_SHARE_PREFIX = "http://4shr.co/";
	private static final String FOURSURE_SHARE_PROMPT = "[enter message or link]";
	private static final int SHARE_UPDATE = 1;
	private static final int SHARE_DELETE = 2;
	private FourSureApplication myApp;
	protected String mCurrentAssetValue;
	private int mCurrentState;
	private ImageView mBtnBack;
	private ImageView mBtnNext;
	private EditText mTxtAssetValue;
	protected String mCurrentShieldValue;
	protected boolean mKnowledgeActive;
	protected boolean mLocationActive;
	private boolean mTimeActive;
	private boolean mBehaviorActive;
	protected boolean mShieldActive;
	protected boolean mTrackActive;
	private Random mRandom = new Random();
	// reddit, bulldog outed
	private String[] mShieldURLs = { "https://www.reddit.com/new/", "https://www.youtube.com/watch?v=E1d_dmKBiiI" };
	private ImageView mBtnOptionShield;
	private ImageView mBtnOptionTrack;
	private ImageView mBtnFactorKnowledge;
	private ImageView mBtnFactorLocation;
	private ArrayList<FlatButton> buttons = new ArrayList<FlatButton>();
	private FlatButton mFlatButtonKnowledge;
	private FlatButton mFlatButtonLocation;
	private FourSureFSM mFSM;
	private LinearLayout mLytDetailsKnowledge;
	private ImageView mBtnKnowledgeCancel;
	private ImageView mBtnKnowledgeSave;
	protected FactorType mCurrentFactorType;
	protected String mCurrentFactorDetails;
	private EditText mTxtDetailsKnowledge;
	private LinearLayout mLytDetailsLocation;
	private ImageView mBtnLocationSave;
	private ImageView mBtnLocationCancel;
	private RadioGroup mLocationRadioGroup;
	private static final int TWO_MINUTES_MS = 1000 * 60 * 2; //
	private static final long LOC_UPDATE_MS = 4000; // 4 seconds
	private static final long LOC_FASTEST_INTERVAL_MS = 1000; // 1 second
	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	protected String mCurrentRadius;
	protected String mCurrentExpireSeconds;
	private ImageView mBtnHelp;
	private boolean mEditingExistingShare;
	private String mCurrentLocationDetails;
	private ImageView mBtnFactorTime;
	private ImageView mBtnFactorBehavior;
	private FlatButton mFlatButtonTime;
	private FlatButton mFlatButtonBehavior;
	private RadioGroup mTimeRadioGroup;
	private LinearLayout mLytDetailsTime;
	private ImageView mBtnTimeSave;
	private ImageView mBtnTimeCancel;
	private LinearLayout mLytDetailsBehaviorGrid;
	private RelativeLayout mLytDetailsBehaviorButtons;
	protected String mCurrentBehavior;
	private boolean[] mTouchRegionsEnabled;
	private int[] activeTouchQueue;
	private View touchPane1;
	private View touchPane2;
	private View touchPane3;
	private View touchPane4;
	private View touchPane5;
	private View touchPane6;
	private ImageView mBtnBehaviorSave;
	private ImageView mBtnBehaviorCancel;

	// For Location permission
	private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myApp = (FourSureApplication) getApplication();
		myApp.getAppPrefs();
		mFSM = myApp.getFSM();
		mFSM.setUI(TAG, this);
		checkGooglePlayServices();
		setContentView(R.layout.activity_create_share);
		activeTouchQueue = new int[2];
		mTouchRegionsEnabled = new boolean[7];


		if (checkLocationPermission()){
			checkGPS();
		}

		if (myApp.isGoogleServicesAvailable()) {
			try {
				MapsInitializer.initialize(this);
			} catch (GooglePlayServicesNotAvailableException e) {
				Log.e(TAG, "Exception during MapsInitializer.initialize()", e);
			}

			// Initialize the location service for location challenges
			initLocationService();
		} else {
			Log.w(TAG, "looks like google play services is not available");
		}

		mTxtAssetValue = (EditText) findViewById(R.id.share_asset_value);

		// Knowledge settings pane
		mLytDetailsKnowledge = (LinearLayout) findViewById(R.id.lyt_set_knowledge_answer);
		mBtnKnowledgeSave = (ImageView) findViewById(R.id.details_knowledge_save);
		mBtnKnowledgeCancel = (ImageView) findViewById(R.id.details_knowledge_cancel);
		mTxtDetailsKnowledge = (EditText) findViewById(R.id.txt_details_knowledge);

		// Location settings pane
		mLytDetailsLocation = (LinearLayout) findViewById(R.id.lyt_set_location_answer);
		mBtnLocationSave = (ImageView) findViewById(R.id.details_location_save);
		mBtnLocationCancel = (ImageView) findViewById(R.id.details_location_cancel);

		// Time settings pane
		mLytDetailsTime = (LinearLayout) findViewById(R.id.lyt_set_time_answer);
		mBtnTimeSave = (ImageView) findViewById(R.id.details_time_save);
		mBtnTimeCancel = (ImageView) findViewById(R.id.details_time_cancel);

		// Behavior settings pane
		mLytDetailsBehaviorGrid = (LinearLayout) findViewById(R.id.behavior_challenge_grid);
		mLytDetailsBehaviorButtons = (RelativeLayout) findViewById(R.id.lyt_set_behavior_buttons);
		mBtnBehaviorSave = (ImageView) findViewById(R.id.details_behavior_save);
		mBtnBehaviorCancel = (ImageView) findViewById(R.id.details_behavior_cancel);

		// Navigation buttons
		mBtnBack = (ImageView) findViewById(R.id.btn_share_back);
		mBtnNext = (ImageView) findViewById(R.id.share_create);
		mBtnHelp = (ImageView) findViewById(R.id.btn_share_help);

		// Factor buttons
		mBtnFactorKnowledge = (ImageView) findViewById(R.id.share_factor_knowledge);
		mBtnFactorLocation = (ImageView) findViewById(R.id.share_factor_location);
		mBtnFactorTime = (ImageView) findViewById(R.id.share_factor_time);
		mBtnFactorBehavior = (ImageView) findViewById(R.id.share_factor_behavior);

		// Share option buttons
		mBtnOptionShield = (ImageView) findViewById(R.id.share_option_shield);
		mBtnOptionTrack = (ImageView) findViewById(R.id.share_option_track);

		// Create the flatbutton objects for the shares (behave as a button group)
		Resources res = myApp.getResources();
		mFlatButtonKnowledge = new FlatButton(mBtnFactorKnowledge, res.getString(R.string.chal_knowledge), FactorType.KNOWLEDGE, R.drawable.icon_knowledge,
				R.drawable.icon_knowledge_circle);
		mFlatButtonLocation = new FlatButton(mBtnFactorLocation, res.getString(R.string.chal_location), FactorType.LOCATION, R.drawable.icon_location,
				R.drawable.icon_location_circle);
		mFlatButtonTime = new FlatButton(mBtnFactorTime, res.getString(R.string.chal_time), FactorType.TIME, R.drawable.icon_time, R.drawable.icon_time_circle);
		mFlatButtonBehavior = new FlatButton(mBtnFactorBehavior, res.getString(R.string.chal_behavior), FactorType.BEHAVIOR, R.drawable.icon_behavior,
				R.drawable.icon_behavior_circle);
		buttons.add(mFlatButtonKnowledge);
		buttons.add(mFlatButtonLocation);
		buttons.add(mFlatButtonTime);
		buttons.add(mFlatButtonBehavior);
		buttons.add(new FlatButton(mBtnOptionShield, res.getString(R.string.shield), FactorType.BEHAVIOR, R.drawable.icon_shield, R.drawable.icon_shield_circle));
		buttons.add(new FlatButton(mBtnOptionTrack, res.getString(R.string.track), FactorType.KNOWLEDGE, R.drawable.icon_track, R.drawable.icon_track_circle));

		mBtnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				myApp.resetFsSession();
				myApp.clearCurrentShareDetails();
				finish();
			}
		});
		mBtnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callShareUpdate();
			}
		});
		// mTxtAssetValue.setLongClickable(true);
		// mTxtAssetValue.setOnLongClickListener(new OnLongClickListener() {
		//
		// @Override
		// public boolean onLongClick(View v) {
		// mTxtAssetValue.setText("http://www.liveensure.com");
		// return true;
		// }
		// });
		for (FlatButton b : buttons) {
			b.getView().setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					handleButtonTap(v);
				}
			});
		}
		mTxtAssetValue.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (FOURSURE_SHARE_PROMPT.equals(mTxtAssetValue.getText().toString()))
					mTxtAssetValue.setText(""); // clear input if it's the default message
			}
		});
		;
		mBtnKnowledgeSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mTxtDetailsKnowledge.getText().toString().length() > 0) {
					mCurrentFactorDetails = mTxtDetailsKnowledge.getText().toString();
					mCurrentFactorType = FactorType.KNOWLEDGE;
					mLytDetailsKnowledge.setVisibility(View.INVISIBLE);
				} else {
					Toast.makeText(getApplicationContext(), "Please enter a secret value", Toast.LENGTH_SHORT).show();
				}
			}
		});
		mBtnKnowledgeCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLytDetailsKnowledge.setVisibility(View.INVISIBLE);
				// turn off knowledge button
				for (FlatButton b : buttons) {
					if (b.getName().equals(myApp.getResources().getString(R.string.chal_knowledge))) {
						b.setActive(false);
						((TransitionDrawable) b.getView().getDrawable()).resetTransition();
					}
				}
			}
		});
		mBtnLocationSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (StringHelper.isEmpty(mCurrentRadius)) {
					Toast.makeText(getApplicationContext(), "Please choose a location", Toast.LENGTH_SHORT).show();
				} else {
					mCurrentFactorType = FactorType.LOCATION;
					mLytDetailsLocation.setVisibility(View.INVISIBLE);
				}
			}
		});
		mBtnLocationCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLytDetailsLocation.setVisibility(View.INVISIBLE);
				// turn off knowledge button
				for (FlatButton b : buttons) {
					if (b.getName().equals(myApp.getResources().getString(R.string.chal_location))) {
						b.setActive(false);
						((TransitionDrawable) b.getView().getDrawable()).resetTransition();
					}
				}
			}
		});

		mLocationRadioGroup = (RadioGroup) findViewById(R.id.radioLoc);
		mLocationRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radioHere) {
					mCurrentRadius = "8";
				} else if (checkedId == R.id.radioNearby) {
					mCurrentRadius = "80";
				} else if (checkedId == R.id.radioRegion) {
					mCurrentRadius = "800";
				} else if (checkedId == R.id.radioAnywhere) {
					mCurrentRadius = "8000000";
				} else {
					mCurrentRadius = null;
				}
			}
		});

		mBtnTimeSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentFactorType = FactorType.TIME;
				mLytDetailsTime.setVisibility(View.INVISIBLE);
			}
		});
		mBtnTimeCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLytDetailsTime.setVisibility(View.INVISIBLE);
				// turn off time button
				for (FlatButton b : buttons) {
					if (b.getName().equals(myApp.getResources().getString(R.string.chal_time))) {
						b.setActive(false);
						((TransitionDrawable) b.getView().getDrawable()).resetTransition();
					}
				}
				// TODO: possibly reset the chosen factor to the previously active one like Marc does in ios
			}
		});

		mTimeRadioGroup = (RadioGroup) findViewById(R.id.radioTime);
		mTimeRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radioOneMinute) {
					mCurrentExpireSeconds = "60";
				} else if (checkedId == R.id.radioOneHour) {
					mCurrentExpireSeconds = "3600";
				} else if (checkedId == R.id.radioOneDay) {
					mCurrentExpireSeconds = "86400";
				} else if (checkedId == R.id.radioOneWeek) {
					mCurrentExpireSeconds = "604800";
				} else {
					mCurrentExpireSeconds = null;
				}
			}
		});

		mBtnFactorBehavior.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleButtonTap(v);
			}
		});
		mBtnBehaviorSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (StringHelper.isEmpty(mCurrentBehavior)) {
					// Toast.makeText(getApplicationContext(), "Please choose a behavior", Toast.LENGTH_SHORT).show();
					mCurrentFactorType = FactorType.NONE;
					// turn off behavior button
					for (FlatButton b : buttons) {
						if (b.getName().equals(myApp.getResources().getString(R.string.chal_behavior))) {
							b.setActive(false);
							((TransitionDrawable) b.getView().getDrawable()).resetTransition();
						}
					}
				} else {
					mCurrentFactorType = FactorType.BEHAVIOR;
					mCurrentBehavior = "-1::" + mCurrentBehavior;
				}
				mLytDetailsBehaviorButtons.setVisibility(View.INVISIBLE);
				mLytDetailsBehaviorGrid.setVisibility(View.GONE);
			}
		});
		mBtnBehaviorCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLytDetailsBehaviorButtons.setVisibility(View.INVISIBLE);
				mLytDetailsBehaviorGrid.setVisibility(View.GONE);
				// turn off behavior button
				for (FlatButton b : buttons) {
					if (b.getName().equals(myApp.getResources().getString(R.string.chal_behavior))) {
						b.setActive(false);
						((TransitionDrawable) b.getView().getDrawable()).resetTransition();
					}
				}
			}
		});

		setUpTouchRegionListeners();
	}

	protected void deleteShare() {
		// check for an expired session
		if (myApp.getFsSessionExpiration() < System.currentTimeMillis()) {
			myApp.resetFsSession();
			myApp.clearCurrentShareDetails();
			Toast.makeText(myApp, "Unable to delete share, session expired", Toast.LENGTH_SHORT).show();
			finish();
		}

		new AlertDialog.Builder(this).setTitle("Delete Share").setMessage("Do you want to delete this share?").setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {
						mCurrentState = SHARE_DELETE;
						mFSM.callShareUpdate(true);
					}
				}).setNegativeButton(android.R.string.no, null).show();
	}

	public boolean checkLocationPermission() {
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.ACCESS_FINE_LOCATION)) {

				// Show an explanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
				new AlertDialog.Builder(this)
						.setTitle(R.string.title_location_permission)
						.setMessage(R.string.text_location_permission)
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								//Prompt the user once explanation has been shown
								ActivityCompat.requestPermissions(ShareCreateActivity.this,
										new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
										MY_PERMISSIONS_REQUEST_LOCATION);
							}
						})
						.create()
						.show();


			} else {
				// No explanation needed, we can request the permission.
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						MY_PERMISSIONS_REQUEST_LOCATION);
			}
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_LOCATION: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// permission was granted, yay! Do the
					// location-related task you need to do.
					if (ContextCompat.checkSelfPermission(this,
							Manifest.permission.ACCESS_FINE_LOCATION)
							== PackageManager.PERMISSION_GRANTED) {
						checkGPS();
//						startTimer();

						//Request location updates:
//                        locationManager.requestLocationUpdates(provider, 400, 1, this);
					}

				}
				return;
			}

		}
	}

	private void checkGPS() {
		LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!statusOfGPS) {
			new android.support.v7.app.AlertDialog.Builder(this)
					.setTitle(R.string.enable_gps)
					.setMessage(R.string.tirn_on_gps)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							//Prompt the user once explanation has been shown
							Intent goToSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(goToSettingIntent);
						}
					})
					.create()
					.show();
		}
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "in onResume()");
		super.onResume();
		mFSM = myApp.getFSM();
		mFSM.setUI(TAG, this);

		// TODO: talk with Marc about the possibility of being sent to LE to authenticate from this activity. if so, handle IN_LIVEENSURE_SESSION state as well
		if (mCurrentState == SHARE_DELETE) {
			finish();
		} else {
			resetUI();
		}
		if (mLocationClient != null) {
			mLocationClient.connect();
		}
	}

	/**
	 * called when the app is backgrounded (but not ended)
	 */
	@Override
	public void onPause() {
		Log.w(TAG, "in onPause()");
		super.onPause();
		if (mLocationClient != null)
			mLocationClient.disconnect(); // stop listening to location updates if we're paused
	}

	private void resetUI() {
		mLytDetailsKnowledge.setVisibility(View.INVISIBLE);
		mCurrentAssetValue = myApp.getActivatedShareAsset();
		mCurrentFactorType = myApp.getShareFactorType();
		mCurrentFactorDetails = myApp.getShareFactorDetails();
		mCurrentShieldValue = myApp.getActivatedShieldValue();
		mKnowledgeActive = (mCurrentFactorType == FactorType.KNOWLEDGE);
		mLocationActive = (mCurrentFactorType == FactorType.LOCATION);
		mTimeActive = (mCurrentFactorType == FactorType.TIME);
		mBehaviorActive = (mCurrentFactorType == FactorType.BEHAVIOR);
		if (mKnowledgeActive) {
			mTxtDetailsKnowledge.setText(mCurrentFactorDetails);
			// Log.e(TAG, "knowledge factor is active, setting factor details to " + mCurrentFactorDetails);
		}
		if (mLocationActive)
			mCurrentLocationDetails = mCurrentFactorDetails;
		if (mTimeActive)
			mCurrentExpireSeconds = mCurrentFactorDetails;
		if (mBehaviorActive)
			mCurrentBehavior = mCurrentFactorDetails;
		mShieldActive = myApp.isShareOptionShield();
		mTrackActive = myApp.isShareOptionTracking();
		mTxtAssetValue.setImeOptions(EditorInfo.IME_ACTION_DONE);
		// mTxtAssetValue.setHorizontallyScrolling(false);
		// mTxtAssetValue.setMaxLines(Integer.MAX_VALUE);
		mTxtAssetValue.setSingleLine();
		mTxtAssetValue.setEllipsize(TruncateAt.END);
		Resources res = myApp.getResources();
		for (FlatButton b : buttons) {
			b.setActive(false);
			if (b.getName().equals(res.getString(R.string.chal_knowledge)) && mKnowledgeActive)
				b.setActive(true);
			if (b.getName().equals(res.getString(R.string.chal_location)) && mLocationActive)
				b.setActive(true);
			if (b.getName().equals(res.getString(R.string.chal_time)) && mTimeActive)
				b.setActive(true);
			if (b.getName().equals(res.getString(R.string.chal_behavior)) && mBehaviorActive)
				b.setActive(true);
			if (b.getName().equals(res.getString(R.string.shield)) && mShieldActive)
				b.setActive(true);
			if (b.getName().equals(res.getString(R.string.track)) && mTrackActive)
				b.setActive(true);
		}
		// set active button to active image
		Drawable backgrounds[] = new Drawable[2];

		for (FlatButton b : buttons) {
			backgrounds[0] = res.getDrawable(b.getInactiveDrawableID());
			backgrounds[1] = res.getDrawable(b.getActiveDrawableID());
			TransitionDrawable crossfader = new TransitionDrawable(backgrounds);
			b.getView().setImageDrawable(crossfader);
			if (b.isActive()) {
				// active button
				crossfader.startTransition(300);
			} else {
				// inactive button
				crossfader.resetTransition();
			}
		}
		// check to see if we're editing an existing share or creating a new one
		mEditingExistingShare = (!StringHelper.isEmpty(myApp.getShortCode()));

		// set the text of the asset box
		String av = FOURSURE_SHARE_PROMPT;
		mCurrentAssetValue = "";

		if (mEditingExistingShare) {
			av = myApp.getActivatedShareAsset();
			mCurrentAssetValue = myApp.getActivatedShareAsset();

		} else {
			String clipData = getClipboardContents();
			if (!StringHelper.isEmpty(clipData)) {
				av = clipData;
				mCurrentAssetValue = clipData;
			}
		}
		mTxtAssetValue.setText(av);
		// set icon and action of bottom right button (trash/help)
		if (mEditingExistingShare) {
			mBtnHelp.setImageDrawable(res.getDrawable(R.drawable.trash_gray));
			mBtnHelp.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteShare();
				}
			});
		} else {
			mBtnHelp.setImageDrawable(res.getDrawable(R.drawable.help_gray));
			mBtnHelp.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					goToHelp();
				}
			});
		}

	}

	private void handleButtonTap(View v) {
		if (!(v instanceof ImageView))
			return;
		ImageView iv = (ImageView) v;
		Resources res = myApp.getResources();

		// update button state
		for (FlatButton b : buttons) {
			if (b.getView() == iv) {
				b.setActive(!b.isActive()); // toggle button state
				if (b.getName().equals(res.getString(R.string.chal_knowledge)) && b.isActive()) {
					mFlatButtonLocation.setActive(false);
					mFlatButtonTime.setActive(false);
					mFlatButtonBehavior.setActive(false);
				}
				if (b.getName().equals(res.getString(R.string.chal_location)) && b.isActive()) {
					mFlatButtonKnowledge.setActive(false);
					mFlatButtonTime.setActive(false);
					mFlatButtonBehavior.setActive(false);
				}
				if (b.getName().equals(res.getString(R.string.chal_time)) && b.isActive()) {
					mFlatButtonKnowledge.setActive(false);
					mFlatButtonLocation.setActive(false);
					mFlatButtonBehavior.setActive(false);
				}
				if (b.getName().equals(res.getString(R.string.chal_behavior)) && b.isActive()) {
					mFlatButtonKnowledge.setActive(false);
					mFlatButtonTime.setActive(false);
					mFlatButtonLocation.setActive(false);
				}
				if (b.getName().equals(res.getString(R.string.shield)))
					mShieldActive = b.isActive();
				if (b.getName().equals(res.getString(R.string.track)))
					mTrackActive = b.isActive();
				break;
			}
		}
		for (FlatButton b : buttons) {
			TransitionDrawable crossfader = (TransitionDrawable) b.getView().getDrawable();
			if (b.isActive()) {
				// set the button active
				if (b.getView() == iv) {
					crossfader.startTransition(250);
					if (b.getName().equals(res.getString(R.string.chal_knowledge))) {
						mLytDetailsKnowledge.setAlpha(0.0f);
						mLytDetailsKnowledge.setVisibility(View.VISIBLE);
						mLytDetailsKnowledge.animate().alpha(1.0f).setListener(null).start();

						mKnowledgeActive = true;
						mLocationActive = false;
						mTimeActive = false;
						mBehaviorActive = false;
					}
					if (b.getName().equals(res.getString(R.string.chal_location))) {
						mLytDetailsLocation.setAlpha(0.0f);
						mLytDetailsLocation.setVisibility(View.VISIBLE);
						mLytDetailsLocation.animate().alpha(1.0f).setListener(null).start();
						mLocationActive = true;
						mKnowledgeActive = false;
						mTimeActive = false;
						mBehaviorActive = false;
					}
					if (b.getName().equals(res.getString(R.string.chal_time))) {
						mLytDetailsTime.setAlpha(0.0f);
						mLytDetailsTime.setVisibility(View.VISIBLE);
						mLytDetailsTime.animate().alpha(1.0f).setListener(null).start();
						mTimeActive = true;
						mKnowledgeActive = false;
						mLocationActive = false;
						mBehaviorActive = false;
					}
					if (b.getName().equals(res.getString(R.string.chal_behavior))) {
						mLytDetailsBehaviorButtons.setAlpha(0.0f);
						mLytDetailsBehaviorButtons.setVisibility(View.VISIBLE);
						mLytDetailsBehaviorButtons.animate().alpha(1.0f).setListener(null).start();
						mLytDetailsBehaviorGrid.setVisibility(View.VISIBLE);
						mBehaviorActive = true;
						mTimeActive = false;
						mLocationActive = false;
						mKnowledgeActive = false;
					}
				}
			} else {
				// inactive button
				if (b.getView() == iv) {
					if (b.getName().equals(res.getString(R.string.chal_knowledge))) {
						mKnowledgeActive = false;
					}
					if (b.getName().equals(res.getString(R.string.chal_location))) {
						mLocationActive = false;
					}
					if (b.getName().equals(res.getString(R.string.chal_time))) {
						mTimeActive = false;
					}
					if (b.getName().equals(res.getString(R.string.chal_behavior))) {
						mBehaviorActive = false;
					}
				}
				crossfader.resetTransition();
			}
		}
	}

	private void callShareUpdate() {
		String av = mTxtAssetValue.getText().toString();
		if (StringHelper.isEmpty(av) || FOURSURE_SHARE_PROMPT.equals(av)) {
			Toast.makeText(this, "Enter a text or URL to share", Toast.LENGTH_SHORT).show();
			return;
		}
		// check for an expired session
		if (myApp.getFsSessionExpiration() < System.currentTimeMillis()) {
			myApp.resetFsSession();
			myApp.clearCurrentShareDetails();
			Toast.makeText(myApp, "Unable to save share, session expired", Toast.LENGTH_SHORT).show();
			finish();
		}

		mCurrentAssetValue = av;
		myApp.setActivatedShareAsset(mCurrentAssetValue);
		ShareType assetType = (mCurrentAssetValue.startsWith("http")) ? ShareType.URL : ShareType.TEXT;
		myApp.setActivatedShareAssetType(assetType);
		if (mShieldActive) {
			int randIndex = mRandom.nextInt(mShieldURLs.length);
			mCurrentShieldValue = mShieldURLs[randIndex];
			// mCurrentShieldValue = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"; // when am I gonna give you up?
		} else {
			mCurrentShieldValue = "";
		}
		myApp.setActivatedShieldValue(mCurrentShieldValue);
		myApp.setShareFactorType(FactorType.NONE);
		myApp.setShareFactorDetails("");
		if (mKnowledgeActive) {
			myApp.setShareFactorType(FactorType.KNOWLEDGE);
			myApp.setShareFactorDetails(mCurrentFactorDetails);
		}
		if (mLocationActive && !StringHelper.isEmpty(mCurrentLocationDetails)) {
			myApp.setShareFactorType(FactorType.LOCATION);
			myApp.setShareFactorDetails(mCurrentLocationDetails);
		}
		if (mTimeActive && !StringHelper.isEmpty(mCurrentExpireSeconds)) {
			myApp.setShareFactorType(FactorType.TIME);
			myApp.setShareFactorDetails(mCurrentExpireSeconds);
		}
		if (mBehaviorActive && !StringHelper.isEmpty(mCurrentBehavior)) {
			myApp.setShareFactorType(FactorType.BEHAVIOR);
			myApp.setShareFactorDetails(mCurrentBehavior);
		}
		myApp.setShareOptionTracking(mTrackActive);
		myApp.setShareOptionShield(mShieldActive);
		mCurrentState = SHARE_UPDATE;
		mFSM.callShareUpdate(false);
	}

	@Override
	public void showSuccess(String msg) {
		if (mCurrentState == SHARE_UPDATE) {
			// FSM says share was created. switch to share success activity
			Intent intent = new Intent(myApp, ShareSuccessActivity.class);
			startActivity(intent);
			finish();
		} else if (mCurrentState == SHARE_DELETE) {
			// FSM says share was deleted. clear state and finish, should take user back to front page
			myApp.resetFsSession();
			myApp.clearCurrentShareDetails();
			finish();
		}
	}

	@Override
	public void handleError(String msg) {
		Toast.makeText(this, "Error when updating share", Toast.LENGTH_SHORT).show();
	}

	protected void goToHelp() {
		Uri webpage = Uri.parse(FourSureApplication.FOURSHARE_HELP_URL);
		Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivity(intent);
		} else {
			Toast.makeText(getApplicationContext(), "Unable to find a handler for " + FourSureApplication.FOURSHARE_HELP_URL, Toast.LENGTH_LONG).show();
		}
	}

	private String getClipboardContents() {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		String clipData = "";
		if (!(clipboard.hasPrimaryClip())) {
			// Nothing currently in the clipboard
			Log.e(TAG, "nothing in clipboard");
			return clipData;
		} else if (!(clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))) {
			// something in the clipboard, but it's not text (could be a picture, etc)
			Log.e(TAG, "something in clipboard, but it's not text");
			return clipData;
		} else {
			ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
			if (item != null && item.getText() != null) {
				clipData = item.getText().toString();
				if (clipData != null && clipData.length() > 0 && clipData.startsWith(FOURSURE_SHARE_PREFIX)) {
					Log.e(TAG, "text in clipboard, but it's a 4shr.co url, so clearing it out");
					clipData = "";
				}
			}
			Log.e(TAG, "retrieved clipboard text '" + clipData + "'");
		}
		return clipData;
	}

	/**
	 * set up (but do not start) the location service objects needed to listen to core location updates from the OS
	 */
	private void initLocationService() {
		Log.w(TAG, "in initLocationService()");
		int gPlayAvail = GooglePlayServicesUtil.isGooglePlayServicesAvailable(myApp);
		if (gPlayAvail == ConnectionResult.SUCCESS) {
			// Log.d(TAG, "Google Play Service is available, enabling location reporting");
		} else {
			Log.e(TAG, "Google Play Service is not available, no location info.  Code: " + gPlayAvail);
			GooglePlayServicesUtil.getErrorDialog(gPlayAvail, this, 1122).show();
			return;
		}

		// create a location request object to specify accuracy and frequency of updates
		mLocationRequest = LocationRequest.create();
		// mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setInterval(LOC_UPDATE_MS);
		mLocationRequest.setFastestInterval(LOC_FASTEST_INTERVAL_MS);

		// create the location client
		mLocationClient = new LocationClient(myApp, this, this);

		// all ready to go. we'll connect and start actually listening in the onResume() method
	}

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES_MS;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES_MS;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	// Google play services callbacks
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.w(TAG, "google play services connect failed.  unable to get location updates.");
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.i(TAG, "google play services connected.  starting to listen for location updates now");
		if (mLocationClient != null)
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	@Override
	public void onDisconnected() {
		Log.w(TAG, "google play services discconnected.  unable to get location updates.");
	}

	@Override
	public void onLocationChanged(Location location) {
		// update our current notion of location
		if (isBetterLocation(location, mCurrentLocation)) {
			Log.i(TAG, "Updating current location:\naccuracy: " + location.getAccuracy() + "\nlat: " + location.getLatitude() + "\nlong: " + location.getLongitude());
			mCurrentLocation = location;
			mCurrentLocationDetails = mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude() + "," + mCurrentRadius;
		}
		Log.d(TAG, "got a location update and in the location challenge. setting location answer");
	}

	boolean checkGooglePlayServices() {
		int googlePlayRequestID = 1;
		try {
			int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
			if (status == ConnectionResult.SUCCESS) {
				Log.i(TAG, "Google Play Services is installed and up to date");
				myApp.setGoogleServicesAvailable(true);
				return true;
			} else {
				Log.w(TAG, "GooglePlayServicesUtil Connection Result: " + status);
				myApp.setGoogleServicesAvailable(false);
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

	@SuppressLint("ClickableViewAccessibility")
	private void setUpTouchRegionListeners() {

		touchPane1 = (View) findViewById(R.id.behavior_challenge_cell_1);
		touchPane1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (MotionEvent.ACTION_UP == e.getAction())
					handleGridTouch(v);
				return true;
			}
		});

		touchPane2 = (View) findViewById(R.id.behavior_challenge_cell_2);
		touchPane2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (MotionEvent.ACTION_UP == e.getAction())
					handleGridTouch(v);
				return true;
			}
		});

		touchPane3 = (View) findViewById(R.id.behavior_challenge_cell_3);
		touchPane3.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (MotionEvent.ACTION_UP == e.getAction())
					handleGridTouch(v);
				return true;
			}
		});

		touchPane4 = (View) findViewById(R.id.behavior_challenge_cell_4);
		touchPane4.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (MotionEvent.ACTION_UP == e.getAction())
					handleGridTouch(v);
				return true;
			}
		});

		touchPane5 = (View) findViewById(R.id.behavior_challenge_cell_5);
		touchPane5.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (MotionEvent.ACTION_UP == e.getAction())
					handleGridTouch(v);
				return true;
			}
		});

		touchPane6 = (View) findViewById(R.id.behavior_challenge_cell_6);
		touchPane6.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (MotionEvent.ACTION_UP == e.getAction())
					handleGridTouch(v);
				return true;
			}
		});

	}

	protected void refreshTouchGrid() {
		Resources res = myApp.getResources();
		if (activeTouchQueue[0] == 1 || activeTouchQueue[1] == 1) {
			// turn on region 1
			touchPane1.setBackground(res.getDrawable(R.drawable.stipple_touch));
			mTouchRegionsEnabled[1] = true;
		} else {
			// turn off region 1
			touchPane1.setBackgroundColor(Color.TRANSPARENT);
			mTouchRegionsEnabled[1] = false;
		}
		if (activeTouchQueue[0] == 2 || activeTouchQueue[1] == 2) {
			// turn on region 2
			touchPane2.setBackground(res.getDrawable(R.drawable.stipple_touch));
			mTouchRegionsEnabled[2] = true;
		} else {
			// turn off region 2
			touchPane2.setBackgroundColor(Color.TRANSPARENT);
			mTouchRegionsEnabled[2] = false;
		}
		if (activeTouchQueue[0] == 3 || activeTouchQueue[1] == 3) {
			// turn on region 3
			touchPane3.setBackground(res.getDrawable(R.drawable.stipple_touch));
			mTouchRegionsEnabled[3] = true;
		} else {
			// turn off region 3
			touchPane3.setBackgroundColor(Color.TRANSPARENT);
			mTouchRegionsEnabled[3] = false;
		}
		if (activeTouchQueue[0] == 4 || activeTouchQueue[1] == 4) {
			// turn on region 4
			touchPane4.setBackground(res.getDrawable(R.drawable.stipple_touch));
			mTouchRegionsEnabled[4] = true;
		} else {
			// turn off region 4
			touchPane4.setBackgroundColor(Color.TRANSPARENT);
			mTouchRegionsEnabled[4] = false;
		}
		if (activeTouchQueue[0] == 5 || activeTouchQueue[1] == 5) {
			// turn on region 5
			touchPane5.setBackground(res.getDrawable(R.drawable.stipple_touch));
			mTouchRegionsEnabled[5] = true;
		} else {
			// turn off region 5
			touchPane5.setBackgroundColor(Color.TRANSPARENT);
			mTouchRegionsEnabled[5] = false;
		}
		if (activeTouchQueue[0] == 6 || activeTouchQueue[1] == 6) {
			// turn on region 6
			touchPane6.setBackground(res.getDrawable(R.drawable.stipple_touch));
			mTouchRegionsEnabled[6] = true;
		} else {
			// turn off region 6
			touchPane6.setBackgroundColor(Color.TRANSPARENT);
			mTouchRegionsEnabled[6] = false;
		}
		mCurrentBehavior = "";

		for (int x = 1; x < mTouchRegionsEnabled.length; x++) {
			if (mTouchRegionsEnabled[x]) {
				if (mCurrentBehavior.length() > 0)
					mCurrentBehavior = mCurrentBehavior + "," + String.valueOf(x);
				else
					mCurrentBehavior = String.valueOf(x);
			}
		}
		if (mCurrentBehavior.length() == 0)
			mCurrentBehavior = "0"; // no touches at all == "0"

		// if (activeTouchQueue[0] > 0)
		// mCurrentBehavior = "" + activeTouchQueue[0];
		// if (activeTouchQueue[1] > 0)
		// mCurrentBehavior += "," + activeTouchQueue[1];
		// // Log.e(TAG, "after refreshing touches, behavior pattern is " + mCurrentBehavior);
	}

	protected void dequeue(int i) {
		if (i == 0)
			return;
		if (activeTouchQueue[0] == i) {
			activeTouchQueue[0] = activeTouchQueue[1];
			activeTouchQueue[1] = 0;
		} else if (activeTouchQueue[1] == i) {
			activeTouchQueue[1] = 0;
		}
	}

	protected void enqueue(int i) {
		if (i == 0)
			return;
		activeTouchQueue[1] = activeTouchQueue[0];
		activeTouchQueue[0] = i;
	}

	private void handleGridTouch(View v) {
		int index = 0;
		if (v == touchPane1)
			index = 1;
		if (v == touchPane2)
			index = 2;
		if (v == touchPane3)
			index = 3;
		if (v == touchPane4)
			index = 4;
		if (v == touchPane5)
			index = 5;
		if (v == touchPane6)
			index = 6;
		mTouchRegionsEnabled[index] = !mTouchRegionsEnabled[index];
		if (mTouchRegionsEnabled[index]) {
			// user just turned this spot on. push it on the queue
			enqueue(index);
		} else {
			// user just turned off this spot, remove it from the queue
			dequeue(index);
		}
		refreshTouchGrid();
	}

}
