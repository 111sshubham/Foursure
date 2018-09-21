package com.foursure1;

// Small commit test comment from Marc

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.foursure1.api.AccessShareResponse;
import com.foursure1.util.FactorType;
import com.foursure1.util.HandleType;
import com.foursure1.util.ShareType;

public class FourSureApplication extends Application {
	public static final String TAG = FourSureApplication.class.getSimpleName();
	public static final String DEFAULT_USER_NAME = "Unknown";
	public static final String DEFAULT_DEV_UUID = "";
	public static final String DEFAULT_RECIPIENT_USER_NAME = "na";
	public static final HandleType DEFAULT_USER_ID_TYPE = HandleType.EMAIL;
	// public static final String FOURSHARE_HELP_URL = "https://www.youtube.com/watch?v=LwIxWqkYk-U";
	public static final String FOURSHARE_HELP_URL = "http://www.foursure.com/mobile_help";
	public static final String DEFAULT_API_HOST = "https://fs.mastlabs.com/fs/api/v1";
	public static final FactorType DEFAULT_CHALLENGE = FactorType.NONE;
	public static final String DEFAULT_CHALLENGE_ANSWER = "";
	public static final String DEFAULT_STORAGE_TYPE = "none";
	private static final String APP_PREFS = "assetensure.prefs";
	private static final String IDENTIFIER_USER_FACTOR = "user.factor.type";
	private static final String IDENTIFIER_USER_FACTOR_DETAILS = "user.factor.details";
	private static final String IDENTIFIER_API_HOST = "api.host";
	private static final String IDENTIFIER_USER_NAME = "user.name";
	private static final String IDENTIFIER_DEVICE_UUID = "device.uuid";
	private static final String IDENTIFIER_SHOW_CHALLENGE_INDICATORS = "show.challenge.indicators";
	private String mApiHost;
	private FactorType mUserFactorType = DEFAULT_CHALLENGE;
	private String mUserFactorDetails = "";
	private FactorType mShareFactorType = DEFAULT_CHALLENGE;
	private String mActivatedShareAsset = "";
	private ShareType mActivatedShareAssetType = ShareType.NONE;
	private String mActivatedShieldValue = "";
	private String mUserName = DEFAULT_USER_NAME;
	private HandleType mUserIdType = DEFAULT_USER_ID_TYPE;
	private String mRecipientUserName = DEFAULT_RECIPIENT_USER_NAME;
	private HandleType mRecipientUserIdType = DEFAULT_USER_ID_TYPE;
	private boolean mShowChallengeIndicators = true;
	private String mShortCode = "";
	private String mShareFactorDetails;
	private boolean mFailedFinish;
	private RequestQueue mRequestQueue;
	private FourSureFSM mFSM;
	private String mDeviceUUID;
	private String mLESessionToken;
	private String mLEIdentityServer;
	private Context mAppContext;
	private boolean mShareOptionTracking;
	private boolean mShareOptionShield;
	private AccessShareResponse mShareToAccess;
	private String mCurrentErrorMessage;

	private String mFsSessionToken;
	private long mFsSessionExpiration;
	private boolean mGoogleServicesAvailable;
	private boolean mLiveEnsureAuthenticated;
	private boolean mSettingsSuccess;
	private String mDefaultApiHost;

	public FourSureApplication() {
		/*
		 * the onCreate() method is only called when the app is initially started. Often, this is once per power cycle on a device the only logic that should be in here is basic loading of assets
		 * (e.g. fonts) and initialization of singleton classes
		 * 
		 * most startup logic belongs in the onResume() method of the primary activity (e.g. MiniLiveEnsureActivity) since we are often already running as a background application and when another app
		 * invokes us, usually via an Intent, the onResume() method is executed first.
		 */
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "in onCreate()");
		mAppContext = this;
		// Instantiate the cache
		DiskBasedCache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

		// Set up the network to use HttpURLConnection as the HTTP client.
		Network network = new BasicNetwork(new HurlStack());

		// Instantiate the RequestQueue with the cache and network.
		mRequestQueue = new RequestQueue(cache, network);

