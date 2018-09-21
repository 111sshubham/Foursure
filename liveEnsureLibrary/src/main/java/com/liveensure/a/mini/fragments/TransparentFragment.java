package com.liveensure.a.mini.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.liveensure.a.mini.R;

public class TransparentFragment extends Fragment
{

  public static TransparentFragment newInstance()
  {
    TransparentFragment f = new TransparentFragment();
    return f;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.transparent_view, container, false);
  }

  @Override
  public void onResume()
  {
    super.onResume();
  }

}
