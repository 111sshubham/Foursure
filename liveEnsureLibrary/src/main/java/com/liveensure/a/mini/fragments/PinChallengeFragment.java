package com.liveensure.a.mini.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.liveensure.a.mini.R;
import com.liveensure.a.mini.UIForFiniteStateMachine;
import com.liveensure.core.AgentFSM;
import com.liveensure.rest.api.AgentChallengeResponse;
import com.liveensure.util.Log;

public class PinChallengeFragment extends BaseChallengeFragment {
	private static final String EXTRA_CHALLENGE = "challengeObject";
	private static final String TAG = PinChallengeFragment.class.getSimpleName();
	AgentChallengeResponse mChallenge;
	private int mPinLength;
	private boolean mMalformedChallenge;
	private String[] mNumPadArray = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "del" };
	private int mCurrentDigit;
	private TextView[] mDigitInputs;
	private StringBuffer mEnteredPin;
	protected boolean mSendingPin;

	public static final BaseChallengeFragment newInstance(AgentChallengeResponse chal) {
		BaseChallengeFragment f = new PinChallengeFragment();
		Bundle bdl = new Bundle(1);
		bdl.putSerializable(EXTRA_CHALLENGE, chal);
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
		mMalformedChallenge = false;
		mCurrentDigit = 1;
		AgentChallengeResponse chal = (AgentChallengeResponse) getArguments().getSerializable(EXTRA_CHALLENGE);
		if (chal != null) {
			mChallenge = chal;
			// make sure all the necessary challenge features are present
			if (chal.getChallengeDetails() == null || !chal.getChallengeDetails().containsKey("length")) {
				Log.e(TAG, "unable to get required details for pin challenge");
				mMalformedChallenge = true;
				return;
			}
			mPinLength = 0;
			try {
				mPinLength = Integer.parseInt((String) chal.getChallengeDetails().get("length"));
			} catch (NumberFormatException e) {
				Log.e(TAG, "unable to convert server response of pin length to int");
			}
			if (mPinLength == 0) {
				Log.e(TAG, "unable to get pin length for pin challenge");
				mMalformedChallenge = true;
				return;
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.pin_challenge_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onResume() {
		Log.d(TAG, "in onResume()");
		super.onResume();
		if (mMalformedChallenge) {
			Log.w(TAG, "PIN challenge is malformed, unable to show challenge. Redirecting to error view");
			ui.showError(getResources().getString(R.string.error_fragment_text));
			return;
		}
		WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;

		LinearLayout lytInputs = (LinearLayout) mActivity.findViewById(R.id.pin_challenge_inputs);
		mDigitInputs = new TextView[mPinLength];
		mEnteredPin = new StringBuffer();
		mCurrentDigit = 0;
		mSendingPin = false;
		int cellWidth = width / 8;
		int cellSpacing = cellWidth / 4; 

		for (int x = 0; x < mPinLength; x++) {
			mDigitInputs[x] = new TextView(mActivity);
			mDigitInputs[x].setBackgroundResource(R.drawable.pin_input_edittext);
			mDigitInputs[x].setGravity(Gravity.CENTER);
			mDigitInputs[x].setTextSize(30);
			LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(cellWidth, cellWidth);
			lp1.setMargins(cellSpacing, 0, cellSpacing, 0);
			mDigitInputs[x].setLayoutParams(lp1);
			lytInputs.addView(mDigitInputs[x]);
		}

		GridView gridview = (GridView) mActivity.findViewById(R.id.pin_challenge_numpad);
		gridview.setAdapter(new ArrayAdapter<String>(mActivity, R.layout.pin_numpad_button_view, mNumPadArray));

		gridview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				if (mSendingPin)
					return;
				Log.w(TAG, "Tap detected at position " + position);
				if (position == 9)
					return; // empty box to the left of zero
				if (position == 11) {
					// special case for delete, clear digit at pointer

					if (mCurrentDigit > 0) {
						// remove the last character from our buffer
						if (mEnteredPin.length() > 0)
							mEnteredPin.deleteCharAt(mEnteredPin.length() - 1);
						mDigitInputs[mCurrentDigit - 1].setText("");
					}
					Log.i(TAG, "deleted character at " + mCurrentDigit + ", entered pin is: " + mEnteredPin);
					mCurrentDigit--;
					if (mCurrentDigit < 0)
						mCurrentDigit = 0;
					return;
				} else {
					// add the digit pressed to our stringbuffer
					mEnteredPin.append(mNumPadArray[position]);
					Log.i(TAG, "current entered pin: " + mEnteredPin);
					if (mCurrentDigit < mPinLength)
						mDigitInputs[mCurrentDigit].setText(R.string.pin_entry_dot);
					mCurrentDigit++;
				}
				// now check to see if we've just entered the last digit. if so, submit our answer
				if (mCurrentDigit >= mPinLength) {
					Log.i(TAG, "PIN entry complete, submitting answer: " + mEnteredPin);
					mSendingPin = true;
					// pass the answer text to the FSM
					AgentFSM fsm = ui.getAgentFSM();
					String plainAnswer = mEnteredPin.toString();
					String hashedAnswer = sha1Session(plainAnswer);

					fsm.makeAnswerChallengeCallWithID(mChallenge.getChallengeID(), hashedAnswer, "2.0");
				}
			}
		});
	}

	@Override
	public void retryChallenge() {
		// display a failure notice
		// clear entered digits
		mEnteredPin = new StringBuffer();
		mCurrentDigit = 0;
		mSendingPin = false;
		for (TextView t : mDigitInputs) {
			t.setText("");
		}
	}
}