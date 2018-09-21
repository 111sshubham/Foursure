package com.liveensure.shuffler;

import java.io.Serializable;
import java.util.HashMap;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import com.liveensure.util.MessageDigester;

/**
 * 
 * @author aapel
 * 
 */
public class AndroidShuffler extends HardwareUIDBase implements Serializable
{

  private static final long     serialVersionUID         = -9002266574824044784L;
  protected static final String ID_POINT_ERROR           = "ERROR: cannot get ID";
  HashMap<String, String>       deviceAnswers            = new HashMap<String, String>();
  private String                LE_SHUFFLER_PREFS        = "le.suffler.prefs";
  private String                IDENTIFIER_INSTALL_NONCE = "install.nonce";
  protected final static String TAG                      = AndroidShuffler.class.getSimpleName();

  public AndroidShuffler(Context ctx)
  {
    context = ctx;
  }

  /*
   * Lots of the hw id points here are from the Build object, documented at http://developer.android.com/reference/android/os/Build.html
   * 
   * Other points are discussed on the Android dev blog at these sites:
   * 
   * http://android-developers.blogspot.com/2011/03/identifying-app-installations .html http://www.pocketmagic.net/?p=1662
   * http://stackoverflow.com/questions /2785485/is-there-a-unique-android-device-id
   * 
   * @see com.liveensure.agents.common.interfaces.HardwareUIDBase#getHardwareIDs (int[])
   */
  @Override
  public String[] getHardwareIDs(int[] idFlags)
  {
    String[] response = new String[idFlags.length];
    int x = 0;
    for (int i : idFlags) {
      switch (i) {
        case DRIVE_SERIAL:
          // we'll use the unique Android ID
          response[x] = getAndroidID();
          break;
        case CPU_ID:
          response[x] = getAndroidCPUID();
          break;
        case BYTES_RAM:
          // use the IMEI
          response[x] = getAndroidIMEI();
          break;
        case PRIMARY_MAC_ADDR:
          response[x] = getAndroidWiFiMacAddr();
          break;
        case VIDEO_CHIPSET:
          // Use app install nonce
          response[x] = getSoftwareInstallNonce();
          break;
        case MBOARD_CHIPSET:
          response[x] = getAndroidMboardChipset();
          break;
        case BIOS_ID:
          response[x] = getAndroidBiosID();
          break;
      }
      // Log.i(TAG, "Found hardware point #" + i + " as '" + response[x] + "'");
      x++;
    }
    return response;
  }

  private String getAndroidID()
  {
    // lazy loading - if we already have the ID point, return it. otherwise, go get it
    String keyForThisIDPoint = "hw1";
    if (deviceAnswers.containsKey(keyForThisIDPoint)) return deviceAnswers.get(keyForThisIDPoint);
    String r = ID_POINT_ERROR + "- " + keyForThisIDPoint;
    try {
      String androidID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
      if (androidID != null) r = androidID;
    }
    catch (Exception e) {
      // Log.e(LOG_TAG, "exception when getting Android ID: " + e.getMessage());
    }
    deviceAnswers.put(keyForThisIDPoint, r);
    return r;
  }

  private String getAndroidCPUID()
  {
    // lazy loading - if we already have the ID point, return it. otherwise, go get it
    String keyForThisIDPoint = "hw2";
    if (deviceAnswers.containsKey(keyForThisIDPoint)) return deviceAnswers.get(keyForThisIDPoint);
    String r = ID_POINT_ERROR + "- " + keyForThisIDPoint;
    String cpuABI1 = Build.CPU_ABI;
    String cpuABI2 = Build.CPU_ABI2;
    if (cpuABI1 != null) r = cpuABI1;
    if (cpuABI2 != null) r = r + "_" + cpuABI2;
    deviceAnswers.put(keyForThisIDPoint, r);
    return r;
  }

  private String getAndroidIMEI()
  {
    // lazy loading - if we already have the ID point, return it. otherwise, go get it
    String keyForThisIDPoint = "hw3";
    if (deviceAnswers.containsKey(keyForThisIDPoint)) return deviceAnswers.get(keyForThisIDPoint);
    String r = ID_POINT_ERROR + "- " + keyForThisIDPoint;
    try {
      TelephonyManager tpMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      // Requires READ_PHONE_STATE permission
      String imei = null;
      if (tpMgr != null) imei = tpMgr.getDeviceId();

      if (imei != null) r = imei;
    }
    catch (Exception e) {
      // Log.e(LOG_TAG, "exception when getting IMEI: " + e.getMessage());
    }

    deviceAnswers.put(keyForThisIDPoint, r);
    return r;
  }

