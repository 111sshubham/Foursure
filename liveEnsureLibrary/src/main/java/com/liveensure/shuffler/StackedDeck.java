package com.liveensure.shuffler;


public class StackedDeck extends HardwareUIDBase
{
  private String[] hw = new String[] {
      "DriveSerial_", "CPUID_", "BytesRam_", "MacAddr_", "VideoChipset_", "Motherboard_", "BIOS_"
                      };

  public StackedDeck()
  {
    super();
  }

  public StackedDeck(String uniqueHexID)
  {
    super();

    // append the current iteration number to each hardware ID point
    // for uniqueness and consistency
    // e.g. DriveSerial_1000, BIOS_1000, etc
    for (int i = 0; i < hw.length; i++) {
      hw[i] += uniqueHexID;
    }
  }

  /**
   * Gets the bogus hardware ids for the given flags.
   * 
   * @param idFlags
   * @return hardware ids
   */
  public String[] getHardwareIDs(int[] idFlags)
  {
    String[] res = new String[idFlags.length];
    for (int i = 0; i < idFlags.length; i++) {
      res[i] = hw[idFlags[i]];
    }
    return res;
  }

  /**
   * allow a stacked deck to be manipulated
   * 
   * @param i
   *          - integer (0-6) of index to change
   * @param s
   *          - new value to return
   */
  public void setHardwareID(int i, String s)
  {

    if (s != null && i > 0 && i < hw.length) {
      hw[i] = s;
    }

  }

  @Override
  public void resetSoftwareInstallNonce()
  {
    // no-op
  }

}
