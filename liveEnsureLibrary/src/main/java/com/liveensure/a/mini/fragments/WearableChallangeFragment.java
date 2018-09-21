package com.liveensure.a.mini.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.liveensure.a.mini.R;
import com.liveensure.a.mini.UIForFiniteStateMachine;
import com.liveensure.core.AgentFSM;
import com.liveensure.pebble.PebbleClient;
import com.liveensure.rest.api.AgentChallengeResponse;
import com.liveensure.util.Log;
import com.liveensure.util.MessageDigester;
import com.liveensure.util.StringHelper;

public class WearableChallangeFragment extends BaseChallengeFragment
{
  private static final String EXTRA_MESSAGE = "challengeObject";
  private static final String TAG           = WearableChallangeFragment.class.getSimpleName();
  AgentChallengeResponse      mChallenge;
  private RelativeLayout      mLayout;
  private ImageView           mWearableChallengeGraphic;

  public static final WearableChallangeFragment newInstance(AgentChallengeResponse chal)
  {
    WearableChallangeFragment f = new WearableChallangeFragment();
    Bundle bdl = new Bundle(1);
    bdl.putSerializable(EXTRA_MESSAGE, chal);
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
    AgentChallengeResponse chal = (AgentChallengeResponse) getArguments().getSerializable(EXTRA_MESSAGE);
    if (chal != null) {
      mChallenge = chal;
      // make sure all the necessary challenge features are present
    }
    else {
      Log.e(TAG, "unable to display wearable challenge, challenbge object is null");
      return;
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.wearable_view, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);
  }

  @Override
  public void onResume()
  {
    super.onResume();

    mLayout = (RelativeLayout) mActivity.findViewById(R.id.wearable_challenge_fragment_layout);
    mWearableChallengeGraphic = (ImageView) mActivity.findViewById(R.id.wearable_challenge_graphic);
    float graphicAlpha = (ui.isShowChallengeIndicators()) ? 0.2f : 0f;
    mWearableChallengeGraphic.setAlpha(graphicAlpha);
    fadeInUI();
  }

  private void fadeInUI()
  {
    mLayout.setAlpha(0f);
    mLayout.setVisibility(View.VISIBLE);
    mLayout.animate().alpha(1.0f).setDuration(200).setListener(new AnimatorListenerAdapter()
    {
      @Override
      public void onAnimationEnd(Animator animation)
      {
        Log.w(TAG, "calling sendAnswer()");
        sendAnswer();
      }
    });
  }

  private void fadeOutUI()
  {
    mLayout.animate().alpha(0f).setDuration(200).setListener(new AnimatorListenerAdapter()
    {
      @Override
      public void onAnimationEnd(Animator animation)
      {
        mLayout.setVisibility(View.INVISIBLE);
      }
    });
  }

  private void sendAnswer()
  {

    String plainAnswer = "";
    String hashedAnswer = "unavailable";
    Log.w(TAG, "in sendAnswer()");
    AgentFSM fsm = ui.getAgentFSM();
    PebbleClient pebbleClient = new PebbleClient(ui.getApp());
    if (pebbleClient != null && pebbleClient.isPebbleConnected() && !StringHelper.isEmpty(pebbleClient.getPebbleHWID())) {
      plainAnswer = pebbleClient.getPebbleHWID();
      if (fsm.getSession() != null) hashedAnswer = MessageDigester.getHash(fsm.getSession().getSessionToken() + plainAnswer);
    }
    Log.w(TAG, "Final answer for pebble challenge: " + hashedAnswer);
    fsm.setPebbleChallengeAnswered(true);
    fadeOutUI();
    fsm.makeAnswerChallengeCallWithID(mChallenge.getChallengeID(), hashedAnswer, "2.0");
  }

  @Override
  public void retryChallenge()
  {
  }
}
