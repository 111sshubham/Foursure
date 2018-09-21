package com.liveensure.a.mini;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.liveensure.core.AgentFSM;
import com.liveensure.core.IDSession;
import com.liveensure.util.Log;

public class LEApplication {

	static final String TAG = "LEApplication";

	private boolean launchedBy3rdParty;
	private boolean invalidQRCode = false;
	private boolean pinCanceled = Boolean.FALSE;
	private boolean googleServicesAvailable;
	private AgentFSM agentFSM;
	private boolean homeChallengePurchased;
	private boolean pinChallengePurchased;
	private boolean behaviorChallengePurchased;
	private boolean pebbleChallengePurchased;
	public static final int NUM_TOUCH_REGIONS = 6;

	private boolean storeOverride = false;
	private boolean inPurchase;
	private Intent startupIntent;

	private String DEV_PREFS = "dev.prefs";
	private String IDENTIFIER_DEFAULT_HOST = "default.host";

	private String defaultHost = "localhost"; // safe default, no network calls would work with this though

	private Context mAppContext;
	private RequestQueue mRequestQueue;

	public LEApplication(Activity a, Context c) {
		/*
		 * the onCreate() method is only called when the app is initially started. Often, this is once per power cycle on a device the only logic that should be in here is basic loading of assets
		 * (e.g. fonts) and initialization of singleton classes like AgentFSM
		 * 
		 * most startup logic belongs in the onResume() method of the primary activity (e.g. MiniLiveEnsureActivity) since we are often already running as a background application and when another app
		 * invokes us, usually via an Intent, the onResume() method is excecuted first.
		 */
		Log.i(TAG, "in onCreate()");
		mAppContext = c;
		// create a singleton volley request queue for network requests - but based on the MiniLiveEnsureActivity

		// Instantiate the cache
		DiskBasedCache cache = new DiskBasedCache(a.getCacheDir(), 1024 * 1024); // 1MB cap

		// Set up the network to use HttpURLConnection as the HTTP client.
		Network network = new BasicNetwork(new HurlStack());

		// Instantiate the RequestQueue with the cache and network.
		mRequestQueue = new RequestQueue(cache, network);

		// Start the queue
		mRequestQueue.start();
		// mRequestQueue = VolleySingleton.getInstance(a).getRequestQueue();
		configureLogging();
		SharedPreferences prefs = mAppContext.getSharedPreferences(DEV_PREFS, Context.MODE_PRIVATE);
		String defHost = null;
		if (prefs != null) {
			defHost = prefs.getString(IDENTIFIER_DEFAULT_HOST, null);
			Log.i(TAG, "Retrieved default host from existing shared prefs file: " + defHost);
		}
		if (defHost == null) {
			// Read from the /assets directory
			try {
				Resources resources = mAppContext.getResources();
				AssetManager assetManager = resources.getAssets();
				InputStream is = assetManager.open("local.properties");
				Properties properties = new Properties();
				properties.load(is);
				defHost = properties.getProperty("default.host");
				Log.i(TAG, "Retrieved default host from /assets: " + defHost);
			} catch (IOException e) {
				Log.e(TAG, "Failed to open local property file from /assets");
			}
			if (defHost == null)
				defHost = "https://app.liveensure.com/live-identity/rest"; // fallback
			Log.w(TAG, "using last-case fallback host of " + defHost);
		}

		agentFSM = new AgentFSM(this);
		setDefaultHost(defHost); // also saves to prefs
		Log.i(TAG, "Setting default host to " + defHost);
		defaultHost = defHost;
	}

	public String getDefaultHost() {
		return defaultHost;
	}

	public IDSession getSession() {
		if (this.agentFSM == null)
			return null;
		return this.agentFSM.getSession();
	}

	public boolean wasLaunchedBy3rdParty() {
		return this.launchedBy3rdParty;
	}

	public void setLaunchedBy3rdParty(boolean launchedBy3rdParty) {
		this.launchedBy3rdParty = launchedBy3rdParty;
	}

	public boolean isInvalidQRCode() {
		return invalidQRCode;
	}

	public void setInvalidQRCode(boolean b) {
		invalidQRCode = b;
	}

	private void configureLogging() {
		boolean debuggable = isDebugMode();
		Log.setEnabled(debuggable);
		Log.d(TAG, "Logging: " + debuggable);
	}

	public boolean isDebugMode() {
		// if (true) return true;
		boolean debuggable = Boolean.FALSE;

		PackageManager pm = mAppContext.getPackageManager();
		try {
			ApplicationInfo appinfo = pm.getApplicationInfo(mAppContext.getPackageName(), 0);
			debuggable = (0 != (appinfo.flags &= ApplicationInfo.FLAG_DEBUGGABLE));
		} catch (PackageManager.NameNotFoundException e) {
			/* debuggable variable will remain false */
		}
		return debuggable;
	}

	public boolean isPinCanceled() {
		return pinCanceled;
	}

	public void setPinCanceled(boolean pinCanceled) {
		this.pinCanceled = pinCanceled;
	}

	public AgentFSM getAgentFSM() {
		return agentFSM;
	}

	public void setAgentFSM(AgentFSM a) {
		agentFSM = a;
	}

	public boolean isGoogleServicesAvailable() {
		return googleServicesAvailable;
	}

	public void setGoogleServicesAvailable(boolean googleServicesAvailable) {
		this.googleServicesAvailable = googleServicesAvailable;
	}

	public boolean isHomeChallengePurchased() {
		return isStoreOverride() || homeChallengePurchased;
	}

	public void setHomeChallengePurchased(boolean b) {
		this.homeChallengePurchased = b;
	}

	public boolean isPinChallengePurchased() {
		return isStoreOverride() || pinChallengePurchased;
	}

	public void setPinChallengePurchased(boolean pinChallengePurchased) {
		this.pinChallengePurchased = pinChallengePurchased;
	}

	public boolean isPebbleChallengePurchased() {
		return isStoreOverride() || pebbleChallengePurchased;
	}

	public void setPebbleChallengePurchased(boolean b) {
		this.pebbleChallengePurchased = b;
	}

	public boolean isBehaviorChallengePurchased() {
		return isStoreOverride() || behaviorChallengePurchased;
	}

	public void setBehaviorChallengePurchased(boolean behaviorChallengePurchased) {
		this.behaviorChallengePurchased = behaviorChallengePurchased;
	}

	public String getProgramVersion() {
		PackageInfo pInfo;
		String version = "Unknown";
		try {
			pInfo = mAppContext.getPackageManager().getPackageInfo(mAppContext.getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			version = "Unknown";
		}
		return version;
	}

	public void setInPurchase(boolean b) {
		inPurchase = b;
	}

	public boolean isInPurchase() {
		return inPurchase;
	}

	public boolean isStoreOverride() {
		return storeOverride;
	}

	public void setStoreOverride(boolean storeOverride) {
		this.storeOverride = storeOverride;
	}

	public void setStartupIntent(Intent i) {
		startupIntent = i;
	}

	public Intent getStartupIntent() {
		return startupIntent;
	}

	public void setDefaultHost(String dh) {
		SharedPreferences.Editor editor = mAppContext.getApplicationContext().getSharedPreferences(DEV_PREFS, Context.MODE_PRIVATE).edit();
		editor.putString(IDENTIFIER_DEFAULT_HOST, dh);
		editor.commit();
		defaultHost = dh;
		getAgentFSM().setDefaultHost(dh);
	}

	public Context getApplicationContext() {
		return mAppContext;
	}

	public RequestQueue getRequestQueue() {
		return mRequestQueue;
	}

}