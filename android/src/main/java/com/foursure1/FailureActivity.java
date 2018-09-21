package com.foursure1;

import com.liveensure.util.StringHelper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FailureActivity extends Activity {
	public static final String TAG = FailureActivity.class.getSimpleName();
	private FourSureApplication myApp;
	public Bitmap mImgBitMap;
	private TextView mTxtMessage;
	private ImageView mImgFailed;
	private ImageView mBtnOK;
	private ImageView mBtnHelp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myApp = (FourSureApplication) getApplication();
		setContentView(R.layout.activity_failed);
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "in onResume()");
		super.onResume();
		mTxtMessage = (TextView) findViewById(R.id.lbl_error_message);
		mImgFailed = (ImageView) findViewById(R.id.img_graphic_failed);
		mBtnOK = (ImageView) findViewById(R.id.failed_ok);
		mBtnHelp = (ImageView) findViewById(R.id.failed_help);
		resetUI();
	};

	private void resetUI() {
		String fMsg = "FAILED";
		if (!StringHelper.isEmpty(myApp.getCurrentErrorMessage()))
			fMsg = myApp.getCurrentErrorMessage();
		mTxtMessage.setText(fMsg);
		myApp.setCurrentErrorMessage(""); // clear error message
		myApp.clearCurrentShareDetails();
		mTxtMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				myApp.setLiveEnsureAuthenticated(false);
				finish();
			}
		});
		mImgFailed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				myApp.setLiveEnsureAuthenticated(false);
				finish();
			}
		});
		mBtnOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				myApp.setLiveEnsureAuthenticated(false);
				finish();
			}
		});
		mBtnHelp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri webpage = Uri.parse(FourSureApplication.FOURSHARE_HELP_URL);
				Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
				if (intent.resolveActivity(getPackageManager()) != null) {
					startActivity(intent);
				} else {
					Toast.makeText(getApplicationContext(), "Unable to find a handler for " + FourSureApplication.FOURSHARE_HELP_URL, Toast.LENGTH_LONG).show();
				}
			}
		});

	}

	@Override
	protected void onPause() {
		Log.e(TAG, "in onPause()");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.e(TAG, "in onDestroy()");
		super.onPause();
	}

}