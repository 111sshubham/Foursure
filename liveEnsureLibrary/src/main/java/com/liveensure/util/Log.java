package com.liveensure.util;

public class Log
{
  static boolean enabled = false;

  public static void setEnabled(boolean enabled) {
    Log.enabled = enabled;
  }

  public static int v(String tag, String message)
  {
    if (!enabled) return -1;
    return android.util.Log.v(tag, message);
  }

  public static int d(String tag, String message)
  {
    if (!enabled) return -1;
    return android.util.Log.d(tag, message);
  }

  public static int i(String tag, String message)
  {
    if (!enabled) return -1;
    return android.util.Log.i(tag, message);
  }

  public static int w(String tag, String message)
  {
    if (!enabled) return -1;
    return android.util.Log.w(tag, message);
  }

  public static int w(String tag, String message, Throwable t)
  {
    if (!enabled) return -1;
    return android.util.Log.w(tag, message, t);
  }

  public static int e(String tag, String message)
  {
    if (!enabled) return -1;
    return android.util.Log.e(tag, message);
  }

  public static int e(String tag, String message, Throwable t)
  {
    if (!enabled) return -1;
    return android.util.Log.e(tag, message, t);
  }

}
