package com.liveensure.a.mini.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.liveensure.a.mini.R;
import com.liveensure.a.mini.UIForFiniteStateMachine;
import com.liveensure.util.Log;

public class EmptyFragment extends Fragment
{

  private static final String     TAG           = EmptyFragment.class.getSimpleName();
  private static final String     EXTRA_ICON_ID = "iconid";
  private int                     mIconID;
  private ImageView               mImage;
  private Activity                mActivity;
  private UIForFiniteStateMachine ui;

  public static EmptyFragment newInstance(int statusIconID)
  {
    EmptyFragment f = new EmptyFragment();
    Bundle bdl = new Bundle(1);
    bdl.putInt(EXTRA_ICON_ID, statusIconID);
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
    int iconID = 0;
    if (getArguments() != null) {
      iconID = getArguments().getInt(EXTRA_ICON_ID);
    }
    else {
      Log.w(TAG, "getArguments() is null");
    }
    mIconID = iconID;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.empty_view, container, false);
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
    mImage = (ImageView) mActivity.findViewById(R.id.empty_view_graphic);

    // create and add the supplied graphic
    if (mIconID > 0 && mImage != null) {
      mImage.setImageResource(mIconID);
      mImage.setVisibility(View.VISIBLE);
    }
    else {
      // the onResume() method is called when the parent activity is resumed (via the super.onResume() call in the activity's onResume())
      if (mImage != null) mImage.setVisibility(View.INVISIBLE);
    }
//    // create and add the liveensure drawer graphic
//    ImageView leDrawerIcon = (ImageView) getView().findViewById(R.id.empty_drawer_icon);
//    // set up a touch listener so that if the image is tapped we stop the camera preview
//    leDrawerIcon.setOnClickListener(new OnClickListener()
//    {
//      @Override
//      public void onClick(View v)
//      {
//        ui.showDrawer();
//      }
//    });

  }

}
