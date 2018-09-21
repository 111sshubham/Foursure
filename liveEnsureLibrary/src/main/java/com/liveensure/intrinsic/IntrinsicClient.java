package com.liveensure.intrinsic;


public class IntrinsicClient
{

  private static final String LE_INTRINSIC_PREFS      = "le.intrinsic.prefs";
  private static final String INTRINSIC_SYMMETRIC_KEY = "symmetric.key";
  private static final String INTRINSIC_AUTH_CODE     = "auth.code";
  protected String            TAG                     = IntrinsicClient.class.getSimpleName();
//
//  protected KeyManagement     mKeyManager;
//  private AuthenticationCode  mAuthCode;
//  private Context             mContext;
//  private SymmetricKey        mSymmetricKey;
  private boolean             mIntrinsicAvailable;
//  // IntrinsicID says we don't need this once the auth code is generated
//  private boolean             storeSymmetricKey       = false;
//
//  public IntrinsicClient(final Context context)
//  {
//    this.mContext = context;
//    new AsyncTask<Void, Void, KeyManagement>()
//    {
//      @Override
//      protected void onPreExecute()
//      {
//      }
//
//      @Override
//      protected KeyManagement doInBackground(Void... params)
//      {
//        try {
//          Log.w(TAG, "creating intrinsic key management object");
//          KeyManagement km = KeyManagementFactory.getInstance().create(context);
//          Log.w(TAG, "done creating intrinsic key management object");
//          return km;
//        }
//        catch (Exception e) {
//          Log.e(TAG, "Exception generated during key manager creation: " + e);
//          return null;
//        }
//      }
//
//      @Override
//      protected void onPostExecute(KeyManagement keyManagement)
//      {
//        mKeyManager = keyManagement;
//        Log.w(TAG, "created key manager object: " + mKeyManager);
//      }
//    }.execute();
//  }
//
//  private SymmetricKey generateSymmetricKey()
//  {
//    if (mKeyManager == null) {
//      Log.w(TAG, "key manager is null, unable to generate a symmetric key");
//      return null;
//    }
//    try {
//      Log.w(TAG, "creating intrinsic symmetric key");
//      SymmetricKey key = mKeyManager.generateSymmetricKey(KeyLength.BITS_256, LogInfo.getDefault());
//      Log.w(TAG, "done creating intrinsic symmetric key");
//      return key;
//    }
//    catch (SSFNativeException e) {
//      Log.e(TAG, "SSFNativeException during symmetric key generation: " + e.getLocalizedMessage());
//      return null;
//    }
//    catch (SSFException e) {
//      Log.e(TAG, "SSFException during symmetric key generation: " + e.getLocalizedMessage());
//      return null;
//    }
//  }
//
//  private AuthenticationCode generateAuthenticationCode(SymmetricKey symmetricKey)
//  {
//    if (mKeyManager == null) return null;
//    try {
//      Log.w(TAG, "creating intrinsic authentication code");
//      AuthenticationCode ac = mKeyManager.generateAuthenticationCode(symmetricKey, LogInfo.getDefault());
//      Log.w(TAG, "done creating intrinsic authentication code");
//      return ac;
//    }
//    catch (SSFNativeException e) {
//      Log.e(TAG, "SSFNativeException during authentication code generation: " + e.getLocalizedMessage());
//      return null;
//    }
//    catch (SSFException e) {
//      Log.e(TAG, "SSFException during authentication code generation: " + e.getLocalizedMessage());
//      return null;
//    }
//  }
//
//  /**
//   * get the authentication code. This is a secret, device-specific value from Intrinsic ID that need never be sent to the server, but can be used to validate a
//   * session-specific random value via a shared key that both the device and the server have.
//   * 
//   * @return
//   */
//  public AuthenticationCode getAuthenticationCode()
//  {
//    // lazy loading
//    if (mAuthCode != null) return mAuthCode;
//
//    // we don't have our key and auth code yet. first, check for stored data
//
//    String authCodeBase64 = null;
//
//    SharedPreferences prefs = mContext.getSharedPreferences(LE_INTRINSIC_PREFS, Context.MODE_PRIVATE);
//    if (prefs != null) {
//      authCodeBase64 = prefs.getString(INTRINSIC_AUTH_CODE, null);
//      Log.i(TAG, "Retrieved an intrinsic ID auth code (base64) from existing shared prefs file: " + authCodeBase64);
//      if (!StringHelper.isEmpty(authCodeBase64)) mAuthCode = new AuthenticationCode(authCodeBase64);
//    }
//    else {
//      Log.w(TAG, "prefs is null, not retrieving auth code");
//    }
//
//    if (mAuthCode == null) {
//      // still don't have an auth code.  we need to generate one now
//      mSymmetricKey = getSymmetricKey();
//      if (mSymmetricKey != null)
//        mAuthCode = generateAuthenticationCode(mSymmetricKey);
//      else
//        Log.w(TAG, "symmetric key is null, not able to generate auth code");
//      if (mAuthCode != null) {
//        authCodeBase64 = mAuthCode.toBase64();
//        SharedPreferences.Editor editor = mContext.getSharedPreferences(LE_INTRINSIC_PREFS, Context.MODE_PRIVATE).edit();
//        editor.putString(INTRINSIC_AUTH_CODE, authCodeBase64);
//        editor.commit();
//        Log.i(TAG, "Generated a new intrinsic ID auth code " + authCodeBase64 + " and saved to prefs file " + LE_INTRINSIC_PREFS);
//      }
//      else
//        Log.e(TAG, "auth code is null, unable to retrieve from prefs or generate one");
//    }
//
//    return mAuthCode;
//  }
//
//  public SymmetricKey getSymmetricKey()
//  {
//    if (mSymmetricKey != null) return mSymmetricKey;
//
//    String symmetricKeyBase64 = null;
//    SharedPreferences prefs = mContext.getSharedPreferences(LE_INTRINSIC_PREFS, Context.MODE_PRIVATE);
//    if (prefs != null) {
//      symmetricKeyBase64 = prefs.getString(INTRINSIC_SYMMETRIC_KEY, null);
//      Log.i(TAG, "Retrieved an intrinsic ID symmetric key (base64) from existing shared prefs file: " + symmetricKeyBase64);
//      if (!StringHelper.isEmpty(symmetricKeyBase64)) mSymmetricKey = new SymmetricKey(symmetricKeyBase64);
//    }
//    else
//      Log.w(TAG, "prefs is null, not retrieving symmetric key");
//
//    if (mSymmetricKey == null) {
//      // don't have a symmetric key.  we need to generate one now
//      mSymmetricKey = generateSymmetricKey();
//      if (mSymmetricKey != null && storeSymmetricKey) {
//        SharedPreferences.Editor editor = mContext.getSharedPreferences(LE_INTRINSIC_PREFS, Context.MODE_PRIVATE).edit();
//        editor.putString(INTRINSIC_SYMMETRIC_KEY, mSymmetricKey.toBase64());
//        editor.commit();
//      }
//      else {
//        if (mSymmetricKey == null) Log.w(TAG, "symmetric key is null, unable to generate one");
//      }
//    }
//    return mSymmetricKey;
//  }
//
//  /**
//   * take an auth challenge object (usually a wrapper around some random bytes) and sign it using the auth code stored on this device. return the response
//   * object for verification on the server side
//   * 
//   * @param authenticationChallenge
//   * @return an AuthenticationResponse object
//   */
//  public AuthenticationResponse authenticate(AuthenticationChallenge authenticationChallenge)
//  {
//    if (mKeyManager == null) return null;
//    if (authenticationChallenge == null) return null;
//    AuthenticationCode ac = getAuthenticationCode();
//    if (ac == null) {
//      Log.w(TAG, "unable to generate an authentication code");
//      return null;
//    }
//    try {
//      return mKeyManager.authenticate(ac, authenticationChallenge, LogInfo.getDefault());
//    }
//    catch (SSFNativeException e) {
//      Log.e(TAG, "SSFNativeException during authenticate: " + e.getLocalizedMessage());
//      return null;
//    }
//    catch (SSFException e) {
//      Log.e(TAG, "SSFException during authenticate: " + e.getLocalizedMessage());
//      return null;
//    }
//  }
//
//  public HashMap<String, String> getRegistrationInfoAsHashMap()
//  {
//    HashMap<String, String> resp = new HashMap<String, String>();
//    SymmetricKey sk = getSymmetricKey();
//    getAuthenticationCode();
//    if (!mIntrinsicAvailable || sk == null) {
//      resp.put("registrationAvailable", "false");
//    }
//    else {
//      resp.put("registrationAvailable", "true");
//      resp.put("symmetricKey", sk.toBase64());
//    }
//    return resp;
//  }

  public boolean isIntrinsicAvailable()
  {
    return mIntrinsicAvailable;
  }

  public void setIntrinsicAvailable(boolean mIntrinsicAvailable)
  {
    this.mIntrinsicAvailable = mIntrinsicAvailable;
  }

}
