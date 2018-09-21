package com.liveensure.a.mini.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.liveensure.a.mini.R;
import com.liveensure.a.mini.UIForFiniteStateMachine;
import com.liveensure.core.AgentFSM;
import com.liveensure.rest.api.AgentChallengeResponse;
import com.liveensure.util.Log;
import com.liveensure.util.StringHelper;

public class PromptChallengeFragment extends BaseChallengeFragment
{
  private static final String EXTRA_MESSAGE = "challengeObject";
  private static final String TAG           = PromptChallengeFragment.class.getSimpleName();
  AgentChallengeResponse      mChallenge;
  private String              mPromptText;
  private EditText            mAnswerInput;
  private Button              mBtnSend;
  private RelativeLayout      mLayout;
  private boolean             mKeepLayoutVisible;

  public static final PromptChallengeFragment newInstance(AgentChallengeResponse chal)
  {
    PromptChallengeFragment f = new PromptChallengeFragment();
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
    if (getArguments() != null) {
      AgentChallengeResponse chal = (AgentChallengeResponse) getArguments().getSerializable(EXTRA_MESSAGE);
      if (chal != null) {
        mChallenge = chal;
        // make sure all the necessary challenge features are present
        if (chal.getChallengeDetails() == null) {
          Log.e(TAG, "unable to get details for challenge prompt");
          return;
        }
        mPromptText = "";
        if (chal.getChallengeDetails().containsKey("prompt")) mPromptText = (String) chal.getChallengeDetails().get("prompt");
        if (StringHelper.isEmpty(mPromptText)) {
          Log.e(TAG, "unable to get prompt string for challenge prompt");
          return;
        }
      }
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.prompt_view, container, false);
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

    mLayout = (RelativeLayout) mActivity.findViewById(R.id.prompt_fragment_layout);
    TextView promptText = (TextView) mActivity.findViewById(R.id.prompt_fragment_label);
    if (promptText == null) {
      Log.e(TAG, "in onResume(), layout element promptText is null.  Returning");
      return;
    }
    promptText.setText(mPromptText);
    mAnswerInput = (EditText) mActivity.findViewById(R.id.prompt_fragment_input);
    mAnswerInput.setOnEditorActionListener(new OnEditorActionListener()
    {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
      {
        boolean handled = false;
        Log.w(TAG, "received an editor action: " + actionId);
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
          sendAnswer();
          handled = true;
        }
        return handled;
      }
    });
    mBtnSend = (Button) mActivity.findViewById(R.id.prompt_fragment_next_button);
    mBtnSend.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View view)
      {
        sendAnswer();
      }
    });
    if (mAnswerInput.requestFocus()) {
      mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
      InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.showSoftInput(mAnswerInput, InputMethodManager.SHOW_IMPLICIT);
    }
    else {
      Log.w(TAG, "mAnswerInput is unable to get focus");
    }
    if (mLayout.getVisibility() != View.VISIBLE) fadeInUI();
  }

  private void sendAnswer()
  {
    // hide the keyboard
    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(mAnswerInput.getWindowToken(), 0);
    if (StringHelper.isEmpty(mAnswerInput.getText().toString())) return; // only process a non-empty answer
    // pass the answer text to the FSM
    AgentFSM fsm = ui.getAgentFSM();
    String plainAnswer = mAnswerInput.getText().toString();
    String hashedAnswer = sha1Session(plainAnswer);
    ui.showBusyCues(true);
    fadeOutUI();
    fsm.makeAnswerChallengeCallWithID(mChallenge.getChallengeID(), hashedAnswer, "2.0");
  }

  private void fadeInUI()
  {
    Log.w(TAG, "in fadeInUI()");
    mKeepLayoutVisible = true;
    mLayout.setAlpha(0f);
    mLayout.setVisibility(View.VISIBLE);
    mLayout.animate().alpha(1.0f).setDuration(200).setListener(null).start();
  }

  private void fadeOutUI()
  {
    mKeepLayoutVisible = false;
    mLayout.animate().alpha(0f).setDuration(200).setListener(new AnimatorListenerAdapter()
    {
      @Override
      public void onAnimationEnd(Animator animation)
      {
        if (mKeepLayoutVisible) {
          Log.w(TAG, "in onAnimationEnd(), but mKeepLayoutVisible is true, not setting layout to invisible");
        }
        else {
          mLayout.setVisibility(View.INVISIBLE);
        }
      }
    }).start();
  }

  @Override
  public void retryChallenge()
  {
    Log.w(TAG, "in retryChallenge()");
    mBtnSend.setText(getResources().getString(R.string.btn_label_retry));
    mAnswerInput.setText("");
    if (mAnswerInput.requestFocus()) {
      mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
      InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.showSoftInput(mAnswerInput, InputMethodManager.SHOW_IMPLICIT);
    }
    else {
      Log.w(TAG, "mAnswerInput is unable to get focus");
    }
    fadeInUI();
  }
}
