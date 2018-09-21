package com.liveensure.a.mini.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.liveensure.a.mini.R;
import com.liveensure.a.mini.UIForFiniteStateMachine;
import com.liveensure.util.Log;

public class HelpFragment extends Fragment
{

  private static final String     TAG = HelpFragment.class.getSimpleName();
  protected int                   mTapCount;
  private Activity                mActivity;
  private UIForFiniteStateMachine ui;

  public static HelpFragment newInstance()
  {
    HelpFragment f = new HelpFragment();
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
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.help_view, container, false);
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
    mTapCount = 0;

    // update the app version label in the bottom right
    TextView lblVersion = (TextView) mActivity.findViewById(R.id.help_app_version_label);
    lblVersion.setText(ui.getApp().getProgramVersion());
    //    if (ui.isDevHostScanned()) {
    //      lblVersion.setOnClickListener(new OnClickListener()
    //      {
    //
    //        @Override
    //        public void onClick(View v)
    //        {
    //          mTapCount++;
    //          if (mTapCount == 3 && ui.isDevHostScanned()) {
    //            ui.showDeveloper();
    //          }
    //        }
    //      });
    //    }

    // add a handler to the liveensure drawer graphic
    //    ImageView leDrawerIcon = (ImageView) mActivity.findViewById(R.id.help_drawer_icon);
    //    leDrawerIcon.setOnClickListener(new OnClickListener()
    //    {
    //      @Override
    //      public void onClick(View v)
    //      {
    //        ui.showDrawer();
    //      }
    //    });

    // click handlers for our help buttons (open default browser and direct to the appropriate URL)
    Button btnTutorial = (Button) mActivity.findViewById(R.id.help_tutorial_button);
    btnTutorial.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        goToUrl("http://assets.liveensure.com/mobile_help_v5/index.html#tutorial");
      }
    });
    Button btnOptions = (Button) mActivity.findViewById(R.id.help_options_button);
    btnOptions.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        goToUrl("http://assets.liveensure.com/mobile_help_v5/index.html#options");
      }
    });
    Button btnSupport = (Button) mActivity.findViewById(R.id.help_support_button);
    btnSupport.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        goToUrl("http://assets.liveensure.com/mobile_help_v5/index.html#support");
      }
    });

  }

  private void goToUrl(String url)
  {
    Uri uriUrl = Uri.parse(url);
    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
    startActivity(launchBrowser);
  }
}
