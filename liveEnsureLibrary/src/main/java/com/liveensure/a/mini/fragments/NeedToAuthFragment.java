package com.liveensure.a.mini.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import com.liveensure.a.mini.R;
import com.liveensure.a.mini.UIForFiniteStateMachine;
import com.liveensure.util.Log;

public class NeedToAuthFragment extends Fragment
{

  private static final String     TAG = NeedToAuthFragment.class.getSimpleName();
  private UIForFiniteStateMachine ui;

  public static NeedToAuthFragment newInstance()
  {
    NeedToAuthFragment f = new NeedToAuthFragment();
    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onAttach(Activity activity)
  {
    super.onAttach(activity);
    if (activity instanceof UIForFiniteStateMachine) {
      ui = (UIForFiniteStateMachine) activity;
    }
    else {
      throw new ClassCastException("Invoking activity for this fragment must implement the UIForFiniteStateMachine interface");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.need_auth_view, container, false);
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

    // create and add the liveensure drawer graphic
    //    ImageView leDrawerIcon = (ImageView) getView().findViewById(R.id.need_auth_drawer_icon);
    //    // set up a touch listener so that if the image is tapped we stop the camera preview
    //    leDrawerIcon.setOnClickListener(new OnClickListener()
    //    {
    //      @Override
    //      public void onClick(View v)
    //      {
    //        ui.showDrawer();
    //      }
    //    });
    Button btnNewSession = (Button) getActivity().findViewById(R.id.need_auth_new_session_btn);
    btnNewSession.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View v)
      {
        ui.startNewSession();
      }
    });
  }

}
