package com.liveensure.a.mini;

import com.liveensure.core.AgentFSM;
import com.liveensure.rest.api.AgentChallengeResponse;

public interface UIForFiniteStateMachine
{

  public void showBusyCues(boolean areWeBusy);

  public void showSuccess(String msg);

  public void showFailure();

  public void retryCurrentChallenge();

  public void showChallenge(AgentChallengeResponse chal);

  public void showError(String msg);

  public void showEmpty(int iconID);

  public void startNewSession();

  public AgentFSM getAgentFSM();

  public int getCurrentOrientation();

  public LEApplication getApp();

  public void sendSucessAlertToPebble();

  public boolean isDevHostScanned();

  public boolean isShowChallengeIndicators();

  public void setShowChallengeIndicators(boolean showChallengeIndicators);

  public void showHelp();

  public boolean isExitPending();

  public void setExitPending(boolean b);

}
