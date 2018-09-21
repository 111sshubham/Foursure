package com.liveensure.core;

import java.util.HashMap;
import com.liveensure.util.Log;
import com.liveensure.util.StringHelper;

public class LocationChallenge extends DynamicChallenge
{

  private static final long serialVersionUID = -603754815177440165L;
  private String            TAG;
  private double            latitude;
  private double            longitude;
  private double            radius;

  public LocationChallenge()
  {
    TAG = this.getClass().getSimpleName();
    challengeType = DynamicChallengeType.LAT_LONG;
  }

  /**
   * @return the latitude
   */
  public double getLatitude()
  {
    return latitude;
  }

  /**
   * @param lattitude
   *          the latitude to set
   */
  public void setLatitude(double latitude)
  {
    this.latitude = latitude;
  }

  /**
   * @return the longitude
   */
  public double getLongitude()
  {
    return longitude;
  }

  /**
   * @param longitude
   *          the longitude to set
   */
  public void setLongitude(double longitude)
  {
    this.longitude = longitude;
  }

  /**
   * @return the radius
   */
  public double getRadius()
  {
    return radius;
  }

  /**
   * @param radius
   *          the radius to set
   */
  public void setRadius(double radius)
  {
    this.radius = radius;
  }

  @Override
  public boolean isAnswerOK(Object answer)
  {
    if (!(answer instanceof String)) {
      return false;
    }
    String[] parts = answer.toString().split("\\s*,\\s*");
    double lat2 = 1.0;
    double lon2 = 1.0;

    try {
      lat2 = Double.parseDouble(parts[0]);
      lon2 = Double.parseDouble(parts[1]);
    }
    catch (Exception e) {
      Log.e(TAG, "Failed to parse LatLong challenge answer: " + answer, e);
      return false;
    }
    // Uses haversine great-circle distance calculation
    double R = 6371; // km
    double dLat = Math.toRadians(lat2 - latitude);
    double dLon = Math.toRadians(lon2 - longitude);
    double lat1 = Math.toRadians(latitude);
    lat2 = Math.toRadians(lat2);

    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double d = R * c;
    Log.d(TAG, "Checking LAT_LONG distance " + d + " within radius of " + radius);
    return d <= radius;
  }

  @Override
  public HashMap<String, Object> toHashMap()
  {
    HashMap<String, Object> m = new HashMap<String, Object>();
    // we don't supply any information to the client for this one, just add its id
    m.put("challengeID", challengeID);
    if (!StringHelper.isEmpty(successMessage)) m.put("successMessage", successMessage);
    if (!StringHelper.isEmpty(failureMessage)) m.put("failureMessage", failureMessage);
    return m;
  }

}
