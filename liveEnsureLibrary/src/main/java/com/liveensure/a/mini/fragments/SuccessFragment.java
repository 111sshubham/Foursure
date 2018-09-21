package com.liveensure.a.mini.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.liveensure.a.mini.R;
import com.liveensure.a.mini.UIForFiniteStateMachine;
import com.liveensure.util.Log;
import com.liveensure.util.StringHelper;

public class SuccessFragment extends Fragment {
	private static final String EXTRA_MESSAGE = "msg";
	private static final String TAG = SuccessFragment.class.getSimpleName();
	private String mMessage;
	private Activity mActivity;
	private UIForFiniteStateMachine ui;

	public static final SuccessFragment newInstance(String message) {
		SuccessFragment f = new SuccessFragment();
		Bundle bdl = new Bundle(1);
		bdl.putString(EXTRA_MESSAGE, message);
		f.setArguments(bdl);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof UIForFiniteStateMachine) {
			mActivity = activity;
			ui = (UIForFiniteStateMachine) activity;
		} else {
			throw new ClassCastException("Invoking activity for this fragment must implement the UIForFiniteStateMachine interface");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String message = getArguments().getString(EXTRA_MESSAGE);
		if (!StringHelper.isEmpty(message)) {
			mMessage = message;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.success_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onResume() {
		Log.d(TAG, "in onResume()");
		super.onResume();
		if (StringHelper.isEmpty(mMessage)) {
			Log.e(TAG, "unable to get message string for success pane");
			mMessage = getResources().getString(R.string.success);
		}
		// add a handler to the liveensure drawer graphic
		ImageView leDrawerIcon = (ImageView) mActivity.findViewById(R.id.success_drawer_icon);
		leDrawerIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// cancel any pending exit handler
				if (ui.isExitPending())
					ui.setExitPending(false);
			}
		});

		// ImageView successGraphic = (ImageView) mActivity.findViewById(R.id.success_graphic);
		// if (ui.getAgentFSM().isPebbleChallengeAnswered()) {
		// successGraphic.setImageResource(R.drawable.status_success_pebble_512);
		// ui.sendSucessAlertToPebble();
		// }
		// else {
		// successGraphic.setImageResource(R.drawable.status_success_512);
		// }
	}
}
