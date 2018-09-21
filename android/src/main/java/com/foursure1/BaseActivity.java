package com.foursure1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

public class BaseActivity extends Activity implements UIForFiniteStateMachine {

	public static final String TAG = ReceiveAssetActivity.class.getSimpleName();

	@Override
	public void showBusyCues(boolean areWeBusy) {
	}

	@Override
	public void showSuccess(String msg) {
	}

	@Override
	public void handleError(String msg) {
	}

	@Override
	public void startNewSession() {
	}

	@Override
	public FourSureFSM getFourSureFSM() {
		return null;
	}

	@Override
	public FourSureApplication getApp() {
		return null;
	}

	@Override
	public boolean isExitPending() {
		return false;
	}

	@Override
	public void setExitPending(boolean b) {
	}

	@Override
	public void launchLiveEnsure() {
	}

	@Override
	public void handleStatusResult(int status) {
	}

	@Override
	public void showUpgrade(String statusMessage) {
		AlertDialog.Builder dialogUpdate = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle("New Version Available")
				.setIcon(getResources().getDrawable(android.R.drawable.stat_sys_download)).setMessage(statusMessage).setCancelable(true)
				.setNeutralButton("UPDATE", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
						} catch (ActivityNotFoundException e) {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
						}
					}
				});
		AlertDialog updatedialog = dialogUpdate.create();
		updatedialog.show();
	}

}
