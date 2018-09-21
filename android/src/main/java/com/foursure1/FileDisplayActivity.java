package com.foursure1;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.foursure1.api.AccessShareResponse;
import com.foursure1.util.ShareType;

public class FileDisplayActivity extends Activity {
	public static final String TAG = FileDisplayActivity.class.getSimpleName();
	private FourSureApplication myApp;
	private ImageView mBtnLogout;
	private ImageView mBtnGetAsset;
	private AccessShareResponse mShare;
	private TextView mTxtAssetValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myApp = (FourSureApplication) getApplication();
		setContentView(R.layout.activity_file_display);
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "in onResume()");
		super.onResume();
		mTxtAssetValue = (TextView) findViewById(R.id.share_asset_value);
		mBtnGetAsset = (ImageView) findViewById(R.id.btn_get_asset);
		mBtnLogout = (ImageView) findViewById(R.id.btn_receive_cancel);
		mShare = myApp.getShareToAccess();
		resetUI();
	}

	private void resetUI() {
		mBtnGetAsset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mShare == null) {
					// stuff
				} else {
					if (mShare.getShareType() == ShareType.URL) {
						openWebPage(mShare.getShareContent());
						finish();
					}
					if (mShare.getShareType() == ShareType.TEXT) {
						// first, see if we can extract any URLs
						ArrayList<String> possibleLinks = pullLinks(mShare.getShareContent());
						if (possibleLinks != null && possibleLinks.size() > 0) {
							openWebPage(possibleLinks.get(0));
						} else {
							openTextPage(mShare.getShareContent());
						}
					}
				}
			}
		});
		mBtnLogout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				myApp.resetFsSession();
				myApp.clearCurrentShareDetails();
				Intent intent = new Intent(myApp, FrontPageActivity.class);
				startActivity(intent);
				finish();
			}
		});
		mTxtAssetValue.setText(mShare.getShareContent());
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

	public void openWebPage(String url) {
		Uri webpage = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivity(intent);
		} else {
			Toast.makeText(getApplicationContext(), "Unable to find a handler for " + url, Toast.LENGTH_LONG).show();
		}
	}

	public void openTextPage(String text) {
		mTxtAssetValue.setText(text);
		// copy text to clipboard and show a toast
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("", text);
		clipboard.setPrimaryClip(clip);
		Toast.makeText(getApplicationContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
	}

	// Pull all links from the body for easy retrieval
	private ArrayList<String> pullLinks(String text) {
		ArrayList<String> links = new ArrayList<String>();

		String regex = "(?:^|[\\W])(http(s?):\\/\\/|www\\.)" + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*" + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = p.matcher(text);
		while (m.find()) {
			int matchStart = m.start(1);
			int matchEnd = m.end();
			String urlStr = text.substring(matchStart, matchEnd);
			links.add(urlStr);
		}
		return links;
	}
}
