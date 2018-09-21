package com.foursure1;

import java.util.ArrayList;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.foursure1.util.FactorType;
import com.foursure1.util.HandleType;
import com.liveensure.a.mini.MiniLiveEnsureActivity;
import com.liveensure.util.StringHelper;

public class SettingsActivity extends BaseActivity {

	public static final String TAG = SettingsActivity.class.getSimpleName();
	private ImageView mBtnWearable;
	private ImageView mBtnPin;
	private FourSureApplication myApp;
	protected String mUserName;
	private ImageView mBtnSave;
	private EditText mTxtUserID;
	private HandleType mUserIdType;
	private ImageView mBtnCancel;
	private ArrayList<FlatButton> buttons = new ArrayList<FlatButton>();
	private FourSureFSM mFSM;
	private int mCurrentState;
	private LinearLayout mLytDetailsWearable;
	private ImageView mBtnWearableSave;
	private ImageView mBtnWearableCancel;
	protected String mCurrentWearableType;
	protected FactorType mCurrentFactorType;
	private RadioGroup mWearableRadioGroup;
	private FlatButton mFlatButtonWearable;
	private FlatButton mFlatButtonPin;
	private static final int NORMAL_SETTINGS = 1;
	private static final int IN_LIVEENSURE_SESSION = 3;
	private static final int SESSION_STATUS = 4;
	private RelativeLayout mLytWindowShade;
	private TextView mLblConnectID;
	private LinearLayout mLytDetailsPin;
	private ImageView mBtnPinCancel;
	private ImageView mBtnPinSave;
	private int mPinLength = 4;
	private TextView[] mDigitInputs;
	private StringBuffer mEnteredPin;
	private int mCurrentDigit;
	private String[] mNumPadArray = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "del" };
	private TextView mBtnSwapStack;
	protected String currentHost;
	private ImageView mBtnHelp;
	private boolean mAllowSwitchStack = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myApp = (FourSureApplication) getApplication();
		mFSM = myApp.getFSM();
		mFSM.setUI(TAG, this);
		myApp.getAppPrefs();
		setContentView(R.layout.activity_settings);

		// set variables for UI elements
		mLblConnectID = (TextView) findViewById(R.id.lbl_connect_id);
		mBtnSwapStack = (TextView) findViewById(R.id.btn_swap_stacks);
		mTxtUserID = (EditText) findViewById(R.id.new_user_id);
		mBtnWearable = (ImageView) findViewById(R.id.img_btn_factor_wearable);
		mBtnPin = (ImageView) findViewById(R.id.img_btn_factor_pin);
		mBtnCancel = (ImageView) findViewById(R.id.settings_cancel);
		mBtnHelp = (ImageView) findViewById(R.id.settings_help);
		mBtnSave = (ImageView) findViewById(R.id.settings_save);
		mCurrentState = NORMAL_SETTINGS;

		// Make sure the windows shade is gone
		mLytWindowShade = (RelativeLayout) findViewById(R.id.lyt_settings_windowshade);
		mLytWindowShade.setVisibility(View.GONE);

		// Wearable settings pane
		mLytDetailsWearable = (LinearLayout) findViewById(R.id.lyt_set_wearable_answer);
		mBtnWearableSave = (ImageView) findViewById(R.id.details_wearable_save);
		mBtnWearableCancel = (ImageView) findViewById(R.id.details_wearable_cancel);
		mWearableRadioGroup = (RadioGroup) findViewById(R.id.radioWearable);

		// pin settings pane
		mLytDetailsPin = (LinearLayout) findViewById(R.id.lyt_set_pin_answer);
		mBtnPinSave = (ImageView) findViewById(R.id.details_pin_save);
		mBtnPinCancel = (ImageView) findViewById(R.id.details_pin_cancel);

		// set up button array
		Resources res = myApp.getResources();
		mFlatButtonWearable = new FlatButton(mBtnWearable, res.getString(R.string.chal_wearable), FactorType.WEARABLE, R.drawable.icon_wearable, R.drawable.icon_wearable_circle);
		buttons.add(mFlatButtonWearable);
		mFlatButtonPin = new FlatButton(mBtnPin, res.getString(R.string.chal_pin), FactorType.PIN, R.drawable.icon_pin, R.drawable.icon_pin_circle);
		buttons.add(mFlatButtonPin);

		// set handlers for various buttons
		mBtnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mBtnHelp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri webpage = Uri.parse(FourSureApplication.FOURSHARE_HELP_URL);
				Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
				if (intent.resolveActivity(getPackageManager()) != null) {
					startActivity(intent);
				} else {
					Toast.makeText(getApplicationContext(), "Unable to find a handler for " + FourSureApplication.FOURSHARE_HELP_URL, Toast.LENGTH_LONG).show();
				}
			}
		});
		mBtnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// hide keyboard
				InputMethodManager inputManager = (InputMethodManager) myApp.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				mUserName = mTxtUserID.getText().toString();
				if (mUserName != null)
					mUserName = mUserName.trim();
				if (StringHelper.isEmpty(mUserName) || !isValidEmail(mUserName)) {
					Toast.makeText(getApplicationContext(), "Enter a valid email address", Toast.LENGTH_SHORT).show();
					bounceTextEntry();
					return;
				}
				saveSettings();
			}
		});
		mLblConnectID.setLongClickable(true);
		mLblConnectID.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				PackageInfo pInfo;
				String version = "Unknown";
				try {
					pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
					version = pInfo.versionName;
				} catch (NameNotFoundException e) {
					version = "Unknown";
				}
				currentHost = myApp.getApiHost();
				if (currentHost.contains("fs2.mastlabs")) {
					version += " (fs2)";
				} else {
					version += " (api)";
				}

				Toast.makeText(getApplicationContext(), "App version " + version, Toast.LENGTH_LONG).show();
				return true;
			}
		});
		mBtnSwapStack.setLongClickable(true);
		mBtnSwapStack.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				swapStack();
				return true;
			}
		});
		mBtnWearable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleButtonTap(v);
			}
		});

		mBtnWearableSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (StringHelper.isEmpty(mCurrentWearableType)) {
					Toast.makeText(getApplicationContext(), "Please choose a wearable", Toast.LENGTH_SHORT).show();
				} else {
					if (mCurrentWearableType.equals("none")) {
						mCurrentFactorType = FactorType.NONE;
						// turn off wearable button
						for (FlatButton b : buttons) {
							if (b.getName().equals(myApp.getResources().getString(R.string.chal_wearable))) {
								b.setActive(false);
								((TransitionDrawable) b.getView().getDrawable()).resetTransition();
							}
						}
					} else {
						mCurrentFactorType = FactorType.WEARABLE;
					}
					mLytDetailsWearable.setVisibility(View.INVISIBLE);
				}
			}
		});
		mBtnWearableCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLytDetailsWearable.setVisibility(View.INVISIBLE);
				// turn off wearable button
				for (FlatButton b : buttons) {
					if (b.getName().equals(myApp.getResources().getString(R.string.chal_wearable))) {
						b.setActive(false);
						((TransitionDrawable) b.getView().getDrawable()).resetTransition();
					}
				}
			}
		});

		mCurrentWearableType = "none"; // TODO: pull wearable type from stored
										// app prefs
		if (mCurrentFactorType == FactorType.WEARABLE)
			mCurrentWearableType = "pebble";

		mWearableRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radioPebbleClassic) {
					mCurrentWearableType = "pebble";
				} else if (checkedId == R.id.radioPebbleTime) {
					mCurrentWearableType = "pebble";
				} else if (checkedId == R.id.radioSamsungGear) {
					mCurrentWearableType = "pebble";
				} else {
					mCurrentWearableType = null;
				}
			}
		});
		mBtnPin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setUpPinGrid();
				handleButtonTap(v);
			}
		});
		mBtnPinSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String pin = mEnteredPin.toString();
				if (pin == null || pin.length() < 4) {
					Toast.makeText(getApplicationContext(), "PIN must be 4 digits long", Toast.LENGTH_SHORT).show();
					return;
				} else {
					mCurrentFactorType = FactorType.PIN;
				}
				mLytDetailsPin.setVisibility(View.INVISIBLE);
			}
		});
		mBtnPinCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLytDetailsPin.setVisibility(View.INVISIBLE);
				mCurrentFactorType = FactorType.NONE;
				// turn off behavior button
				for (FlatButton b : buttons) {
					if (b.getName().equals(myApp.getResources().getString(R.string.chal_pin))) {
						b.setActive(false);
						((TransitionDrawable) b.getView().getDrawable()).resetTransition();
					}
				}
			}
		});
	}

	protected void swapStack() {
		if (!mAllowSwitchStack)
			return;
		currentHost = myApp.getApiHost();
		if (currentHost.contains("fs2.mastlabs")) {
			currentHost = "https://fs.mastlabs.com/fs/api/v1";
		} else {
			currentHost = "https://fs2.mastlabs.com/fs/api/v1";
		}
		new AlertDialog.Builder(this).setTitle("Change Host").setMessage("Change your API host to\n" + currentHost + "?").setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {
						myApp.setApiHost(currentHost);
						myApp.setDeviceUUID("");
						myApp.resetFsSession();
						myApp.saveAppPrefs();
						myApp.getFSM().resetState();
						myApp.clearCurrentShareDetails();
						Toast.makeText(getApplicationContext(), "Remember to re-register", Toast.LENGTH_LONG).show();
						finish();
					}
				}).setNegativeButton(android.R.string.no, null).show();

	}

	protected void bounceTextEntry() {
		// bounce the settings icon
		View v = mTxtUserID;
		AnimatorSet as = new AnimatorSet();
		float scaleAmount = 0.05f;
		as.playSequentially(ObjectAnimator.ofFloat(v, "scaleX", 1.0f + scaleAmount), ObjectAnimator.ofFloat(v, "scaleX", 1.0f), ObjectAnimator.ofFloat(v, "scaleX", 1.0f + (scaleAmount / 2.0f)),
				ObjectAnimator.ofFloat(v, "scaleX", 1.0f));
		as.setDuration(200);
		as.start();
	}

	protected void saveSettings() {
		// get the current ID
		mUserName = mTxtUserID.getText().toString();
		if (mUserName != null)
			mUserName = mUserName.trim();
		myApp.setUserName(mUserName);
		myApp.setUserIdType(mUserIdType);
		String details = "";
		if (mCurrentFactorType == FactorType.WEARABLE)
			details = mCurrentWearableType;
		if (mCurrentFactorType == FactorType.PIN && mEnteredPin != null && mEnteredPin.length() > 0)
			details = mEnteredPin.toString();
		myApp.setUserFactorType(mCurrentFactorType);
		myApp.setUserFactorDetails(details);
		myApp.setSettingsSuccess(false);
		myApp.saveAppPrefs();

		// call the signup API call
		if (mFSM != null) {
			if (StringHelper.isEmpty(myApp.getDeviceUUID())) {
				mFSM.callSignup();
			} else {
				mFSM.callSaveSettings();
			}
		}
	}

	private boolean isValidEmail(CharSequence target) {
		if (target == null) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "in onResume()");
		super.onResume();
		mFSM = myApp.getFSM();
		// check to see if we're resuming from a signup call
		if (mCurrentState == IN_LIVEENSURE_SESSION) {
			// should be resuming from an LE call, check FSM for state of
			// session
			Log.w(TAG, "resuming from LIVEENSURE session");
			mFSM.setUI(TAG, this);
			mCurrentState = SESSION_STATUS;
			mFSM.callStatus();
		} else {
			resetUI();
		}
	}

	@Override
	public void handleError(String msg) {
		Log.i(TAG, "in handleError()");
		// check to see if we're resuming from a signup call
		mLytWindowShade.setVisibility(View.GONE);
		if (mFSM != null) {
			if (mFSM.getCurrentState() == FourSureFSM.DEVICE_SIGNUP_FAILED) {
				Log.w(TAG, "signup failed, allowing user to try again");
				Toast.makeText(getApplicationContext(), R.string.unable_to_signup_please_try_again, Toast.LENGTH_LONG).show();
			} else if (mFSM.getCurrentState() == FourSureFSM.SAVE_SETTINGS_FAILED) {
				Log.w(TAG, "save settings failed, allowing user to try again");
				Toast.makeText(getApplicationContext(), R.string.unable_to_save_settings_please_try_again, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			}
		}
	}

	private void resetUI() {
		// turn "on" current ID
		mUserName = myApp.getUserName();
		mTxtUserID.setText((FourSureApplication.DEFAULT_USER_NAME.equals(mUserName)) ? "" : mUserName);
		mUserIdType = myApp.getUserIdType();
		mCurrentFactorType = myApp.getUserFactorType();
		Resources res = myApp.getResources();
		for (FlatButton b : buttons) {
			if (b.getFactorType() == mCurrentFactorType) {
				b.setActive(true);
			}
		}
		// set active button to active image
		Drawable backgrounds[] = new Drawable[2];

		for (FlatButton b : buttons) {
			backgrounds[0] = res.getDrawable(b.getInactiveDrawableID());
			backgrounds[1] = res.getDrawable(b.getActiveDrawableID());
			TransitionDrawable crossfader = new TransitionDrawable(backgrounds);
			b.getView().setImageDrawable(crossfader);
			if (b.isActive()) {
				// the active button
				crossfader.startTransition(300);
			} else {
				// inactive button
				crossfader.resetTransition();
			}
		}
		mLytDetailsPin.setVisibility(View.INVISIBLE);
		// mLytDetailsBehaviorGrid.setVisibility(View.GONE);
		mLytDetailsWearable.setVisibility(View.INVISIBLE);
		mLytWindowShade.setVisibility(View.GONE);
		if (mCurrentFactorType == FactorType.WEARABLE)
			mCurrentWearableType = "pebble";

	}

	private void handleButtonTap(View v) {
		if (!(v instanceof ImageView))
			return;
		ImageView iv = (ImageView) v;
		Resources res = myApp.getResources();

		Log.w(TAG, "in handleButtonTap()");
		// update button state
		for (FlatButton b : buttons) {
			if (b.getView() == iv) {
				Log.w(TAG, "detected button tap on button named " + b.getName());
				b.setActive(!b.isActive()); // toggle button state
				if (b.getName().equals(res.getString(R.string.chal_pin)) && b.isActive())
					mFlatButtonWearable.setActive(false);
				if (b.getName().equals(res.getString(R.string.chal_wearable)) && b.isActive())
					mFlatButtonPin.setActive(false);
				break;
			}
		}

		mCurrentFactorType = FactorType.NONE;
		for (FlatButton b : buttons) {
			TransitionDrawable crossfader = (TransitionDrawable) b.getView().getDrawable();
			if (b.isActive()) {
				// set the button active
				crossfader.startTransition(250);
				mCurrentFactorType = b.getFactorType();

				// if (mCurrentFactorType.equals(FactorType.BEHAVIOR)) {
				// mLytDetailsBehaviorButtons.setAlpha(0.0f);
				// mLytDetailsBehaviorButtons.setVisibility(View.VISIBLE);
				// mLytDetailsBehaviorButtons.animate().alpha(1.0f).setListener(null).start();
				// mLytDetailsBehaviorGrid.setVisibility(View.VISIBLE);
				// }
				if (mCurrentFactorType.equals(FactorType.PIN)) {
					mLytDetailsPin.setAlpha(0.0f);
					mLytDetailsPin.setVisibility(View.VISIBLE);
					mLytDetailsPin.animate().alpha(1.0f).setListener(null).start();
				}
				if (mCurrentFactorType.equals(FactorType.WEARABLE)) {
					mLytDetailsWearable.setAlpha(0.0f);
					mLytDetailsWearable.setVisibility(View.VISIBLE);
					mLytDetailsWearable.animate().alpha(1.0f).setListener(null).start();
				}
			} else {
				// inactive button
				crossfader.resetTransition();
			}
		}
	}

	// TODO: perhaps move this method up to BaseActivity, since all activities
	// do pretty much the same thing when launching LE
	public void launchLiveEnsure() {

		Intent miniLiveEnsureIntent = new Intent(this, MiniLiveEnsureActivity.class);
		// miniLiveEnsureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
		// Intent.FLAG_ACTIVITY_NEW_TASK);
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
			Toast.makeText(getApplicationContext(), R.string.unable_to_launch_liveensure, Toast.LENGTH_LONG).show();
		}
	}

	public void handleStatusResult(int status) {
		if (status == FourSureFSM.SESSION_STATUS_UNDETERMINED) {
			mCurrentState = SESSION_STATUS;
			mFSM.callStatus();
		} else if (status == FourSureFSM.SESSION_STATUS_SUCCESS) {
			// move back to front screen
			// TODO: work with possible wearable problem (if user chose that
			// factor, and it failed to register)
			Log.i(TAG, "signup success, going back to start screen");
			myApp.setSettingsSuccess(true);
			finish();
		} else if (status == FourSureFSM.SAVE_SETTINGS_SUCCESS) {
			// move back to front screen
			// TODO: work with possible wearable problem (if user chose that
			// factor, and it failed to register)
			Log.i(TAG, "save settings success, going back to start screen");
			myApp.setSettingsSuccess(true);
			finish();
		} else {
			// some error condition
			Log.w(TAG, "signup failed, allowing user to try again");
			// mLytWindowShade.setVisibility(View.GONE);
			Toast.makeText(getApplicationContext(), R.string.registration_failed_please_try_again, Toast.LENGTH_LONG).show();
			myApp.setDeviceUUID("");
			myApp.saveAppPrefs();
			myApp.resetFsSession();
			myApp.clearCurrentShareDetails();
			finish();
		}
	}

	private void setUpPinGrid() {
		super.onResume();
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;

		LinearLayout lytInputs = (LinearLayout) findViewById(R.id.settings_pin_challenge_inputs);
		lytInputs.removeAllViews();
		mDigitInputs = new TextView[mPinLength];
		mEnteredPin = new StringBuffer();
		mCurrentDigit = 0;
		int cellWidth = width / 8;
		int cellSpacing = cellWidth / 4;

		for (int x = 0; x < mPinLength; x++) {
			mDigitInputs[x] = new TextView(this);
			mDigitInputs[x].setBackgroundResource(R.drawable.pin_input_edittext);
			mDigitInputs[x].setGravity(Gravity.CENTER);
			mDigitInputs[x].setTextSize(30);
			LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(cellWidth, cellWidth);
			lp1.setMargins(cellSpacing, 0, cellSpacing, 0);
			mDigitInputs[x].setLayoutParams(lp1);
			lytInputs.addView(mDigitInputs[x]);
		}

		GridView gridview = (GridView) findViewById(R.id.settings_pin_challenge_numpad);
		gridview.setAdapter(new ArrayAdapter<String>(this, R.layout.settings_pin_numpad_button_view, mNumPadArray));

		gridview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Log.w(TAG, "Tap detected at position " + position);
				if (position == 9)
					return; // empty box to the left of zero
				if (position == 11) {
					// special case for delete, clear digit at pointer

					if (mCurrentDigit > 0) {
						// remove the last character from our buffer
						if (mEnteredPin.length() > 0)
							mEnteredPin.deleteCharAt(mEnteredPin.length() - 1);
						mDigitInputs[mCurrentDigit - 1].setText("");
					}
					Log.i(TAG, "deleted character at " + mCurrentDigit + ", entered pin is: " + mEnteredPin);
					mCurrentDigit--;
					if (mCurrentDigit < 0)
						mCurrentDigit = 0;
					return;
				} else {
					if (mCurrentDigit >= mPinLength) {
						// past the end, don't do anything
						Log.i(TAG, "PIN entry complete, ignoring new digit");
						return;
					}
					// add the digit pressed to our stringbuffer
					mEnteredPin.append(mNumPadArray[position]);
					Log.i(TAG, "current entered pin: " + mEnteredPin);
					if (mCurrentDigit < mPinLength)
						mDigitInputs[mCurrentDigit].setText(mNumPadArray[position]);
					mCurrentDigit++;
				}
			}
		});
	}

}
