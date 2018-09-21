package com.liveensure.shuffler;

import java.util.HashMap;
import android.content.Context;
import com.liveensure.util.MessageDigester;

/**
 * The Class HardwareUIDBase. This class is an abstract class, it is meant to be extended by a platform-specific instance of the
 * {@link com.liveensure.agents.util.impl.rim.Shuffler} class. It contains some helper methods and some constants meant to be used when identifying various
 * hardware points for various platforms.
 * 
 * @author aapel
 * @version 1.0
 */
public abstract class HardwareUIDBase
{

  /** Hard Drive Serial Number */
  public static final int            DRIVE_SERIAL     = 0;

  /** CPU ID */
  public static final int            CPU_ID           = 1;

  /** Total bytes of memory */
  public static final int            BYTES_RAM        = 2;

  /** MAC Address of the primary network interface */
  public static final int            PRIMARY_MAC_ADDR = 3;

  /** Video Chipset installed on this system */
  public static final int            VIDEO_CHIPSET    = 4;

  /** Motherboard Chipset installed on this system. */
  public static final int            MBOARD_CHIPSET   = 5;

  /** ID of the BIOS Chipset installed on this system */
  public static final int            BIOS_ID          = 6;

  // <String,Integer>
  protected HashMap<String, Integer> devices          = new HashMap<String, Integer>();

  protected Context                  context;

  /**
   * Instantiates a new hardware uid base. Only meant to be used by the {@link com.liveensure.agents.util.impl.rim.Shuffler} class
   */
  public HardwareUIDBase()
  {
    init();
  }

  /**
   * Loads up the device IDs uniquely.
   */
  protected void init()
  {
    devices.put("hw1", Integer.valueOf(DRIVE_SERIAL));
    devices.put("hw2", Integer.valueOf(CPU_ID));
    devices.put("hw3", Integer.valueOf(BYTES_RAM));
    devices.put("hw4", Integer.valueOf(PRIMARY_MAC_ADDR));
    devices.put("hw5", Integer.valueOf(VIDEO_CHIPSET));
    devices.put("hw6", Integer.valueOf(MBOARD_CHIPSET));
    devices.put("hw7", Integer.valueOf(BIOS_ID));
  }

  /**
   * Required method to be implemented by all derived types. This method should do the work of filling up the devices hash map.
   * 
   * @param idFlags
   *          flagged values to return
   * 
   * @return the hardware i ds
   */
  public abstract String[] getHardwareIDs(int[] idFlags);

  /**
   * Gets the hardware ids for the given flags.
   * 
   * @param idFlags
   *          the id flags
   * 
   * @return hardware ids
   */
  public String[] getHardwareIDs(String[] idFlags)
  {
    int[] idFlagsInt = new int[idFlags.length];
    for (int i = 0; i < idFlags.length; i++) {
      idFlagsInt[i] = ((Integer) devices.get(idFlags[i])).intValue();
    }
    return getHardwareIDs(idFlagsInt);
  }

  /**
   * Gets all the hardware ids available.
   * 
   * @return Array of Strings with all available hardware ID's for this machine
   */
  public String[] getAllHardwareIDs()
  {
    return getHardwareIDs(new int[] {
        DRIVE_SERIAL, CPU_ID, BYTES_RAM, PRIMARY_MAC_ADDR, VIDEO_CHIPSET, MBOARD_CHIPSET, BIOS_ID
    });
  }

  /**
   * Gets the hardware id for the given flag.
   * 
   * @param idFlag
   *          an int specifying which Hardware ID to return. It is recommended to use the constants defined in this class.
   * 
   * @return String containing the specified ID point
   */
  public String getHardwareID(int idFlag)
  {
    String hardwareID = null;
    int[] idFlags = {
      idFlag
    };
    String[] hardwareIDs = getHardwareIDs(idFlags);
    if (hardwareIDs.length > 0) {
      hardwareID = hardwareIDs[0];
    }
    return hardwareID;
  }

  /**
   * Gets the hardware id for the given flag.
   * 
   * @param idFlag
   *          a String specifying which Hardware ID to return. It is expected that the String will be parseable into an int value using Integer.intValue()
   * 
   * @return String containing the specified ID point
   */
  public String getHardwareID(String idFlag)
  {

    int idFlagInt = ((Integer) devices.get(idFlag)).intValue();
    return safeString(getHardwareID(idFlagInt));
  }

  /**
   * Safe string. Remove any potentially problematic characters from a string, and guarantee that the string will be at least 2 characters long.
   * 
   * @param s
   *          The String to be cleaned up
   * 
   * @return String with the : or / characters turned into underscores (_), or "_empty" if <code>s</code> is null or "", or <code>s</code> preceded by "__" if
   *         the length was less than 3 characters
   */
  protected String safeString(String s)
  {
    if (s == null || s.length() == 0) {
      return "_empty";
    }
    s = s.replace(':', '_');
    s = s.replace('/', '_');
    s = s.replace(';', '_');
    if (s.length() < 3) {
      s = "__" + s;
    }
    return s;
  }

  /**
   * Gets the Full UUID for this device.
   * 
   * @return FullUID, a 32 byte hex string containing the unique fingerprint of this specific device
   */
  public String getFullUID()
  {

    String[] ids = getHardwareIDs(new int[] {
        DRIVE_SERIAL, CPU_ID, BYTES_RAM, PRIMARY_MAC_ADDR, VIDEO_CHIPSET, MBOARD_CHIPSET, BIOS_ID
    });
    StringBuffer idString = new StringBuffer(32);
    for (int i = 0; i < ids.length; i++) {
      idString.append(MessageDigester.getHash(ids[i]).substring(0, 4));
    }
    String filler = MessageDigester.getHash("Non Illegitimus Carborundum");
    if (idString.length() < 32) {
      idString.append(filler.substring(0, 32 - idString.length()));
    }

    return idString.toString();
  }

  /**
   * Checks if this instance of Shuffler is initialized.
   * 
   * @return true if this instance is initialized and false if the instance is not. It is possible for a platform-specific instance of the
   *         {@link com.liveensure.agents.util.impl.rim.Shuffler} class to fail to load the native library that is often used for hardware ID collection. In
   *         that case the initialized flag will be false. This can be used by consumers of the Shuffler class to determine if actual hardware IDs will be
   *         available
   */
  public boolean isInitialized()
  {
    return true;
  }

  public void setContext(Context ctx)
  {
    context = ctx;
  }

  public HashMap<String, String> getAnswersAsHashMap()
  {
    HashMap<String, String> resp = new HashMap<String, String>();
    for (String k : devices.keySet()) {
      resp.put(k, getHardwareID(k));
    }
    return resp;
  }

  public abstract void resetSoftwareInstallNonce();
}