		// Start the queue
		mRequestQueue.start();
		// create a singleton volley request queue for network requests
		// mRequestQueue = VolleySingleton.getInstance(mAppContext).getRequestQueue();
		mDefaultApiHost = DEFAULT_API_HOST;
		// Read from the /assets directory - if there is a local.properties file, use that - otherwise, use the prevailing api host (production)
		try {
			Resources resources = mAppContext.getResources();
			AssetManager assetManager = resources.getAssets();
			InputStream is = assetManager.open("local.properties");
			Properties properties = new Properties();
			properties.load(is);
			mDefaultApiHost = properties.getProperty("api.host");
			Log.i(TAG, "Retrieved api host from /assets: " + mDefaultApiHost);
		} catch (IOException e) {
			Log.w(TAG, "Failed to open local property file from /assets");
		}
		getAppPrefs();
		mFSM = new FourSureFSM(this);
		mFSM.setDefaultHost(mApiHost);
		Log.i(TAG, "Setting API host to " + mApiHost);
	}

	public void getAppPrefs() {
		SharedPreferences prefs = getApplicationContext().getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
		String userName = null;
		String deviceUUID = null;
		FactorType userFactorType = null;
		String userFactorDetails = null;
		String apiHost = null;
		boolean indicators = true;
		if (prefs != null) {
			userName = prefs.getString(IDENTIFIER_USER_NAME, DEFAULT_USER_NAME);
			deviceUUID = prefs.getString(IDENTIFIER_DEVICE_UUID, DEFAULT_DEV_UUID);
			userFactorType = FactorType.valueOf(prefs.getString(IDENTIFIER_USER_FACTOR, FactorType.NONE.name()));
			userFactorDetails = prefs.getString(IDENTIFIER_USER_FACTOR_DETAILS, DEFAULT_CHALLENGE_ANSWER);
			indicators = prefs.getBoolean(IDENTIFIER_SHOW_CHALLENGE_INDICATORS, true);
			apiHost = prefs.getString(IDENTIFIER_API_HOST, mDefaultApiHost);
		} else {
			// No shared prefs yet, create a new set with application defaults
			userName = DEFAULT_USER_NAME;
			deviceUUID = DEFAULT_DEV_UUID;
			userFactorType = DEFAULT_CHALLENGE;
			userFactorDetails = DEFAULT_CHALLENGE_ANSWER;
			apiHost = mDefaultApiHost;
			indicators = true;
			SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE).edit();
			editor.putString(IDENTIFIER_API_HOST, apiHost);
			editor.putString(IDENTIFIER_USER_NAME, userName);
			editor.putString(IDENTIFIER_DEVICE_UUID, deviceUUID);
			editor.putString(IDENTIFIER_USER_FACTOR, userFactorType.name());
			editor.putString(IDENTIFIER_USER_FACTOR_DETAILS, userFactorDetails);
			editor.putBoolean(IDENTIFIER_SHOW_CHALLENGE_INDICATORS, indicators);
			editor.commit();
		}
		mUserName = userName;
		mDeviceUUID = deviceUUID;
		mUserFactorType = userFactorType;
		mUserFactorDetails = userFactorDetails;
		mShowChallengeIndicators = indicators;
		mApiHost = apiHost;
		Log.i(TAG, "Loaded prefs, apiHost is " + apiHost + ", challenge is " + mUserFactorType + ", user name is " + mUserName + ", device UUID is " + mDeviceUUID);

		// TODO: reset any app state here, or have an explicit reset() function?
		mFailedFinish = false;

	}

	public void saveAppPrefs() {
		Log.i(TAG, "Saving prefs, host is " + mApiHost + " challenge is " + mUserFactorType + ", user name is " + mUserName + ", user id type is " + mUserIdType);
		SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE).edit();
		editor.putString(IDENTIFIER_API_HOST, mApiHost);
		editor.putString(IDENTIFIER_USER_NAME, mUserName);
		editor.putString(IDENTIFIER_DEVICE_UUID, mDeviceUUID);
		editor.putString(IDENTIFIER_USER_FACTOR, mUserFactorType.name());
		editor.putString(IDENTIFIER_USER_FACTOR_DETAILS, mUserFactorDetails);
		editor.putBoolean(IDENTIFIER_SHOW_CHALLENGE_INDICATORS, mShowChallengeIndicators);
		editor.commit();
	}

	public boolean isLiveEnsureAuthenticated() {
		return mLiveEnsureAuthenticated;
	}

	public void setLiveEnsureAuthenticated(boolean auth) {
		this.mLiveEnsureAuthenticated = auth;
	}

	public String getUserName() {
		return mUserName;
	}

	public void setUserName(String mUserName) {
		this.mUserName = mUserName;
	}

	public String getApiHost() {
		return mApiHost;
	}

	public void setApiHost(String mApiHost) {
		this.mApiHost = mApiHost;
	}

	public FactorType getUserFactorType() {
		return mUserFactorType;
	}

	public void setUserFactorType(FactorType factorType) {
		this.mUserFactorType = factorType;
	}

	public void setShowChallengeIndicators(boolean checked) {
		this.mShowChallengeIndicators = checked;
	}

	public boolean isShowChallengeIndicators() {
		return mShowChallengeIndicators;
	}

	public HandleType getUserIdType() {
		return mUserIdType;
	}

	public void setUserIdType(HandleType mUserIdType) {
		this.mUserIdType = mUserIdType;
	}

	public String getShortCode() {
		return mShortCode;
	}

	public void setShortCode(String mShortCode) {
		this.mShortCode = mShortCode;
	}

	public FactorType getShareFactorType() {
		return mShareFactorType;
	}

	public void setShareFactorType(FactorType mActivatedShareChallenge) {
		this.mShareFactorType = mActivatedShareChallenge;
	}

	public String getShareFactorDetails() {
		return mShareFactorDetails;
	}

	public void setShareFactorDetails(String details) {
		this.mShareFactorDetails = details;
	}

	public String getActivatedShareAsset() {
		return mActivatedShareAsset;
	}

	public void setActivatedShareAsset(String mActivatedShareAsset) {
		this.mActivatedShareAsset = mActivatedShareAsset;
	}

	public String getRecipientUserName() {
		return mRecipientUserName;
	}

	public void setRecipientUserName(String mRecipientUserName) {
		this.mRecipientUserName = mRecipientUserName;
	}

	public HandleType getRecipientUserIdType() {
		return mRecipientUserIdType;
	}

	public void setRecipientUserIdType(HandleType mRecipientUserIdType) {
		this.mRecipientUserIdType = mRecipientUserIdType;
	}

	public String getUserFactorDetails() {
		return mUserFactorDetails;
	}

	public void setUserFactorDetails(String details) {
		this.mUserFactorDetails = details;
	}

	public ShareType getActivatedShareAssetType() {
		return mActivatedShareAssetType;
	}

	public void setActivatedShareAssetType(ShareType shareType) {
		this.mActivatedShareAssetType = shareType;
	}

	public String getActivatedShieldValue() {
		return mActivatedShieldValue;
	}

	public void setActivatedShieldValue(String mActivatedShieldValue) {
		this.mActivatedShieldValue = mActivatedShieldValue;
	}

	public void setFailedFinish(boolean b) {
		this.mFailedFinish = b;
	}

	public boolean isFailedFinish() {
		return mFailedFinish;
	}

	public RequestQueue getRequestQueue() {
		return mRequestQueue;
	}

	/**
	 * 
	 * @return an instance of FourSureFSM that is guaranteed to not be null
	 */
	public FourSureFSM getFSM() {
		if (mFSM == null) {
			mFSM = new FourSureFSM(this);
			mFSM.setDefaultHost(mApiHost);
		}
		return mFSM;
	}

	public void setFSM(FourSureFSM mFSM) {
		this.mFSM = mFSM;
	}

	public String getProgramVersion() {
		PackageInfo pInfo;
		String version = "Unknown";
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			version = "Unknown";
		}
		return version;
	}

	public String getDeviceUUID() {
		return mDeviceUUID;
	}

	public void setDeviceUUID(String id) {
		mDeviceUUID = id;
	}

	public String getFsSessionToken() {
		return mFsSessionToken;
	}

	public void setFsSessionToken(String token) {
		this.mFsSessionToken = token;
	}

	public String getLESessionToken() {
		return mLESessionToken;
	}

	public void setLESessionToken(String mLESessionToken) {
		this.mLESessionToken = mLESessionToken;
	}

	public String getLEIdentityServer() {
		return mLEIdentityServer;
	}

	public void setLEIdentityServer(String mLEIdentityServer) {
		this.mLEIdentityServer = mLEIdentityServer;
	}

	public boolean isShareOptionTracking() {
		return mShareOptionTracking;
	}

	public void setShareOptionTracking(boolean mShareOptionTracking) {
		this.mShareOptionTracking = mShareOptionTracking;
	}

	public AccessShareResponse getShareToAccess() {
		return mShareToAccess;
	}

	public void setShareToAccess(AccessShareResponse shareToAccess) {
		this.mShareToAccess = shareToAccess;
	}

	public boolean isGoogleServicesAvailable() {
		return mGoogleServicesAvailable;
	}

	public void setGoogleServicesAvailable(boolean googleServicesAvailable) {
		this.mGoogleServicesAvailable = googleServicesAvailable;
	}

	public void clearCurrentShareDetails() {
		Log.e(TAG, "in clearCurrentShareDetails()");
		setShortCode("");
		setShareFactorType(FactorType.NONE);
		setShareFactorDetails("");
		setActivatedShareAssetType(ShareType.NONE);
		setActivatedShareAsset("");
		setActivatedShieldValue("");
		setShareOptionTracking(false);
		setShareOptionShield(false);
		setShareToAccess(null);
	}

	public void resetFsSession() {
		setFsSessionToken("");
		setFsSessionExpiration(0L);
	}

	public String getCurrentErrorMessage() {
		return mCurrentErrorMessage;
	}

	public void setCurrentErrorMessage(String mCurrentErrorMessage) {
		this.mCurrentErrorMessage = mCurrentErrorMessage;
	}

	public boolean isShareOptionShield() {
		return mShareOptionShield;
	}

	public void setShareOptionShield(boolean mShareOptionShield) {
		this.mShareOptionShield = mShareOptionShield;
	}

	public boolean isSettingsSuccess() {
		return mSettingsSuccess;
	}

	public void setSettingsSuccess(boolean mSettingsSuccess) {
		this.mSettingsSuccess = mSettingsSuccess;
	}

	public long getFsSessionExpiration() {
		return mFsSessionExpiration;
	}

	public void setFsSessionExpiration(long mFsSessionExpiration) {
		this.mFsSessionExpiration = mFsSessionExpiration;
	}
}
