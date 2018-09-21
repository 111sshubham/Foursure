package com.liveensure.a.mini.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.liveensure.a.mini.R;
import com.liveensure.util.Log;
import com.liveensure.util.StringHelper;

public class ErrorFragment extends Fragment
{
  private static final String EXTRA_MESSAGE = "msg";
  private static final String TAG           = ErrorFragment.class.getSimpleName();
  String                      mMessage      = "Error";

  public static final ErrorFragment newInstance(String message)
  {
    ErrorFragment f = new ErrorFragment();
    Bundle bdl = new Bundle(1);
    Log.i(TAG, "received an error message to display of: " + message);
    bdl.putString(EXTRA_MESSAGE, message);
    f.setArguments(bdl);
    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    String message = getArguments().getString(EXTRA_MESSAGE);
    if (!StringHelper.isEmpty(message)) {
      mMessage = message;
    }

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.error_view, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);
  }

  @Override
  public void onResume()
  {
    Log.d(TAG, "in onResume()");
    super.onResume();
    TextView f = (TextView) getView().findViewById(R.id.error_fragment_label);
    f.setText(mMessage);
  }
}