  private String getAndroidWiFiMacAddr()
  {
    // lazy loading - if we already have the ID point, return it. otherwise, go get it
    String keyForThisIDPoint = "hw4";
    if (deviceAnswers.containsKey(keyForThisIDPoint)) return deviceAnswers.get(keyForThisIDPoint);
    String r = ID_POINT_ERROR + "- " + keyForThisIDPoint;
    try {
      WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      // requires ACCESS_WIFI_STATE permission
      String macAddr = null;
      if (wifiMgr != null) macAddr = wifiMgr.getConnectionInfo().getMacAddress();
      if (macAddr != null) r = macAddr;
    }
    catch (Exception e) {
      // Log.e(LOG_TAG, "exception when getting WiFi MAC addr: " + e.getMessage());
    }
    deviceAnswers.put(keyForThisIDPoint, r);
    return r;
  }

  @SuppressWarnings("unused")
  private String getAndroidBluetoothMacAddr()
  {
    // lazy loading - if we already have the ID point, return it. otherwise, go get it
    String keyForThisIDPoint = "hw5";
    if (deviceAnswers.containsKey(keyForThisIDPoint)) return deviceAnswers.get(keyForThisIDPoint);
    String r = ID_POINT_ERROR + "- " + keyForThisIDPoint;
    try {
      // requires BLUETOOTH permission
      BluetoothAdapter blueToothAdapter = BluetoothAdapter.getDefaultAdapter();
      String macAddr = null;
      if (blueToothAdapter != null) macAddr = blueToothAdapter.getAddress();
      if (macAddr != null) r = macAddr;
    }
    catch (Exception e) {
      // Log.e(LOG_TAG, "exception when getting bluetooth MAC addr: " + e.getMessage());
    }
    deviceAnswers.put(keyForThisIDPoint, r);
    return r;
  }

  private String getSoftwareInstallNonce()
  {
    // lazy loading - if we already have the ID point, return it. otherwise, go get it
    String keyForThisIDPoint = "hw5";
    if (deviceAnswers.containsKey(keyForThisIDPoint)) return deviceAnswers.get(keyForThisIDPoint);
    String r = ID_POINT_ERROR + "- " + keyForThisIDPoint;
    SharedPreferences prefs = context.getSharedPreferences(LE_SHUFFLER_PREFS, Context.MODE_PRIVATE);
    String installNonce = null;
    if (prefs != null) {
      installNonce = prefs.getString(IDENTIFIER_INSTALL_NONCE, null);
      // Log.i(TAG, "Retrieved a software install nonce from existing shared prefs file: " + installNonce);
    }
    if (installNonce == null) {
      installNonce = MessageDigester.secureToken(24);
      SharedPreferences.Editor editor = context.getSharedPreferences(LE_SHUFFLER_PREFS, Context.MODE_PRIVATE).edit();
      editor.putString(IDENTIFIER_INSTALL_NONCE, installNonce);
      editor.commit();
      // Log.i(TAG, "Generated a new software install nonce " + installNonce + " and saved to prefs file " + LE_SHUFFLER_PREFS);
    }
    if (installNonce != null) r = installNonce;
    deviceAnswers.put(keyForThisIDPoint, r);
    return r;
  }

  public void resetSoftwareInstallNonce()
  {
    String installNonce = MessageDigester.secureToken(24);
    SharedPreferences.Editor editor = context.getSharedPreferences(LE_SHUFFLER_PREFS, Context.MODE_PRIVATE).edit();
    editor.putString(IDENTIFIER_INSTALL_NONCE, installNonce);
    editor.commit();
    deviceAnswers.remove("hw5"); // remove the old nonce from our known answers
  }

  private String getAndroidMboardChipset()
  {
    // lazy loading - if we already have the ID point, return it. otherwise, go get it
    String keyForThisIDPoint = "hw6";
    if (deviceAnswers.containsKey(keyForThisIDPoint)) return deviceAnswers.get(keyForThisIDPoint);
    String r = ID_POINT_ERROR + "- " + keyForThisIDPoint;
    String chipset = Build.BOARD;
    if (chipset != null) r = chipset;
    deviceAnswers.put(keyForThisIDPoint, r);
    return r;
  }

  private String getAndroidBiosID()
  {
    // lazy loading - if we already have the ID point, return it. otherwise, go get it
    String keyForThisIDPoint = "hw7";
    if (deviceAnswers.containsKey(keyForThisIDPoint)) return deviceAnswers.get(keyForThisIDPoint);
    String r = ID_POINT_ERROR + "- " + keyForThisIDPoint;
    String biosID = Build.ID;
    if (biosID != null) r = biosID;
    deviceAnswers.put(keyForThisIDPoint, r);
    return r;
  }

}