package com.foursure1;


public interface UIForFiniteStateMachine {

	public void showBusyCues(boolean areWeBusy);

	public void showSuccess(String msg);

	public void handleError(String msg);

	public void startNewSession();

	public FourSureFSM getFourSureFSM();

	public FourSureApplication getApp();

	public boolean isExitPending();

	public void setExitPending(boolean b);

	public void launchLiveEnsure();
	
	public void handleStatusResult(int status);

	public void showUpgrade(String statusMessage);

}
