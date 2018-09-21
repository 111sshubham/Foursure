package com.liveensure.a.mini.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.MapsInitializer;
import com.liveensure.a.mini.R;
import com.liveensure.a.mini.UIForFiniteStateMachine;
import com.liveensure.core.AgentFSM;
import com.liveensure.rest.api.AgentChallengeResponse;
import com.liveensure.util.Log;
import com.liveensure.util.StringHelper;

public class MapChallengeFragment extends BaseChallengeFragment implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener
{

  private static View         view;
  private static final String TAG                     = MapChallengeFragment.class.getSimpleName();
  private static final String EXTRA_CHALLENGE         = "chal";
  AgentChallengeResponse      mChallenge;
  private Location            mCurrentLocation;
  private boolean             mMalformedChallenge;
  private static final int    TWO_MINUTES_MS          = 1000 * 60 * 2;                             //
  private static final long   LOC_UPDATE_MS           = 4000;                                      // 4 seconds
  private static final long   LOC_FASTEST_INTERVAL_MS = 1000;                                      // 1 second
  private LocationRequest     mLocationRequest;
  private LocationClient      mLocationClient;
  private ImageView           mLocationChallengeGraphic;
  private RelativeLayout      mLayout;

  public static MapChallengeFragment newInstance(AgentChallengeResponse chal)
  {
    MapChallengeFragment f = new MapChallengeFragment();
    Bundle bdl = new Bundle(1);
    bdl.putSerializable(EXTRA_CHALLENGE, chal);
    f.setArguments(bdl);
    return f;
  }

  @Override
  public void onAttach(Activity activity)
  {
    super.onAttach(activity);
    if (activity instanceof UIForFiniteStateMachine) {
      mActivity = activity;
      ui = (UIForFiniteStateMachine) activity;
    }
    else {
      throw new ClassCastException("Invoking activity for this fragment must implement the UIForFiniteStateMachine interface");
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mMalformedChallenge = false;
    AgentChallengeResponse chal = (AgentChallengeResponse) getArguments().getSerializable(EXTRA_CHALLENGE);
    if (chal != null) {
      mChallenge = chal;
    }
    if (ui.getApp().isGoogleServicesAvailable()) {
      try {
        MapsInitializer.initialize(mActivity);
      }
      catch (GooglePlayServicesNotAvailableException e) {
        Log.e(TAG, "Exception during MapsInitializer.initialize()", e);
      }

      // Initialize the location service for geo challenges
      initLocationService();

    }
  }

  /**
   * set up (but do not start) the location service objects needed to listen to core location updates from the OS
   */
  private void initLocationService()
  {
    int gPlayAvail = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
    if (gPlayAvail == ConnectionResult.SUCCESS) {
      // Log.d(TAG, "Google Play Service is available, enabling location reporting");
    }
    else {
      Log.e(TAG, "Google Play Service is not available, no location info.  Code: " + gPlayAvail);
      GooglePlayServicesUtil.getErrorDialog(gPlayAvail, mActivity, 1122).show();
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
    mLocationClient = new LocationClient(mActivity, this, this);

    // all ready to go. we'll connect and start actually listening in the onResume() method
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    if (container == null) {
      return null;
    }
    view = (RelativeLayout) inflater.inflate(R.layout.map_view, container, false);
    //    setUpMapIfNeeded(); // For setting up the MapFragment
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState)
  {
  }

  @Override
  public void onResume()
  {
    Log.d(TAG, "in onResume()");
    super.onResume();
    if (mMalformedChallenge) {
      Log.w(TAG, "Location challenge is malformed, unable to show challenge. Redirecting to error view");
      ui.showError(getResources().getString(R.string.error_fragment_text));
      return;
    }
    mLayout = (RelativeLayout) mActivity.findViewById(R.id.location_challenge_fragment_layout);
    mLocationChallengeGraphic = (ImageView) mActivity.findViewById(R.id.location_challenge_graphic);
    float graphicAlpha = (ui.isShowChallengeIndicators()) ? 0.5f : 0f;
    mLocationChallengeGraphic.setAlpha(graphicAlpha);
    if (mLocationClient != null) {
      mLocationClient.connect();
    }
    if (mLayout.getVisibility() != View.VISIBLE) fadeInUI();
  }

  private void fadeInUI()
  {
    mLayout.setAlpha(0f);
    mLayout.setVisibility(View.VISIBLE);
    mLayout.animate().alpha(1.0f).setDuration(200).setListener(null);
  }

  private void fadeOutUI()
  {
    mLayout.animate().alpha(0f).setDuration(200).setListener(new AnimatorListenerAdapter()
    {
      @Override
      public void onAnimationEnd(Animator animation)
      {
        mLayout.setVisibility(View.INVISIBLE);
      }
    });
  }

  /**
   * called when the app is backgrounded (but not ended)
   */
  @Override
  public void onPause()
  {
    super.onPause();
    if (mLocationClient != null) mLocationClient.disconnect(); // stop listening to location updates if we're paused
  }

  /****
   * The mapfragment's id must be removed from the FragmentManager or else if the same it is passed on the next time then app will crash
   ****/
  @Override
  public void onDestroyView()
  {
    super.onDestroyView();

  }

  @Override
  public void retryChallenge()
  {
    // noop, retry not possible    
  }

  // LocationListener callback

  @Override
  public void onLocationChanged(Location location)
  {
    // update our current notion of location
    if (isBetterLocation(location, mCurrentLocation)) {
      Log.i(TAG, "Updating current location:\naccuracy: " + location.getAccuracy() + "\nlat: " + location.getLatitude() + "\nlong: " + location.getLongitude());
      mCurrentLocation = location;
    }
    Log.d(TAG, "got a location update and in the location challenge. sending location answer");
    sendCurrentLocationAsAnswer();
  }

  /**
   * Determines whether one Location reading is better than the current Location fix
   * 
   * @param location
   *          The new Location that you want to evaluate
   * @param currentBestLocation
   *          The current Location fix, to which you want to compare the new one
   */
  protected boolean isBetterLocation(Location location, Location currentBestLocation)
  {
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
    }
    else if (isSignificantlyOlder) {
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
    }
    else if (isNewer && !isLessAccurate) {
      return true;
    }
    else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
      return true;
    }
    return false;
  }

  /** Checks whether two providers are the same */
  private boolean isSameProvider(String provider1, String provider2)
  {
    if (provider1 == null) {
      return provider2 == null;
    }
    return provider1.equals(provider2);
  }

  private void sendCurrentLocationAsAnswer()
  {
    if (mCurrentLocation != null) {
      fadeOutUI();
      // pass the answer text to the FSM
      String answer = mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude();
      AgentFSM fsm = ui.getAgentFSM();
      if (fsm == null) return;
      String obscuredAnswer = StringHelper.obscure(fsm.getSession().getSessionToken(), answer);
      fsm.makeAnswerChallengeCallWithID(mChallenge.getChallengeID(), obscuredAnswer, "2.0");
    }

  }

  // Google play services callbacks
  @Override
  public void onConnectionFailed(ConnectionResult arg0)
  {
    Log.w(TAG, "google play services connect failed.  unable to get location updates.");
  }

  @Override
  public void onConnected(Bundle arg0)
  {
    Log.i(TAG, "google play services connected.  starting to listen for location updates now");
    if (mLocationClient != null) mLocationClient.requestLocationUpdates(mLocationRequest, this);
  }

  @Override
  public void onDisconnected()
  {
    Log.w(TAG, "google play services discconnected.  unable to get location updates.");
  }

}