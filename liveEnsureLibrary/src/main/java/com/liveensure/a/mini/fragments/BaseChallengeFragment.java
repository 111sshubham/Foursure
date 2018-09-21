package com.liveensure.a.mini.fragments;

import android.app.Activity;
import android.app.Fragment;
import com.liveensure.a.mini.UIForFiniteStateMachine;
import com.liveensure.util.MessageDigester;

public abstract class BaseChallengeFragment extends Fragment
{

  protected Activity                mActivity;
  protected UIForFiniteStateMachine ui;

  public abstract void retryChallenge();

  public String sha1Session(String s)
  {
    String result = "unavailable";
    if (ui.getAgentFSM() != null && ui.getAgentFSM().getSession() != null) {
      result = MessageDigester.getHash(ui.getAgentFSM().getSession().getSessionToken() + s);
    }
    return result;
  }

}
