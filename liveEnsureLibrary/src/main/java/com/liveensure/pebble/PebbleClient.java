package com.liveensure.pebble;

import java.util.HashMap;
import java.util.Set;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.getpebble.android.kit.PebbleKit;
import com.liveensure.a.mini.LEApplication;
import com.liveensure.util.Log;
import com.liveensure.util.StringHelper;

public class PebbleClient
{
  private static final String  TAG = PebbleClient.class.getSimpleName();
  private Set<BluetoothDevice> mPairedDevices;
  private BluetoothAdapter     mBluetoothAdapter;
  private boolean              mIsPebbleConnected;
  private String               mPebbleHWID;
  private String               mPebbleName;

  public PebbleClient(LEApplication leApp)
  {

    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    mIsPebbleConnected = false;
    if (mBluetoothAdapter.isEnabled()) {
      mPairedDevices = mBluetoothAdapter.getBondedDevices();

      if (mPairedDevices.size() > 0) {
        for (BluetoothDevice device : mPairedDevices) {
          String deviceBTName = device.getName();
          String deviceBTAddress = device.getAddress();
          if (deviceBTName.matches("(?i).*pebble.*")) {
            Log.w(TAG,
                "Found a pebble device (" + deviceBTName + "), PebbleKit.isWatchConnected() is " + PebbleKit.isWatchConnected(leApp.getApplicationContext()));
            mIsPebbleConnected = PebbleKit.isWatchConnected(leApp.getApplicationContext());
            setPebbleHWID(deviceBTAddress);
            setPebbleName(deviceBTName);
          }
          Log.w(TAG, deviceBTName + "(" + deviceBTAddress + ")");
        }
      }
    }
  }

  public boolean isPebbleConnected()
  {
    return mIsPebbleConnected;
  }

  public String getPebbleHWID()
  {
    return mPebbleHWID;
  }

  public void setPebbleHWID(String id)
  {
    this.mPebbleHWID = id;
  }

  /**
   * 
   * @return a hashmap with the pebble BT MAC Address encoded in Base64
   */
  public HashMap<String, String> getRegistrationInfoAsHashMap()
  {
    HashMap<String, String> resp = new HashMap<String, String>();

    if (!mIsPebbleConnected || StringHelper.isEmpty(mPebbleHWID)) {
      resp.put("registrationAvailable", "false");
    }
    else {
      resp.put("registrationAvailable", "true");
      resp.put("pebbleID", mPebbleHWID);
    }
    return resp;
  }

  public String getPebbleName()
  {
    return mPebbleName;
  }

  public void setPebbleName(String name)
  {
    mPebbleName = name;
  }

}
